package pleocmd.pipe.in;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import pleocmd.cfg.ConfigString;
import pleocmd.exc.InputException;
import pleocmd.pipe.data.Data;

public final class StaticInput extends Input {

	private final ConfigString cfgInput;

	private DataInputStream in;

	public StaticInput() {
		addConfig(cfgInput = new ConfigString("Input", true));
		constructed();
	}

	public StaticInput(final String staticData) {
		this();
		cfgInput.setContent(staticData);
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		in = new DataInputStream(new ByteArrayInputStream(cfgInput.getContent()
				.getBytes("ISO-8859-1")));
	}

	@Override
	protected void close0() throws IOException {
		in.close();
		in = null;
	}

	@Override
	protected boolean canReadData0() throws IOException {
		return in.available() > 0;
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		return Data.createFromAscii(in);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Static Input";
		case Description:
			return "Returns Ascii Data blocks directly from configuration";
		case Configuration:
			return "1: A new-line delimited list of Data blocks in Ascii form";
		default:
			return "???";
		}
	}

}
