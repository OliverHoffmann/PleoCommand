package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;

import pleocmd.StandardInput;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.exc.InternalException;
import pleocmd.pipe.data.Data;

public final class ConsoleInput extends Input {

	private final ConfigEnum<ReadType> cfgType;

	public ConsoleInput() {
		addConfig(cfgType = new ConfigEnum<ReadType>(ReadType.Ascii));
		constructed();
	}

	public ConsoleInput(final ReadType type) {
		this();
		cfgType.setEnum(type);
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgType.getContent();
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (StandardInput.the().available() <= 0) return null;
		switch (cfgType.getEnum()) {
		case Ascii:
			try {
				return Data.createFromAscii(new DataInputStream(StandardInput
						.the()));
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from console");
			}
		case Binary:
			try {
				return Data.createFromBinary(new DataInputStream(StandardInput
						.the()));
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from console");
			}
		default:
			throw new InternalException(cfgType.getEnum());
		}
	}

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "Console Input";
		case Description:
			return "Reads Data blocks from the standard input";
		case Config1:
			return "'Ascii' if Data blocks are in Ascii format or\n"
					+ "   'Binary' if Data blocks should be treated as binary";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

}
