package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.InputException;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.cfg.ConfigPath;
import pleocmd.pipe.cfg.ConfigPath.PathType;
import pleocmd.pipe.data.Data;

public final class FileInput extends Input {

	private final ConfigPath cfg0;

	private final ConfigEnum<ReadType> cfg1;

	private File file;

	private ReadType type;

	private DataInputStream in;

	public FileInput() {
		getConfig().add(cfg0 = new ConfigPath("File", PathType.FileForReading));
		getConfig().add(cfg1 = new ConfigEnum<ReadType>(ReadType.class));
		constructed();
	}

	public FileInput(final File file, final ReadType type) {
		this();
		cfg0.setContent(file);
		cfg1.setEnum(type);
	}

	@Override
	protected void configure0() {
		file = cfg0.getContent();
		type = cfg1.getEnum();
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file '%s' for input", file);
		in = new DataInputStream(new FileInputStream(file));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", file);
		in.close();
		in = null;
	}

	@Override
	protected boolean canReadData0() throws IOException {
		return in.available() > 0;
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		switch (type) {
		case Ascii:
			return Data.createFromAscii(in);
		case Binary:
			return Data.createFromBinary(in);
		default:
			throw new InternalError(String
					.format("Invalid read-type: %s", type));
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "File Input";
		case Description:
			return "Reads Data blocks from external files";
		case Configuration:
			return "1: Path to an external file from which commands should "
					+ "be read in either Ascii or binary form\n"
					+ "2: 'Ascii' if Data blocks are in Ascii format or\n"
					+ "   'Binary' if Data blocks should be treated as binary";
		default:
			return "???";
		}
	}

}
