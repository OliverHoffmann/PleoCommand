package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;

import pleocmd.StandardInput;
import pleocmd.exc.InputException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.cfg.ConfigEnum;
import pleocmd.pipe.data.Data;

public final class ConsoleInput extends Input {

	private ReadType type;

	public ConsoleInput() {
		super(new Config().addV(new ConfigEnum("ReadType", ReadType.values())));
	}

	@Override
	protected void configure0() {
		type = ReadType.values()[((ConfigEnum) getConfig().get(0)).getContent()];
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
	protected boolean canReadData0() throws IOException {
		return StandardInput.the().available() > 0;
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		switch (type) {
		case Ascii:
			return Data
					.createFromAscii(new DataInputStream(StandardInput.the()));
		case Binary:
			return Data.createFromBinary(new DataInputStream(StandardInput
					.the()));
		default:
			throw new InternalError(String
					.format("Invalid read-type: %s", type));
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Console Input";
		case Description:
			return "Reads Data blocks from the standard input";
		case Configuration:
			return "1: 'Ascii' if Data blocks are in Ascii format or\n"
					+ "   'Binary' if Data blocks should be treated as binary";
		default:
			return "???";
		}
	}

}
