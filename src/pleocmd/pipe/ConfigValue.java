package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;

public abstract class ConfigValue {

	private final String label;

	public ConfigValue(final String label) {
		this.label = label;
	}

	public final String getLabel() {
		return label;
	}

	public abstract String getContentAsString();

	public abstract void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc);

	public abstract void setFromGUIComponents(final Container cntr);

	protected abstract void setFromString(String content) throws IOException;

	@Override
	public final String toString() {
		final String str = getContentAsString();
		return str == null ? getLabel() : getLabel() + ": "
				+ getContentAsString();
	}

}
