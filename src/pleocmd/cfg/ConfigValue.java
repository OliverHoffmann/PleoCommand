package pleocmd.cfg;

import java.util.List;

import pleocmd.itfc.gui.Layouter;
import pleocmd.pipe.PipePart;

/**
 * Base class for a value inside a {@link PipePart}'s configuration.
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

	@Override
	public final String toString() {
		final String str = asString();
		return str == null ? getLabel() : getLabel() + ": " + str;
	}

	abstract String asString();

	abstract void setFromString(final String string)
			throws ConfigurationException;

	abstract List<String> asStrings();

	abstract void setFromStrings(final List<String> strings)
			throws ConfigurationException;

	abstract String getIdentifier();

	abstract boolean isSingleLined();

	public abstract void insertGUIComponents(final Layouter lay);

	public abstract void setFromGUIComponents();

}