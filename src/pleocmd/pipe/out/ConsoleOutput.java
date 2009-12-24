package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.data.Data;

public final class ConsoleOutput extends Output {

	private PrintType type;

	public ConsoleOutput() {
		super(new Config()
				.addV(new ConfigEnum("PrintType", PrintType.values())));
	}

	@Override
	protected void configure0() {
		type = PrintType.values()[((ConfigEnum) getConfig().get(0))
				.getContent()];
	}

	@Override
	protected void init0() {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		switch (type) {
		case DataBinary:
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			data.writeToBinary(new DataOutputStream(out));
			Log.consoleOut(out.toString("ISO-8859-1"));
			break;
		case DataAscii:
			Log.consoleOut(data.toString());
			break;
		case PleoMonitorCommands:
			if ("PMC".equals(data.getSafe(0).asString()))
				Log.consoleOut(data.get(1).asString());
			break;
		default:
			throw new InternalError(String.format("Invalid print-type: %s",
					type));
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Console Output";
		case Description:
			return "Writes Data blocks to the standard output";
		case Configuration:
			return "1: 'Ascii' if Data blocks will be in Ascii format or\n"
					+ "   'Binary' if Data blocks will be written as binary";
		default:
			return "???";
		}
	}

}
