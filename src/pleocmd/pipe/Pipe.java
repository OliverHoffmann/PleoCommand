package pleocmd.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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

	private static long startTime;

	private final List<Input> inputList = new ArrayList<Input>();

	private final List<Output> outputList = new ArrayList<Output>();

	private final List<Converter> converterList = new ArrayList<Converter>();

	private final Set<Converter> ignoredConverter = new HashSet<Converter>();

	private final Set<Output> ignoredOutputs = new HashSet<Output>();

	private final Set<Long> deadlockDetection = new HashSet<Long>();

	private final DataQueue dataQueue = new DataQueue();

	private int inputPosition;

	private Thread thrInput;

	private Thread thrOutput;

	private boolean inputThreadInterruped;

	/**
	 * Creates a new {@link Pipe}.
	 */
	public Pipe() {
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
	}

	@Override
	protected void configure0() throws PipeException {
		Log.detail("Configuring all input");
		for (final Input in : inputList)
			in.configure();
		Log.detail("Configuring all converter");
		for (final Converter cvt : converterList)
			cvt.configure();
		Log.detail("Configuring all output");
		for (final Output out : outputList)
			out.configure();
	}

	@Override
	protected void init0() throws PipeException {
		Log.detail("Initializing all input");
		for (final Input in : inputList)
			in.init();
		Log.detail("Initializing all converter");
		for (final Converter cvt : converterList)
			cvt.init();
		Log.detail("Initializing all output");
		for (final Output out : outputList)
			out.init();
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
			inputList.get(i).tryClose();
		Log.detail("Closing all converter");
		for (final PipePart pp : converterList)
			if (!ignoredConverter.contains(pp)) pp.tryClose();
		Log.detail("Closing all output");
		for (final PipePart pp : outputList)
			if (!ignoredOutputs.contains(pp)) pp.tryClose();
		inputPosition = 0;
		ignoredConverter.clear();
		ignoredOutputs.clear();
	}

	/**
	 * Starts two threads which pipe all data of all connected {@link Input}s
	 * through the all connected {@link Converter} to all connected
	 * {@link Output}s.<br>
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
		init();
		dataQueue.resetCache();
		thrInput = new Thread() {
			@Override
			public void run() {
				try {
					runInputThread();
				} catch (final Throwable t) { // CS_IGNORE
					Log.error(t, "Input-Thread died");
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
				}
			}
		};
		startTime = System.currentTimeMillis();
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
		int count = 0;
		try {
			Log.info("Input-Thread started");
			while (!inputThreadInterruped) {
				ensureInitialized();

				// read next data block ...
				final Data data = getFromInput();
				if (data == null) break; // marks end of all inputs
				++count;

				// ... convert it ...
				deadlockDetection.clear();
				final List<Data> dataList = convertDataToDataList(data);

				// ... and put it into the queue for Output classes
				putIntoOutputQueue(dataList);
			}
		} finally {
			Log.info("Input-Thread finished");
			Log.detail("Read %d data blocks from input", count);
			dataQueue.close();
		}
	}

	/**
	 * This is the run() method of the Output-Thread.<br>
	 * It fetches {@link Data} from the {@link DataQueue} and passes it to the
	 * {@link Output}s in a loop until the {@link DataQueue} has been closed.
	 * <p>
	 * If the thread gets interrupted, only the current {@link Data} processing
	 * will be aborted. To interrupt the thread itself, one has to close the
	 * {@link DataQueue}.
	 * 
	 * @throws StateException
	 *             if, during looping, the {@link Pipe} exits the "Initialized"
	 *             state, which should never occur
	 */
	protected void runOutputThread() throws StateException {
		Log.info("Output-Thread started");
		int count = 0;
		try {
			while (true) {
				ensureInitialized();

				// fetch next data block ...
				final Data data;
				try {
					data = dataQueue.get();
				} catch (final InterruptedException e1) {
					Log.detail("Reading next data has been interrupted");
					continue;
				}
				if (data == null) break; // Input-Thread has finished piping
				++count;

				// ... and send it to all currently registered outputs
				writeDataToAllOutputs(data);
			}
		} finally {
			Log.info("Output-Thread finished");
			Log.detail("Sent %d data blocks to output", count);
		}
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
			try {
				if (in.canReadData()) {
					final Data res = in.readData();
					if (res == null)
						throw new InputException(in, false,
								"readData() returned null");
					return res;
				}
			} catch (final InputException e) {
				Log.error(e);
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
	 * Puts all {@link Data}s in the list to the {@link DataQueue}.<br>
	 * If a time is specified for the {@link Data} this method will wait for the
	 * correct time or print a warning if it's already behind.<br>
	 * Immediately returns if sleeping for timed {@link Data} has been
	 * interrupted
	 * 
	 * @param dataList
	 *            list of {@link Data} objects to put into the {@link DataQueue}
	 * @throws IOException
	 *             if the {@link DataQueue} has been closed
	 */
	private void putIntoOutputQueue(final List<Data> dataList)
			throws IOException {
		for (final Data data : dataList) {
			if (data.getTime() != Data.TIME_NOTIME) {
				final long delta = startTime + data.getTime()
						- System.currentTimeMillis();
				if (delta > 0) {
					Log.detail("Waiting %d ms", delta);
					try {
						Thread.sleep(delta);
					} catch (final InterruptedException e) {
						Log.error(e, "Failed to wait for correct output time");
						return;
					}
				} else if (delta < -MAX_BEHIND)
					Log.warn("Output of '%s' is %d ms behind", data, -delta);
				// TODO only warn for the first in dataList?
				else
					Log.detail("Output of '%s' is %d ms behind", data, -delta);
			}
			if (dataQueue.put(data)) {
				Log.info("Canceling current command, "
						+ "because of higher-priority command '%s'", data);
				thrOutput.interrupt();
			}
		}
	}

	/**
	 * Converts one {@link Data} object to a list of {@link Data} objects if a
	 * fitting {@link Converter} could be found. Otherwise the {@link Data}
	 * object itself is returned.
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
	 * permanently during the conversion.
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
				final List<Data> newDatas = cvt.convert(data);
				final List<Data> res = new ArrayList<Data>(newDatas.size());
				for (final Data newData : newDatas)
					res.addAll(convertDataToDataList(newData));
				return res;
			}
		} catch (final ConverterException e) {
			Log.error(e);
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
	 * have permanently failed.
	 * 
	 * @param data
	 *            {@link Data} to write
	 */
	private void writeDataToAllOutputs(final Data data) {
		Log.detail("Writing data block '%s' to %d output(s)", data, outputList
				.size());
		for (final Output out : outputList)
			if (!ignoredOutputs.contains(out)) writeToOutput(data, out);
	}

	/**
	 * Writes one {@link Data} object to one {@link Output}.
	 * 
	 * @param data
	 *            {@link Data} to write
	 * @param out
	 *            {@link Output} to write to
	 */
	private void writeToOutput(final Data data, final Output out) {
		try {
			out.write(data);
		} catch (final OutputException e) {
			Log.error(e);
			if (e.isPermanent()) {
				Log.info("Removing no longer working output '%s'", out);
				out.tryClose();
				ignoredOutputs.add(out);
			} else
				Log.info("Skipping output '%s' for one data block '%s'", out,
						data);
		}
	}

	/**
	 * Removes all connected {@link PipePart}s.
	 * 
	 * @throws PipeException
	 *             if {@link Pipe} is not configured or currently initialized
	 */
	public void reset() throws PipeException {
		ensureNoLongerInitialized();
		inputList.clear();
		converterList.clear();
		outputList.clear();
		assert inputPosition == 0;
		assert ignoredConverter.isEmpty();
		assert ignoredOutputs.isEmpty();
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
		}
		in.close();
		return !skipped;
	}

	public static long getStartTime() {
		if (startTime == 0)
			throw new RuntimeException("Cannot get start-time: There has "
					+ "never been any pipe started");
		return startTime;
	}

}
