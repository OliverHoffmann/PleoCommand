package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigEnum;
import pleocmd.pipe.Data;

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
				Log.consoleOut(data.getSafe(1).asString());
			break;
		default:
			throw new OutputException(this, true,
					"Internal error: Invalid print-type");
		}
	}

}
