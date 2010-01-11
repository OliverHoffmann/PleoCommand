package pleocmd.cfg;

public final class ConfigEnum<E extends Enum<E>> extends ConfigItem {

	private final Class<E> enumClass;

	/**
	 * Creates a new {@link ConfigEnum}.
	 * 
	 * @param enumClass
	 *            the class of the {@link Enum} which should be wrapped
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
		super(label, false, enumClass.getEnumConstants());
		this.enumClass = enumClass;
	}

	public E getEnum() {
		return enumClass.getEnumConstants()[0];
	}

	public void setEnum(final E e) {
		try {
			setContent(e.toString());
		} catch (final ConfigurationException exc) {
			throw new InternalError(String.format(
					"Name of enum not recognized !? ('%s')", exc));
		}
	}
}
