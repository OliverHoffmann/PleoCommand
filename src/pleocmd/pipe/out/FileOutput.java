package pleocmd.pipe.out;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigEnum;
import pleocmd.pipe.ConfigPath;
import pleocmd.pipe.cmd.Command;

public final class FileOutput extends Output {

	private File file;

	private PrintType type;

	private DataOutputStream out;

	public FileOutput() {
		super(new Config().addV(
				new ConfigPath("File", ConfigPath.PathType.FileForWriting))
				.addV(new ConfigEnum("PrintType", PrintType.values())));
	}

	@Override
	protected void configured0() {
		file = new File(((ConfigPath) getConfig().get(0)).getContent());
		type = PrintType.values()[((ConfigEnum) getConfig().get(1))
				.getContent()];
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file " + file + " for output of type " + type);
		out = new DataOutputStream(new FileOutputStream(file));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file " + file);
		out.close();
		out = null;
	}

	@Override
	protected void writeCommand0(final Command command) throws OutputException,
			IOException {
		switch (type) {
		case DataBinary:
			command.getData().writeToBinary(out);
			break;
		case DataAscii:
			command.getData().writeToAscii(out);
			break;
		case DataHumanReadable:
			out.write(command.getData().toString().getBytes());
			break;
		case Command:
			out.write(command.asPleoMonitorCommand().getBytes());
			break;
		case CommandHumanReadable:
			out.write(command.toString().getBytes());
			break;
		default:
			throw new OutputException(this, true,
					"Internal error: Invalid print-type");
		}
	}

}
