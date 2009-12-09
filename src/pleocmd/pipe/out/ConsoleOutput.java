package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigEnum;
import pleocmd.pipe.cmd.Command;

public final class ConsoleOutput extends Output {

	private PrintType type;

	public ConsoleOutput() {
		super(new Config()
				.addV(new ConfigEnum("PrintType", PrintType.values())));
	}

	@Override
	protected void configured0() {
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
	protected void writeCommand0(final Command command) throws OutputException,
			IOException {

		switch (type) {
		case DataBinary: {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			command.getData().writeToAscii(new DataOutputStream(out));
			Log.consoleOut(new String(out.toByteArray(), "ISO-8859-1"));
			break;
		}
		case DataAscii: {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			command.getData().writeToAscii(new DataOutputStream(out));
			Log.consoleOut(new String(out.toByteArray(), "ISO-8859-1"));
			break;
		}
		case DataHumanReadable:
			Log.consoleOut(command.getData().toString());
			break;
		case Command:
			Log.consoleOut(command.asPleoMonitorCommand().toString());
			break;
		case CommandHumanReadable:
			Log.consoleOut(command.toString());
			break;
		default:
			throw new OutputException(this, true,
					"Internal error: Invalid print-type");
		}
	}

}
