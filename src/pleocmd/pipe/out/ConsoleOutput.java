package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.data.Data;

public final class ConsoleOutput extends Output {

	private final ConfigEnum<PrintType> cfg0;

	private PrintType type;

	private Data lastRoot;

	public ConsoleOutput() {
		getConfig().add(cfg0 = new ConfigEnum<PrintType>(PrintType.class));
		constructed();
	}

	@Override
	protected void configure0() {
		type = cfg0.getEnum();
	}

	@Override
	protected void init0() {
		// nothing to do
	}

	@Override
	protected void close0() {
		lastRoot = null;
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		Data root;
		switch (type) {
		case DataAscii:
			Log.consoleOut(data.toString());
			break;
		case DataBinary:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				data.writeToBinary(new DataOutputStream(out));
				Log.consoleOut(out.toString("ISO-8859-1"));
			}
			break;
		case DataAsciiOriginal:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				Log.consoleOut(data.getRoot().toString());
			}
			break;
		case DataBinaryOriginal:
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			data.getRoot().writeToBinary(new DataOutputStream(out));
			Log.consoleOut(out.toString("ISO-8859-1"));
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
