package pleocmd.pipe.out;

import java.io.DataOutputStream;
import java.io.IOException;

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
		case DataBinary:
			command.getData().writeToBinary(new DataOutputStream(System.out));
			break;
		case DataAscii:
			command.getData().writeToAscii(new DataOutputStream(System.out));
			break;
		case DataHumanReadable:
			System.out.println(command.getData());
			break;
		case Command:
			System.out.println(command.asPleoMonitorCommand());
			break;
		case CommandHumanReadable:
			System.out.println(command);
			break;
		default:
			throw new OutputException(this, true,
					"Internal error: Invalid print-type");
		}
	}

}
