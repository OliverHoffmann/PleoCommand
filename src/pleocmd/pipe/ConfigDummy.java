package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;

public final class ConfigDummy extends ConfigValue {

	public ConfigDummy() {
		super("");
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		throw new IllegalStateException("Dummy Configuration");
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
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
