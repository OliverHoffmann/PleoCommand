package pleocmd.pipe.out;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigPath;
import pleocmd.cfg.ConfigPath.PathType;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.exc.OutputException;
import pleocmd.pipe.data.Data;

public final class FileOutput extends Output {

	private final ConfigPath cfgFile;

	private final ConfigEnum<PrintType> cfgType;

	private DataOutputStream out;

	private Data lastRoot;

	public FileOutput() {
		addConfig(cfgFile = new ConfigPath("File", PathType.FileForWriting));
		addConfig(cfgType = new ConfigEnum<PrintType>(PrintType.class));
		cfgFile.setFileFilter(Arrays.asList(new FileFilter[] {
				new FileNameExtensionFilter("Ascii-Textfiles", "txt"),
				new FileNameExtensionFilter("Pleo Ascii Data", "pad"),
				new FileNameExtensionFilter("Pleo Binary Data", "pbd") }));
		constructed();
	}

	public FileOutput(final File file, final PrintType type)
			throws ConfigurationException {
		this();
		cfgFile.setContent(file);
		cfgType.setEnum(type);
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file '%s' for output of type '%s'", cfgFile
				.getContent(), cfgType.getEnum());
		out = new DataOutputStream(new FileOutputStream(cfgFile.getContent()));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", cfgFile.getContent());
		out.close();
		out = null;
		lastRoot = null;
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		Data root;
		switch (cfgType.getEnum()) {
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
			throw new InternalException(cfgType.getEnum());
		}
		return false;
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
