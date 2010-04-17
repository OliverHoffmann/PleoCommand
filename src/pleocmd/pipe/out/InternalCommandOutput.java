package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.data.CommandData;
import pleocmd.pipe.data.Data;

public final class InternalCommandOutput extends Output {

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
		else if ("HELP".equals(arg))
			printHelp();
		else if ("ECHO".equals(arg))
			Log.consoleOut(data.getSafe(2).asString().replace("$ELAPSED",
					String.valueOf(getPipe().getFeedback().getElapsed())));
		else
			throw new OutputException(this, false,
					"Unknown internal command: '%s' in '%s'", arg, data);
		return true;
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

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Internal Commands Output";
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
