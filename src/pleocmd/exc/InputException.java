package pleocmd.exc;

import pleocmd.pipe.in.Input;

public final class InputException extends PipeException {

	private static final long serialVersionUID = -5901713172300885500L;

	public InputException(final Input sender, final boolean permanent,
			final String message) {
		super(sender, permanent, message);
	}

	public InputException(final Input sender, final boolean permanent,
			final String message, final Throwable cause) {
		super(sender, permanent, message, cause);
	}

}
