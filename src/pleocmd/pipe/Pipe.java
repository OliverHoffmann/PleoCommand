package pleocmd.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pleocmd.Log;
import pleocmd.exc.ConverterException;
import pleocmd.exc.InputException;
import pleocmd.exc.OutputException;
import pleocmd.exc.PipeException;
import pleocmd.pipe.cmd.Command;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * @author oliver
 */
public final class Pipe {

	private final List<Input> inputList = new ArrayList<Input>();

	private final List<Output> outputList = new ArrayList<Output>();

	private final List<Converter> converterList = new ArrayList<Converter>();

	private int inputPosition;

	public List<Input> getInputList() {
		return Collections.unmodifiableList(inputList);
	}

	public List<Output> getOutputList() {
		return Collections.unmodifiableList(outputList);
	}

	public List<Converter> getConverterList() {
		return Collections.unmodifiableList(converterList);
	}

	public void addInput(final Input input) {
		inputList.add(input);
	}

	public void addConverter(final Converter converter) {
		converterList.add(converter);
	}

	public void addOutput(final Output output) {
		outputList.add(output);
	}

	private Data getFromInput() {
		Log.detail("Reading one data block from input");
		Input in;
		while (true) {
			assert inputPosition <= inputList.size();
			if (inputPosition >= inputList.size()) {
				Log.detail("InputList is empty");
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
					Log.detail("Skipping no longer working input " + in);
					in.tryClose();
					++inputPosition;
				} else
					Log.detail("Skipping one data block from input " + in);
				// try next data packet / try from next input
				continue;
			}
			// no more data available in this Input, so
			// switch to the next one
			Log.detail("Switching to next input");
			in.tryClose();
			++inputPosition;
			// try data packet from next input
		}
	}

	private List<Command> convertDataToCommandList(final Data data) {
		Log.detail("Converting data block to command(s)");
		for (int i = 0; i < converterList.size(); ++i) {
			final Converter cvt = converterList.get(i);
			try {
				if (cvt.canHandleData(data)) {
					Log.detail("Converting with " + cvt);
					return cvt.convertToCommand(data);
				}
			} catch (final ConverterException e) {
				Log.error(e);
				if (e.isPermanent()) {
					Log.detail("Removing no longer working converter " + cvt);
					cvt.tryClose();
					converterList.remove(i);
					--i;
				} else
					Log.detail("Skipping converter " + cvt
							+ " for one data block " + data);
			}
		}
		Log.error("No fitting Converter found for " + data.toString());
		return null;
	}

	private void writeAllCommands(final List<Command> list) {
		Log.detail("Writing " + list.size() + " command(s) to "
				+ outputList.size() + " output(s)");
		for (final Command cmd : list)
			for (int i = 0; i < outputList.size(); ++i) {
				final Output out = outputList.get(i);
				try {
					out.writeCommand(cmd);
				} catch (final OutputException e) {
					Log.error(e);
					if (e.isPermanent()) {
						Log.detail("Removing no longer working output " + out);
						out.tryClose();
						outputList.remove(i);
						--i;
					} else
						Log.detail("Skipping output " + out
								+ " for one command " + cmd);
				}
			}
	}

	public boolean pipeData() {
		// read next Data
		final Data data = getFromInput();
		if (data == null) return false; // marks end of all inputs

		// convert it
		final List<Command> cmdlist = convertDataToCommandList(data);
		if (cmdlist == null) return true;

		// and send all commands to all connected outputs
		writeAllCommands(cmdlist);
		return true;
	}

	public int pipeAllData() {
		int count = 0;
		while (pipeData())
			++count;
		Log.detail("Piped " + count + " data blocks");
		return count;
	}

	public void configuredAll() throws PipeException {
		Log.detail("Marking all input as configured");
		for (final Input in : inputList)
			in.configured();
		Log.detail("Marking all output as configured");
		for (final Output out : outputList)
			out.configured();
		Log.detail("Marking all converter as configured");
		for (final Converter cvt : converterList)
			cvt.configured();
	}

	public void initializeAll() throws PipeException {
		Log.detail("Initializing all input");
		for (final Input in : inputList)
			in.init();
		Log.detail("Initializing all output");
		for (final Output out : outputList)
			out.init();
		Log.detail("Initializing all converter");
		for (final Converter cvt : converterList)
			cvt.init();
	}

	public void closeAll() {
		Log.detail("Closing all input");
		for (final PipePart pp : inputList)
			pp.tryClose();
		Log.detail("Closing all output");
		for (final PipePart pp : outputList)
			pp.tryClose();
		Log.detail("Closing all converter");
		for (final PipePart pp : converterList)
			pp.tryClose();
	}

	public void reset() {
		closeAll();
		inputList.clear();
		outputList.clear();
		converterList.clear();
		inputPosition = 0;
	}

	public void writeToFile(final File file) throws IOException {
		final Writer out = new FileWriter(file);
		for (final PipePart pp : inputList) {
			out.write(pp.getClass().getSimpleName() + ":\n");
			pp.getConfig().writeToFile(out);
		}
		for (final PipePart pp : converterList) {
			out.write(pp.getClass().getSimpleName() + ":\n");
			pp.getConfig().writeToFile(out);
		}
		for (final PipePart pp : outputList) {
			out.write(pp.getClass().getSimpleName() + ":\n");
			pp.getConfig().writeToFile(out);
		}
		out.close();
	}

	public boolean readFromFile(final File file) throws IOException,
			PipeException {
		boolean skipped = false;
		reset();
		final BufferedReader in = new BufferedReader(new FileReader(file));
		while (true) {
			String line = in.readLine();
			if (line == null) break;
			line = line.trim();
			if (!line.endsWith(":")) throw new IOException("");
			final String cn = line.substring(0, line.length() - 1);
			final String pckName = getClass().getPackage().getName();
			String fcn;
			PipePart pp;
			try {
				try {
					fcn = pckName + ".in." + cn;
					pp = (PipePart) getClass().getClassLoader().loadClass(fcn)
							.newInstance();
				} catch (final ClassNotFoundException e1) {
					try {
						fcn = pckName + ".cvt." + cn;
						pp = (PipePart) getClass().getClassLoader().loadClass(
								fcn).newInstance();
					} catch (final ClassNotFoundException e2) {
						try {
							fcn = pckName + ".out." + cn;
							pp = (PipePart) getClass().getClassLoader()
									.loadClass(fcn).newInstance();
						} catch (final ClassNotFoundException e3) {
							throw new PipeException(null, true,
									"No PipePart with name " + cn
											+ " in any known package under "
											+ pckName + " exists");
						}
					}
				}
			} catch (final InstantiationException e) {
				throw new PipeException(null, true, "PipePart with name " + cn
						+ " cannot be instantiated");
			} catch (final IllegalAccessException e) {
				throw new PipeException(null, true, "PipePart with name " + cn
						+ " cannot be accessed");
			}
			try {
				pp.getConfig().readFromFile(in);
			} catch (final IOException e) {
				// skip this pipe part and try to read the next one
				Log.error(String.format("Skipped reading %s from file:", cn));
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
				throw new PipeException(pp, true, "PipePart with name " + cn
						+ " is not of any known type");
		}
		in.close();
		return !skipped;
	}
}
