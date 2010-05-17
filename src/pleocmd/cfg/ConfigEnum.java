package pleocmd.cfg;

import java.util.Arrays;

import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

public final class ConfigEnum<E extends Enum<E>> extends ConfigItem<E> {

	private final Class<E> enumClass;

	/**
	 * Creates a new {@link ConfigEnum}.
	 * 
	 * @param enumClass
	 *            the class of the {@link Enum} which should be wrapped. Its
	 *            name defines the label of the {@link ConfigValue}
	 */
	public ConfigEnum(final Class<E> enumClass) {
		this(enumClass.getSimpleName(), enumClass);
	}

	/**
	 * Creates a new {@link ConfigEnum}.
	 * 
	 * @param label
	 *            name of this Config - used in GUI mode configuration and for
	 *            configuration files
	 * @param enumClass
	 *            the class of the {@link Enum} which should be wrapped
	 */
	public ConfigEnum(final String label, final Class<E> enumClass) {
		super(label, false, Arrays.asList(enumClass.getEnumConstants()));
		this.enumClass = enumClass;
	}

	/**
	 * Creates a new {@link ConfigEnum}.
	 * 
	 * @param content
	 *            The initial default value. It's declaring enum class defines
	 *            the label of the {@link ConfigValue}
	 */
	public ConfigEnum(final E content) {
		this(content.getDeclaringClass().getSimpleName(), content);
	}

	/**
	 * Creates a new {@link ConfigEnum}.
	 * 
	 * @param content
	 *            the initial default value
	 * @param label
	 *            name of this Config - used in GUI mode configuration and for
	 *            configuration files
	 */
	public ConfigEnum(final String label, final E content) {
		this(label, content.getDeclaringClass());
		setEnum(content);
	}

	public E getEnum() {
		try {
			return Enum.valueOf(enumClass, getContent());
		} catch (final IllegalArgumentException e) {
			// check if the content matches a enum's string representation
			final String cont = getContent();
			for (final E ec : enumClass.getEnumConstants())
				if (ec.toString().equals(cont)) return ec;
			throw e;
		}
	}

	public void setEnum(final E e) {
		try {
			setContent(e.toString());
		} catch (final ConfigurationException exc) {
			throw new InternalException(
					"Name of enum not recognized !? ('%s')", exc);
		}
	}

	public void setEnumGUI(final E e) {
		setContentGUI(e.toString());
	}

	public E getEnumGUI() {
		return Enum.valueOf(enumClass, getContentGUI());
	}

}
