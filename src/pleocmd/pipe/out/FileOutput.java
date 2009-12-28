package pleocmd.pipe.out;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.cfg.ConfigPath;
import pleocmd.pipe.cfg.ConfigPath.PathType;
import pleocmd.pipe.data.Data;

public final class FileOutput extends Output {

	private final ConfigPath cfg0;

	private final ConfigEnum<PrintType> cfg1;

	private File file;

	private PrintType type;

	private DataOutputStream out;

	private Data lastRoot;

	public FileOutput() {
		getConfig().add(cfg0 = new ConfigPath("File", PathType.FileForWriting));
		getConfig().add(cfg1 = new ConfigEnum<PrintType>(PrintType.class));
		constructed();
	}

	@Override
	protected void configure0() {
		file = new File(cfg0.getContent());
		type = cfg1.getEnum();
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file '%s' for output of type '%s'", file, type);
		out = new DataOutputStream(new FileOutputStream(file));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", file);
		out.close();
		out = null;
		lastRoot = null;
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		Data root;
		switch (type) {
		case DataAscii:
			data.writeToAscii(out, true);
			break;
		case DataBinary:
			data.writeToBinary(out);
			break;
		case DataAsciiOriginal:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				root.writeToAscii(out, true);
			}
			break;
		case DataBinaryOriginal:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				data.getRoot().writeToBinary(out);
			}
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
			return "File Output";
		case Description:
			return "Writes Data blocks to external files";
		case Configuration:
			return "1: Path to an external file to which commands should"
					+ "be written in either Ascii or binary form\n"
					+ "2: 'Ascii' if Data blocks will be in Ascii format or\n"
					+ "   'Binary' if Data blocks will be written as binary";
		default:
			return "???";
		}
	}

}
