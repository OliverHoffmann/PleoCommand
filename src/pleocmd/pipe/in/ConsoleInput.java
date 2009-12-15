package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;

import pleocmd.StandardInput;
import pleocmd.exc.InputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigEnum;
import pleocmd.pipe.Data;

public final class ConsoleInput extends Input {

	private ReadType type;

	public ConsoleInput() {
		super(new Config().addV(new ConfigEnum("ReadType", ReadType.values())));
	}

	@Override
	protected void configured0() {
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
			throw new InputException(this, true,
					"Internal error: Invalid read-type: %s", type);
		}
	}

}
