package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.InputException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.cfg.ConfigPath;
import pleocmd.pipe.data.Data;

public final class FileInput extends Input {

	private File file;

	private ReadType type;

	private DataInputStream in;

	public FileInput() {
		super(new Config().addV(
				new ConfigPath("File", ConfigPath.PathType.FileForReading))
				.addV(new ConfigEnum("ReadType", ReadType.values())));
	}

	@Override
	protected void configure0() {
		file = new File(((ConfigPath) getConfig().get(0)).getContent());
		type = ReadType.values()[((ConfigEnum) getConfig().get(1)).getContent()];
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

}
