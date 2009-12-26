package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.data.Data;

public final class InternalCommandOutput extends Output {

	public InternalCommandOutput() {
		super(new Config());
	}

	@Override
	protected void configure0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void init0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void close0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		if ("SC".equals(data.getSafe(0).asString())) {
			final String v2 = data.get(1).asString();
			if ("SLEEP".equals(v2))
				try {
					Thread.sleep(data.getSafe(2).asLong());
				} catch (final InterruptedException e) {
					Log.error(e, "Executing '%s' failed", data);
				}
			else if ("HELP".equals(v2))
				printHelp();
			else
				throw new OutputException(this, false,
						"Unknown internal command: '%s' in '%s'", v2, data);
		}
	}

	private void printHelp() {
		Log.consoleOut("All available inputs:");
		Log.consoleOut("---------------------");
		for (final Class<PipePart> cpp : PipePartDetection
				.getAllPipeParts("in"))
			printHelp(cpp);

		Log.consoleOut("All available converter:");
		Log.consoleOut("------------------------");
		for (final Class<PipePart> cpp : PipePartDetection
				.getAllPipeParts("cvt"))
			printHelp(cpp);

		Log.consoleOut("All available outputs:");
		Log.consoleOut("----------------------");
		for (final Class<PipePart> cpp : PipePartDetection
				.getAllPipeParts("out"))
			printHelp(cpp);
	}

	private void printHelp(final Class<PipePart> cpp) {
		// CS_IGNORE_NEXT format is correct in this context
		Log.consoleOut("%s:", PipePartDetection.callHelp(cpp, HelpKind.Name));
		Log.consoleOut(PipePartDetection.callHelp(cpp, HelpKind.Description));
		final String cfg = PipePartDetection.callHelp(cpp,
				HelpKind.Configuration);
		if (!cfg.isEmpty()) Log.consoleOut(cfg);
		Log.consoleOut("");
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Internal Commands Output";
		case Description:
			return "Processes special commands, which don't need to be "
					+ "send to external targets:\n"
					+ "SC|SLEEP|<duration>\tSleeps the for duration specified "
					+ "in milliseconds\n"
					+ "SC|HELP\t\t\tPrint help for all currently known PipeParts "
					+ "to the standard output";
		case Configuration:
			return "";
		default:
			return "???";
		}
	}

}
