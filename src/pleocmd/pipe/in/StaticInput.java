package pleocmd.pipe.in;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.pipe.data.Data;

public final class StaticInput extends Input {

	private final ConfigString cfgInput;

	private DataInputStream in;

	public StaticInput() {
		addConfig(cfgInput = new ConfigString("Input", true));
		constructed();
	}

	public StaticInput(final String staticData) throws ConfigurationException {
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
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (in.available() <= 0) return null;
		try {
			return Data.createFromAscii(in);
		} catch (final FormatException e) {
			throw new InputException(this, false, e, "Cannot read static data");
		}
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

	@Override
	public boolean isConfigurationSane() {
		return true;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

}
