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
	 * Constructs a new {@link OutputException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#close()} has been
	 *            called).
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public OutputException(final Output sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * Constructs a new {@link OutputException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#close()} has been
	 *            called).
	 * @param cause
	 *            another {@link Exception} which caused this one
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public OutputException(final Output sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
