package pleocmd.exc;

import pleocmd.pipe.cvt.Converter;

/**
 * Subclass of {@link PipeException} which handles Exceptions thrown by
 * {@link Converter} and sub classes.
 * 
 * @author oliver
 */
public final class ConverterException extends PipeException {

	private static final long serialVersionUID = -2310364749103278117L;

	/**
	 * @see PipeException#PipeException(PipePart, boolean, String, Object...)
	 */
	public ConverterException(final Converter sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * @see PipeException#PipeException(PipePart, boolean, Throwable, String,
	 *      Object...)
	 */
	public ConverterException(final Converter sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
