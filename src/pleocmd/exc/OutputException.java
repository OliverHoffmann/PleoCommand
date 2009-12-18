package pleocmd.exc;

import pleocmd.pipe.out.Output;

/**
 * Subclass of {@link PipeException} which handles Exceptions thrown by
 * {@link Output} and sub classes.
 * 
 * @author oliver
 */
public final class OutputException extends PipeException {

	private static final long serialVersionUID = -4203759062995082909L;

	/**
	 * @see PipeException#PipeException(PipePart, boolean, String, Object...)
	 */
	public OutputException(final Output sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * @see PipeException#PipeException(PipePart, boolean, Throwable, String,
	 *      Object...)
	 */
	public OutputException(final Output sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
