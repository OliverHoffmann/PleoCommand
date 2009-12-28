package pleocmd.pipe.cfg;

public final class ConfigEnum<E extends Enum<E>> extends ConfigList {

	private final Class<E> enumClass;

	/**
	 * Creates a new {@link ConfigEnum}.
	 */
	public ConfigEnum(final Class<E> enumClass) {
		super(enumClass.toString(), false, enumClass.getEnumConstants());
		this.enumClass = enumClass;
	}

	public E getEnum() {
		return enumClass.getEnumConstants()[0];
	}

	public void setEnum(final E e) {
		setContent(e.toString());
	}

}
