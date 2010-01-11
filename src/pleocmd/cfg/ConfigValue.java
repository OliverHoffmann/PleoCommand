package pleocmd.cfg;

import java.util.List;

import pleocmd.cfg.ConfigPath.PathType;
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

	public final void assign(final ConfigValue src)
			throws ConfigurationException {
		if (isSingleLined())
			setFromString(src.asString());
		else
			setFromStrings(src.asStrings());
	}

	public static ConfigValue createValue(final String identifier,
			final String label, final boolean singleLined) {
		if ("int".equals(identifier))
			return new ConfigInt(label, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

		if ("float".equals(identifier))
			return new ConfigFloat(label, .0, Double.MIN_VALUE,
					Double.MAX_VALUE);

		if ("dir".equals(identifier))
			return new ConfigPath(label, PathType.Directory);

		if ("read".equals(identifier))
			return new ConfigPath(label, PathType.FileForReading);

		if ("write".equals(identifier))
			return new ConfigPath(label, PathType.FileForWriting);

		if ("bounds".equals(identifier)) return new ConfigBounds(label);

		// "str", "item", null
		return new ConfigString(label, !singleLined);
	}

}
