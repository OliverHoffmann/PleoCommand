package pleocmd.pipe;

import java.io.IOException;

import pleocmd.itfc.gui.Layouter;

public final class ConfigDummy extends ConfigValue {

	public ConfigDummy() {
		super("");
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		throw new IllegalStateException("Dummy Configuration");
	}

	@Override
	public void setFromGUIComponents() {
		throw new IllegalStateException("Dummy Configuration");
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		throw new IllegalStateException("Dummy Configuration");
	}

	@Override
	public String getContentAsString() {
		return null;
	}

}
