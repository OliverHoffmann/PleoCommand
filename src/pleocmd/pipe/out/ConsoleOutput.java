package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.InternalException;
import pleocmd.exc.OutputException;
import pleocmd.pipe.data.Data;

public final class ConsoleOutput extends Output {

	private final ConfigEnum<PrintType> cfgType;

	private Data lastRoot;

	public ConsoleOutput() {
		addConfig(cfgType = new ConfigEnum<PrintType>(PrintType.class));
		constructed();
	}

	public ConsoleOutput(final PrintType type) {
		this();
		cfgType.setEnum(type);
	}

	@Override
	protected void configure0() {
		// nothing to do
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
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		Data root;
		switch (cfgType.getEnum()) {
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
			throw new InternalException(cfgType.getEnum());
		}
		return true;
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
