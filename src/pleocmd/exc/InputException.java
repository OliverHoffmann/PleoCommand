package pleocmd.exc;

import pleocmd.pipe.in.Input;

/**
 * Subclass of {@link PipeException} which handles Exceptions thrown by
 * {@link Input} and sub classes.
 * 
 * @author oliver
 */
public final class InputException extends PipeException {

	private static final long serialVersionUID = -5901713172300885500L;

	/**
	 * @see PipeException#PipeException(PipePart, boolean, String, Object...)
	 */
	public InputException(final Input sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * @see PipeException#PipeException(PipePart, boolean, Throwable, String,
	 *      Object...)
	 */
	public InputException(final Input sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
