package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.RunnableWithArgument;
import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigPath;
import pleocmd.cfg.ConfigPath.PathType;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InputException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.DataFileEditFrame;
import pleocmd.pipe.data.Data;

public final class FileInput extends Input {

	private final ConfigPath cfgFile;

	private final ConfigEnum<ReadType> cfgType;

	private DataInputStream in;

	public FileInput() {
		addConfig(cfgFile = new ConfigPath("File", PathType.FileForReading));
		addConfig(cfgType = new ConfigEnum<ReadType>(ReadType.class));
		cfgFile.setFileFilter(Arrays.asList(new FileFilter[] {
				new FileNameExtensionFilter("Ascii-Textfiles", "txt"),
				new FileNameExtensionFilter("Pleo Ascii Data", "pad"),
				new FileNameExtensionFilter("Pleo Binary Data", "pbd") }));
		cfgFile.setModifyFile(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				new DataFileEditFrame(new File((String) args[0]))
						.freeResources();
				return null;
			}
		});
		constructed();
	}

	public FileInput(final File file, final ReadType type)
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
		Log.detail("Opening file '%s' for input", cfgFile.getContent());
		in = new DataInputStream(new FileInputStream(cfgFile.getContent()));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", cfgFile.getContent());
		in.close();
		in = null;
	}

	@Override
	protected boolean canReadData0() throws IOException {
		return in.available() > 0;
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		switch (cfgType.getEnum()) {
		case Ascii:
			return Data.createFromAscii(in);
		case Binary:
			return Data.createFromBinary(in);
		default:
			throw new InternalException(cfgType.getEnum());
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
