package pleocmd.exc;

import pleocmd.pipe.out.Output;

public final class OutputException extends PipeException {

	private static final long serialVersionUID = -4203759062995082909L;

	public OutputException(final Output sender, final boolean permanent,
			final String message) {
		super(sender, permanent, message);
	}

	public OutputException(final Output sender, final boolean permanent,
			final String message, final Throwable cause) {
		super(sender, permanent, message, cause);
	}

}
