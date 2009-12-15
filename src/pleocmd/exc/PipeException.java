package pleocmd.exc;

import pleocmd.pipe.PipePart;

public class PipeException extends Exception {

	private static final long serialVersionUID = -1800715731102540337L;

	private final PipePart sender;

	private final boolean permanent;

	public PipeException(final PipePart sender, final boolean permanent,
			final String message, final Object... args) {
		super(String.format(message, args));
		this.sender = sender;
		this.permanent = permanent;
	}

	public PipeException(final PipePart sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(String.format(message, args), cause);
		this.sender = sender;
		this.permanent = permanent;
	}

	public final PipePart getSender() {
		return sender;
	}

	public final boolean isPermanent() {
		return permanent;
	}

}
