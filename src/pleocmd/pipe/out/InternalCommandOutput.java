package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.data.CommandData;
import pleocmd.pipe.data.Data;

public final class InternalCommandOutput extends Output { // NO_UCD

	public InternalCommandOutput() {
		constructed();
	}

	@Override
	public String getInputDescription() {
		return "SC";
	}

	@Override
	protected String getShortConfigDescr0() {
		return getName();
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		if (!CommandData.isCommandData(data, "SC")) return false;
		final String arg = CommandData.getArgument(data);
		if ("SLEEP".equals(arg))
			try {
				Thread.sleep(data.getSafe(2).asLong());
			} catch (final InterruptedException e) {
				throw new OutputException(this, false, e,
						"Executing '%s' failed", data);
			}
		else if ("HELP".equals(arg)) {
			if (data.size() == 2)
				printHelp();
			else
				for (int i = 2; i < data.size(); ++i)
					printHelp(data.getSafe(i).asString());
		} else if ("ECHO".equals(arg)) {
			String s = data.getSafe(2).asString();
			s = repl(s, "$ELAPSED", getPipe().getFeedback().getElapsed());
			s = repl(s, "$PACKETS", getPipe().getFeedback().getDataInputCount());
			s = repl(s, "$STATS", getPipe().getFeedback().toString());
			Log.consoleOut(s);
		} else
			throw new OutputException(this, false,
					"Unknown internal command: '%s' in '%s'", arg, data);
		return true;
	}

	private static String repl(final String s, final String id, final long value) {
		return s.replace(id, String.valueOf(value));
	}

	private static String repl(final String s, final String id,
			final String value) {
		return s.replace(id, value);
	}

	private void printHelp() {
		Log.consoleOut("All available inputs:");
		Log.consoleOut("---------------------");
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_INPUT)
			printHelp(cpp);

		Log.consoleOut("All available converter:");
		Log.consoleOut("------------------------");
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_CONVERTER)
			printHelp(cpp);

		Log.consoleOut("All available outputs:");
		Log.consoleOut("----------------------");
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_OUTPUT)
			printHelp(cpp);
	}

	private void printHelp(final String cppName) {
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_INPUT)
			if (cpp.getSimpleName().equals(cppName)) printHelp(cpp);
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_CONVERTER)
			if (cpp.getSimpleName().equals(cppName)) printHelp(cpp);
		for (final Class<? extends PipePart> cpp : PipePartDetection.ALL_OUTPUT)
			if (cpp.getSimpleName().equals(cppName)) printHelp(cpp);
	}

	private void printHelp(final Class<? extends PipePart> cpp) {
		// CS_IGNORE_NEXT format is correct in this context
		Log.consoleOut("%s:", PipePart.getName(cpp));
		Log.consoleOut(PipePart.getDescription(cpp));
		for (int i = 0;; ++i) {
			final String cfg = PipePart.getConfigHelp(cpp, i);
			if (cfg == null) break;
			Log.consoleOut("Config %d: %s", i, cfg);
		}
		Log.consoleOut("");
	}

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "Internal Commands";
		case Description:
			return "Processes special commands like SLEEP or HELP";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

}
