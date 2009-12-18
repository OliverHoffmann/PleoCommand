package pleocmd.exc;

import pleocmd.pipe.StateHandling;

public class StateException extends PipeException {

	private static final long serialVersionUID = 4994472375815624929L;

	/**
	 * @see PipeException#PipeException(PipePart, boolean, String, Object...)
	 */
	public StateException(final StateHandling sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * @see PipeException#PipeException(PipePart, boolean, Throwable, String,
	 *      Object...)
	 */
	public StateException(final StateHandling sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
