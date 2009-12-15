package pleocmd.exc;

import pleocmd.pipe.in.Input;

public final class InputException extends PipeException {

	private static final long serialVersionUID = -5901713172300885500L;

	public InputException(final Input sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	public InputException(final Input sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
