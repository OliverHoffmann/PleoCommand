package pleocmd.pipe.in;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import pleocmd.Log;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.itfc.gui.DataSequenceEditorPanel;
import pleocmd.itfc.gui.Layouter;
import pleocmd.pipe.data.Data;

public final class StaticInput extends Input {

	private final ConfigString cfgInput;

	private DataInputStream in;

	public StaticInput() {
		addConfig(cfgInput = new ConfigString("Input", true) {

			private DataSequenceEditorPanel dse;

			@Override
			public boolean insertGUIComponents(final Layouter lay) {
				dse = new DataSequenceEditorPanel();
				try {
					dse.updateTextPaneFromReader(new BufferedReader(
							new StringReader(getContent())));
				} catch (final IOException e) {
					Log.error(e);
				}
				lay.addWholeLine(dse, true);
				return true;
			}

			@Override
			public void setFromGUIComponents() {
				final StringWriter out = new StringWriter();
				try {
					dse.writeTextPaneToWriter(out);
					setContent(out.toString());
				} catch (final IOException e) {
					Log.error(e, "Cannot set value '%s'", getLabel());
				} catch (final ConfigurationException e) {
					Log.error(e, "Cannot set value '%s'", getLabel());
				}
			}

		});
		constructed();
	}

	public StaticInput(final String staticData) throws ConfigurationException {
		this();
		cfgInput.setContent(staticData);
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
	protected String getShortConfigDescr0() {
		final int count = cfgInput.getContentList().size();
		return String.format("%d Data block%s", count, count == 1 ? "" : "s");
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
		case Config1:
			return "A new-line delimited list of Data blocks in Ascii form";
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
