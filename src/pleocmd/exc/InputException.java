package pleocmd.exc;

import pleocmd.pipe.in.Input;

public final class InputException extends PipeException {

	public InputException(final Input sender, final boolean permanent,
			final String message) {
		super(sender, permanent, message);
	}

	public InputException(final Input sender, final boolean permanent,
			final String message, final Throwable cause) {
		super(sender, permanent, message, cause);
	}

}
