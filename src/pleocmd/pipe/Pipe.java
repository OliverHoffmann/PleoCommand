package pleocmd.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pleocmd.Log;
import pleocmd.exc.ConverterException;
import pleocmd.exc.InputException;
import pleocmd.exc.OutputException;
import pleocmd.exc.PipeException;
import pleocmd.exc.StateException;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.DataQueue;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * The central processing point of {@link Data} objects.
 * 
 * @author oliver
 */
public final class Pipe extends StateHandling {

	private static final int MAX_BEHIND = 300;

	/**
	 * Number of milliseconds to reduce waiting-time in the input thread for
	 * timed {@link Data}. before it's passed to the output thread's
	 * {@link #waitForOutputTime(Data)}
	 * <p>
	 * If too small (smaller than time needed to pass from Input thread via
	 * {@link #dataQueue} to Output thread), very short delays for timed
	 * {@link Data} may occur in some situations.<br>
	 * If too large, {@link Data}s may be dropped or interrupted without need if
	 * a timed {@link Data} with a different priority follows.<br>
	 * Typical values are in [0, 40] for fast and [25, 200] for a slow computer.
	 */
	private static final long OVERHEAD_REDUCTION_TIME = 10;

	/**
	 * Number of milliseconds it approximately takes from
	 * {@link #waitForOutputTime(Data)} via {@link #writeDataToAllOutputs(Data)}
	 * to {@link Output#write(Data)}
	 * <p>
	 * If too small, very short delays for timed {@link Data} may occur.<br>
	 * If too large, timed {@link Data}s may be executed too early.<br>
	 * Typical values are in [0, 10].<br>
	 * Should not be larger than {@link #OVERHEAD_REDUCTION_TIME}<br>
	 * Note that if {@link DataQueue#OUTPUT_THREAD_SLEEP_TIME} is too large, a
	 * delay may already have occurred which can't be compensated here.
	 */
	private static final long OUTPUT_INIT_OVERHEAD = 2;

	private final List<Input> inputList = new ArrayList<Input>();

	private final List<Output> outputList = new ArrayList<Output>();

	private final List<Converter> converterList = new ArrayList<Converter>();

	private final Set<PipePart> ignoredInputs = new HashSet<PipePart>();

	private final Set<PipePart> ignoredConverter = new HashSet<PipePart>();

	private final Set<PipePart> ignoredOutputs = new HashSet<PipePart>();

	private final Set<Long> deadlockDetection = new HashSet<Long>();

	private final DataQueue dataQueue = new DataQueue();

	private int inputPosition;

	private Thread thrInput;

	private Thread thrOutput;

	private boolean inputThreadInterruped;

	private PipeFeedback feedback;

	/**
	 * Creates a new {@link Pipe}.
	 */
	public Pipe() {
		feedback = new PipeFeedback();
		constructed();
	}

	/**
	 * @return an unmodifiable list of all currently connected {@link Input}s
	 */
	public List<Input> getInputList() {
		return Collections.unmodifiableList(inputList);
	}

	/**
	 * @return an unmodifiable list of all currently connected {@link Output}s
	 */
	public List<Output> getOutputList() {
		return Collections.unmodifiableList(outputList);
	}

	/**
	 * @return an unmodifiable list of all currently connected {@link Converter}
	 */
	public List<Converter> getConverterList() {
		return Collections.unmodifiableList(converterList);
	}

	/**
	 * Adds a new {@link Input} to the list of currently connected ones.
	 * 
	 * @param input
	 *            the new {@link Input}
	 * @throws StateException
	 *             if the {@link Pipe} is being constructed or already
	 *             initialized
	 */
	public void addInput(final Input input) throws StateException {
		ensureConstructed();
		Log.detail("Connecting pipe with input '%s'", input);
		inputList.add(input);
		((PipePart) input).connectedToPipe(this);
	}

	/**
	 * Adds a new {@link Converter} to the list of currently connected ones.
	 * 
	 * @param output
	 *            the new {@link Converter}
	 * @throws StateException
	 *             if the {@link Pipe} is being constructed or already
	 *             initialized
	 */
	public void addOutput(final Output output) throws StateException {
		ensureConstructed();
		Log.detail("Connecting pipe with output '%s'", output);
		outputList.add(output);
		((PipePart) output).connectedToPipe(this);
	}

	/**
	 * Adds a new {@link Output} to the list of currently connected ones.
	 * 
	 * @param converter
	 *            the new {@link Output}
	 * @throws StateException
	 *             if the {@link Pipe} is being constructed or already
	 *             initialized
	 */
	public void addConverter(final Converter converter) throws StateException {
		ensureConstructed();
		Log.detail("Connecting pipe with converter '%s'", converter);
		converterList.add(converter);
		((PipePart) converter).connectedToPipe(this);
	}

	@Override
	protected void configure0() throws PipeException {
		Log.detail("Configuring all input");
		for (final PipePart pp : inputList)
			if (!pp.tryConfigure()) ignoredInputs.add(pp);

		Log.detail("Configuring all converter");
		for (final PipePart pp : converterList)
			if (!pp.tryConfigure()) ignoredConverter.add(pp);

		Log.detail("Configuring all output");
		for (final PipePart pp : outputList)
			if (!pp.tryConfigure()) ignoredOutputs.add(pp);
	}

	@Override
	protected void init0() throws PipeException {
		Log.detail("Initializing all input");
		for (final PipePart pp : inputList)
			if (!pp.tryInit()) ignoredInputs.add(pp);

		Log.detail("Initializing all converter");
		for (final PipePart pp : converterList)
			if (!pp.tryInit()) ignoredConverter.add(pp);

		Log.detail("Initializing all output");
		for (final PipePart pp : outputList)
			if (!pp.tryInit()) ignoredOutputs.add(pp);
	}

	@Override
	protected void close0() throws PipeException {
		synchronized (this) {
			if (thrInput != null || thrOutput != null)
				throw new PipeException(this, false,
						"Background threads are still alive");
		}
		Log.detail("Closing all input");
		for (int i = inputPosition; i < inputList.size(); ++i)
			if (!ignoredInputs.contains(inputList.get(i)))
				inputList.get(i).tryClose();
		Log.detail("Closing all converter");
		for (final PipePart pp : converterList)
			if (!ignoredConverter.contains(pp)) pp.tryClose();
		Log.detail("Closing all output");
		for (final PipePart pp : outputList)
			if (!ignoredOutputs.contains(pp)) pp.tryClose();
		inputPosition = 0;
		ignoredInputs.clear();
		ignoredConverter.clear();
		ignoredOutputs.clear();
	}

	/**
	 * Starts two threads which pipe all data of all connected {@link Input}s
	 * through all connected {@link Converter} to all connected {@link Output}s.<br>
	 * Waits until both threads have finished.<br>
	 * The {@link Pipe} is initialized before starting and closed after
	 * finishing.
	 * 
	 * @throws PipeException
	 *             if the object is not already initialized
	 * @throws InterruptedException
	 *             if any thread has interrupted the current thread while
	 *             waiting for the two pipe threads
	 */
	public void pipeAllData() throws PipeException, InterruptedException {
		feedback = new PipeFeedback();
		init();
		dataQueue.resetCache();
		thrInput = new Thread() {
			@Override
			public void run() {
				try {
					runInputThread();
				} catch (final Throwable t) { // CS_IGNORE
					Log.error(t, "Input-Thread died");
					getFeedback().addError(t, true);
				}
			}
		};
		thrOutput = new Thread() {
			@Override
			public void run() {
				try {
					runOutputThread();
				} catch (final Throwable t) { // CS_IGNORE
					Log.error(t, "Output-Thread died");
					getFeedback().addError(t, true);
				}
			}
		};
		feedback.started();
		thrOutput.start();
		thrInput.start();

		Log.detail("Started waiting for threads");
		while (thrOutput.isAlive())
			Thread.sleep(100);
		Log.detail("Output Thread no longer alive");
		if (thrInput.isAlive()) {
			Log.error("Input-Thread still alive but Output-Thread died");
			inputThreadInterruped = true;
			thrInput.interrupt();
			while (thrInput.isAlive())
				Thread.sleep(100);
		}
		Log.detail("Input Thread no longer alive");
		feedback.stopped();
		thrInput = null;
		thrOutput = null;
		close();
		Log.info("Pipe finished and closed");
	}

	/**
	 * Aborts the pipe if one is currently running.<br>
	 * Note that {@link #pipeAllData()} itself blocks until the pipe has
	 * finished, so {@link #abortPipe()} only makes sence if
	 * {@link #pipeAllData()} is called from another thread. <br>
	 * This method waits until the abort has been accepted.
	 * 
	 * @throws StateException
	 *             if the object is not already initialized
	 * @throws InterruptedException
	 *             if waiting has been interrupted
	 */
	public void abortPipe() throws StateException, InterruptedException {
		ensureInitialized();
		Log.info("Aborting pipe");
		inputThreadInterruped = true;
		thrInput.interrupt();
		dataQueue.close();
		thrOutput.interrupt();
		Log.detail("Waiting for accepted abort in threads");
		while (thrInput != null || thrOutput != null)
			Thread.sleep(100);
		Log.info("Pipe successfully aborted");
	}

	/**
	 * This is the run() method of the Input-Thread.<br>
	 * It fetches {@link Data} from the {@link Input}s, passes it to the
	 * {@link Converter} and puts it into the {@link DataQueue} in a loop until
	 * all {@link Input}s have finished or the thread gets interrupted.
	 * 
	 * @throws StateException
	 *             if, during looping, the {@link Pipe} exits the "Initialized"
	 *             state, which should never occur
	 * @throws IOException
	 *             if the {@link DataQueue} has been closed during looping
	 */
	protected void runInputThread() throws StateException, IOException {
		inputThreadInterruped = false;
		try {
			Log.info("Input-Thread started");
			while (!inputThreadInterruped) {
				ensureInitialized();

				// read next data block ...
				final Data data = getFromInput();
				if (data == null) break; // marks end of all inputs

				// ... convert it ...
				deadlockDetection.clear();
				final List<Data> dataList = convertDataToDataList(data);

				// ... and put it into the queue for Output classes
				putIntoOutputQueue(dataList);
			}
		} finally {
			Log.info("Input-Thread finished");
			Log.detail("Read %d data blocks from input", feedback
					.getDataInputCount());
			dataQueue.close();
		}
	}

	/**
	 * This is the run() method of the Output-Thread.<br>
	 * It fetches {@link Data} from the {@link DataQueue} and passes it to the
	 * {@link Output}s in a loop until the {@link DataQueue} has been closed.
	 * <p>
	 * If the thread gets interrupted, only writing of the current {@link Data}
	 * will be aborted. To interrupt the thread itself, one has to close the
	 * {@link DataQueue}.
	 * 
	 * @throws StateException
	 *             if, during looping, the {@link Pipe} exits the "Initialized"
	 *             state, which should never occur
	 */
	protected void runOutputThread() throws StateException {
		Log.info("Output-Thread started");
		try {
			while (true) {
				ensureInitialized();

				// fetch next data block ...
				final Data data;
				try {
					data = dataQueue.get();
				} catch (final InterruptedException e1) {
					Log.detail("Reading next data has been interrupted");
					feedback.incInterruptionCount();
					continue;
				}
				if (data == null) break; // Input-Thread has finished piping

				// There's no need to continue if we have no more outputs
				if (ignoredOutputs.size() == outputList.size()) break;

				// ... wait for the correct time, if needed ...
				if (!waitForOutputTime(data)) continue;

				// ... and send it to all currently registered outputs
				writeDataToAllOutputs(data);
			}
		} finally {
			Log.info("Output-Thread finished");
			Log.detail("Sent %d data blocks to output", feedback
					.getDataOutputCount());
		}
	}

	private boolean waitForOutputTime(final Data data) {
		if (data.getTime() == Data.TIME_NOTIME) return true;
		final long execTime = feedback.getStartTime() + data.getTime();
		final long delta = execTime - System.currentTimeMillis()
				- OUTPUT_INIT_OVERHEAD;
		if (delta > 0) {
			Log.detail("Waiting %d ms", delta);
			try {
				Thread.sleep(delta);
			} catch (final InterruptedException e) {
				Log.error(e, "Failed to wait %d ms for "
						+ "correct output time", delta);
				// no incInterruptionCount() here
				return false;
			}
			return true;
		}
		final boolean significant = delta < -MAX_BEHIND;
		feedback.incBehindCount(-delta, significant);
		if (significant)
			// TODO only warn for the first in dataList?
			Log.warn("Output of '%s' is %d ms behind (should have been "
					+ "executed at '%s')", data, -delta, Log.DATE_FORMATTER
					.format(new Date(execTime)));
		else if (Log.canLog(Log.Type.Detail))
			Log.detail("Output of '%s' is %d ms behind (should have been "
					+ "executed at '%s')", data, -delta, Log.DATE_FORMATTER
					.format(new Date(execTime)));
		return true;
	}

	/**
	 * Tries to read one {@link Data} block from the currently active
	 * {@link Input}.<br>
	 * If the {@link Input} has no more {@link Data} available or it fails, the
	 * next {@link Input} in the list will be used.<br>
	 * If there are no more available {@link Input}s, <b>null</b> will be
	 * returned.
	 * 
	 * @return a new {@link Data} or <b>null</b> if no {@link Input} in the list
	 *         has any more available {@link Data}
	 */
	private Data getFromInput() {
		Log.detail("Reading one data block from input");
		Input in;
		while (true) {
			if (inputThreadInterruped) return null;
			assert inputPosition <= inputList.size();
			if (inputPosition >= inputList.size()) {
				Log.detail("Finished InputList");
				return null;
			}
			in = inputList.get(inputPosition);
			if (ignoredInputs.contains(in)) {
				Log.detail("Skipping input '%s' which failed "
						+ "in config/init phase", in);
				++inputPosition;
				continue;
			}
			try {
				if (in.canReadData()) {
					final Data res = in.readData();
					feedback.incDataInputCount();
					if (res == null)
						throw new InputException(in, false,
								"readData() returned null");
					return res;
				}
			} catch (final InputException e) {
				Log.error(e);
				feedback.addError(e, e.isPermanent());
				if (e.isPermanent()) {
					Log.info("Skipping no longer working input '%s'", in);
					in.tryClose();
					++inputPosition;
				} else
					Log.info("Skipping one data block from input '%s'", in);
				// try next data packet / try from next input
				continue;
			}
			// no more data available in this Input, so
			// switch to the next one
			Log.info("Switching to next input");
			in.tryClose();
			++inputPosition;
			// try data packet from next input
		}
	}

	/**
	 * Puts all {@link Data}s in the list to the {@link DataQueue}.
	 * <p>
	 * Drops the {@link Data} if it's priority is lower than the one in the
	 * queue. <br>
	 * Clears the queue and interrupts the output thread if the {@link Data}'s
	 * priority is higher than the one in the queue.
	 * <p>
	 * If a time is specified for the {@link Data} this method will wait for the
	 * correct time before it decides whether the {@link Data} has to be dropped
	 * or the queue be cleared.<br>
	 * Immediately returns if sleeping for timed {@link Data} has been
	 * interrupted.
	 * 
	 * @param dataList
	 *            list of {@link Data} objects to put into the {@link DataQueue}
	 * @throws IOException
	 *             if the {@link DataQueue} has been closed
	 */
	private void putIntoOutputQueue(final List<Data> dataList)
			throws IOException {
		for (final Data data : dataList) {

			// if time-to-wait is positive we wait here before we are
			// forced to immediately drop a data block or clear the queue
			// TODO wait here only if needed
			if (data.getTime() != Data.TIME_NOTIME) {
				final long execTime = feedback.getStartTime() + data.getTime();
				final long delta = execTime - System.currentTimeMillis()
						- OVERHEAD_REDUCTION_TIME;
				if (delta > 0) {
					Log.detail("Waiting %d ms", delta);
					try {
						Thread.sleep(delta);
					} catch (final InterruptedException e) {
						Log.error(e, "Failed to wait %d ms for "
								+ "correct output time", delta);
						// no incInterruptionCount() here
						return;
					}
				}
			}

			switch (dataQueue.put(data)) {
			case ClearedAndPut:
				Log.info("Canceling current command, "
						+ "because of higher-priority command '%s'", data);
				thrOutput.interrupt();
				feedback.incDropCount(dataQueue.getSizeBeforeClear());
				break;
			case Dropped:
				feedback.incDropCount();
				break;
			case Put:
				break;
			}
		}
	}

	/**
	 * Converts one {@link Data} object to a list of {@link Data} objects if a
	 * fitting {@link Converter} can be found. Otherwise the {@link Data} object
	 * itself is returned.<br>
	 * This method recursively calls {@link #convertOneData(Data, Converter)}
	 * which again calls this method.
	 * 
	 * @param data
	 *            The {@link Data} object to be converted.
	 * @return A single-element list holding the data object given by
	 *         <b>data</b> if no fitting {@link Converter} could be found or a
	 *         list of new {@link Data} objects returned by the first fitting
	 *         {@link Converter}.
	 */
	private List<Data> convertDataToDataList(final Data data) {
		Log.detail("Converting data block to list of data blocks");
		List<Data> res = null;
		for (final Converter cvt : converterList)
			if (!ignoredConverter.contains(cvt)
					&& (res = convertOneData(data, cvt)) != null) return res;

		// no fitting (and not ignored and working) converter found
		Log.info("No Converter found, returning data as is: '%s'", data);
		res = new ArrayList<Data>(1);
		res.add(data);
		return res;
	}

	/**
	 * Tries to convert the given {@link Data} block with the {@link Converter}.
	 * The converter is added to the {@link #ignoredConverter} list if it fails
	 * permanently during the conversion.<br>
	 * All {@link Data}s returned by the converter will immediately be converted
	 * again.<br>
	 * This method recursively calls {@link #convertDataToDataList(Data)} which
	 * again calls this method.
	 * 
	 * @param data
	 *            {@link Data} to convert
	 * @param cvt
	 *            {@link Converter} to use for conversion
	 * @return list of {@link Data} created from {@link Converter} or
	 *         <b>null</b> if the {@link Converter} could not handle the
	 *         {@link Data} or an error occurred during conversion
	 */
	private List<Data> convertOneData(final Data data, final Converter cvt) {
		try {
			if (cvt.canHandleData(data)) {
				Log.detail("Converting '%s' with '%s'", data, cvt);
				final long id = (long) cvt.hashCode() << 32 | data.hashCode();
				if (!deadlockDetection.add(id))
					throw new ConverterException(cvt, true,
							"Detected dead-lock");
				feedback.incDataConvertedCount();
				final List<Data> newDatas = cvt.convert(data);
				final List<Data> res = new ArrayList<Data>(newDatas.size());
				for (final Data newData : newDatas)
					res.addAll(convertDataToDataList(newData));
				return res;
			}
		} catch (final ConverterException e) {
			Log.error(e);
			feedback.addError(e, e.isPermanent());
			if (e.isPermanent()) {
				Log.info("Removing no longer working converter '%s'", cvt);
				cvt.tryClose();
				ignoredConverter.add(cvt);
			} else
				Log.info("Skipping converter '%s' for one data block '%s'",
						cvt, data);
		}
		return null;
	}

	/**
	 * Writes the given {@link Data} to all {@link Output}s ignoring those which
	 * have permanently failed.<br>
	 * Complains if the {@link Data} has not been accepted by at least one
	 * {@link Output}.
	 * 
	 * @param data
	 *            {@link Data} to write
	 */
	private void writeDataToAllOutputs(final Data data) {
		Log.detail("Writing data block '%s' to %d output(s)", data, outputList
				.size());
		boolean foundOne = false;
		for (final Output out : outputList)
			if (!ignoredOutputs.contains(out))
				foundOne |= writeToOutput(data, out);
		if (!foundOne) {
			final Throwable t = new OutputException(null, false,
					"Skipping data block '%s' because no fitting "
							+ "output has been found", data);
			Log.error(t);
			feedback.addError(t, false);
		}
	}

	/**
	 * Writes one {@link Data} object to one {@link Output}.
	 * 
	 * @param data
	 *            {@link Data} to write
	 * @param out
	 *            {@link Output} to write to
	 * @return true if the {@link Output} accepted the {@link Data}
	 */
	private boolean writeToOutput(final Data data, final Output out) {
		try {
			if (!out.write(data)) return false;
		} catch (final OutputException e) {
			Log.error(e);
			if (!e.isPermanent()
					&& e.getCause() instanceof InterruptedException)
				feedback.incInterruptionCount();
			else
				feedback.addError(e, e.isPermanent());
			if (e.isPermanent()) {
				Log.info("Removing no longer working output '%s'", out);
				out.tryClose();
				ignoredOutputs.add(out);
			} else
				Log.info("Skipping output '%s' for one data block '%s'", out,
						data);
		}
		feedback.incDataOutputCount();
		return true;
	}

	/**
	 * Removes all connected {@link PipePart}s.
	 * 
	 * @throws PipeException
	 *             if {@link Pipe} is not configured or currently initialized
	 */
	public void reset() throws PipeException {
		ensureNoLongerInitialized();
		for (final PipePart pp : inputList)
			pp.disconnectedFromPipe(this);
		for (final PipePart pp : converterList)
			pp.disconnectedFromPipe(this);
		for (final PipePart pp : outputList)
			pp.disconnectedFromPipe(this);
		inputList.clear();
		converterList.clear();
		outputList.clear();
		assert inputPosition == 0;
		assert ignoredInputs.isEmpty();
		assert ignoredConverter.isEmpty();
		assert ignoredOutputs.isEmpty();
		assert feedback.getStopTime() > 0;
	}

	/**
	 * Writes the lists of connected {@link PipePart}s to a file.
	 * 
	 * @param file
	 *            {@link File} to write the configuration to
	 * @throws IOException
	 *             on write failures
	 * @throws PipeException
	 *             if {@link Pipe} is not configured or currently initialized
	 */
	public void writeToFile(final File file) throws IOException, PipeException {
		ensureNoLongerInitialized();
		final Writer out = new FileWriter(file);
		for (final PipePart pp : inputList) {
			out.write(pp.getClass().getSimpleName());
			out.write(":\n");
			pp.getConfig().writeToFile(out);
		}
		for (final PipePart pp : converterList) {
			out.write(pp.getClass().getSimpleName());
			out.write(":\n");
			pp.getConfig().writeToFile(out);
		}
		for (final PipePart pp : outputList) {
			out.write(pp.getClass().getSimpleName());
			out.write(":\n");
			pp.getConfig().writeToFile(out);
		}
		out.close();
	}

	/**
	 * Creates the lists of connected {@link PipePart}s from a file.
	 * 
	 * @param file
	 *            {@link File} to read the configuration from
	 * @throws IOException
	 *             on read failures
	 * @throws PipeException
	 *             if {@link Pipe} is not configured or currently initialized
	 * @return true if all {@link PipePart}s could be restored from the file,
	 *         false it at least one failed to load.
	 */
	public boolean readFromFile(final File file) throws IOException,
			PipeException {
		ensureNoLongerInitialized();
		boolean skipped = false;
		reset();
		final BufferedReader in = new BufferedReader(new FileReader(file));
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			line = line.trim();
			if (!line.endsWith(":"))
				throw new IOException("Missing ':' at end of line");
			final String cn = line.substring(0, line.length() - 1);
			final String pckName = getClass().getPackage().getName();
			String fcn;
			PipePart pp;
			// TODO combine with PipePartDetection
			try {
				try {
					fcn = String.format("%s.in.%s", pckName, cn);
					pp = (PipePart) getClass().getClassLoader().loadClass(fcn)
							.newInstance();
				} catch (final ClassNotFoundException e1) {
					try {
						fcn = String.format("%s.cvt.%s", pckName, cn);
						pp = (PipePart) getClass().getClassLoader().loadClass(
								fcn).newInstance();
					} catch (final ClassNotFoundException e2) {
						try {
							fcn = String.format("%s.out.%s", pckName, cn);
							pp = (PipePart) getClass().getClassLoader()
									.loadClass(fcn).newInstance();
						} catch (final ClassNotFoundException e3) {
							throw new PipeException(null, false,
									"Cannot find PipePart with class-name '%s' in any "
											+ "package under '%s'", cn, pckName);
						}
					}
				}
			} catch (final InstantiationException e) {
				throw new PipeException(null, false, e,
						"Cannot create PipePart of class '%s'", cn);
			} catch (final IllegalAccessException e) {
				throw new PipeException(null, false, e,
						"Cannot create PipePart of class '%s'", cn);
			}
			try {
				in.mark(0);
				pp.getConfig().readFromFile(in);
			} catch (final IOException e) {
				// skip this pipe part and try to read the next one
				Log.error("Skipped reading '%s' from file:", cn);
				Log.error(e);
				in.reset();
				skipped = true;
				continue;
			}
			if (pp instanceof Input)
				inputList.add((Input) pp);
			else if (pp instanceof Converter)
				converterList.add((Converter) pp);
			else if (pp instanceof Output)
				outputList.add((Output) pp);
			else
				throw new PipeException(pp, true,
						"Cannot create PipePart of class '%s': "
								+ "Unknown super class", cn);
			pp.connectedToPipe(this);
		}
		in.close();
		return !skipped;
	}

	public PipeFeedback getFeedback() {
		return feedback;
	}

	@Override
	public String toString() {
		return String.format("%s: %s - %s - %s <%s>", getClass()
				.getSimpleName(), inputList, converterList, outputList,
				getState());
	}

}
