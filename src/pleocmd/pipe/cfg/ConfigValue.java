package pleocmd.pipe.cfg;

import java.io.IOException;

import pleocmd.itfc.gui.Layouter;

/**
 * Base class for a value inside a {@link Config}.
 * 
 * @author oliver
 */
public abstract class ConfigValue {

	private final String label;

	public ConfigValue(final String label) {
		this.label = label;
	}

	public final String getLabel() {
		return label;
	}

	public abstract String getContentAsString();

	public abstract void insertGUIComponents(final Layouter lay);

	public abstract void setFromGUIComponents();

	public abstract void setFromString(String content) throws IOException;

	@Override
	public final String toString() {
		final String str = getContentAsString();
		return str == null ? getLabel() : getLabel() + ": "
				+ getContentAsString();
	}

}
