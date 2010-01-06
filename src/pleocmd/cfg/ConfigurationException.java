package pleocmd.cfg;


public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 1287713973334997815L;

	public ConfigurationException(final Throwable cause, final String msg,
			final Object... args) {
		super(String.format(msg, args), cause);
	}

	public ConfigurationException(final int nr, final String line,
			final String msg, final Object... args) {
		super(String.format("At line %d - '%s': " + msg, nr, line, args));
	}

	public ConfigurationException(final String msg, final Object... args) {
		super(String.format(msg, args));
	}

}
