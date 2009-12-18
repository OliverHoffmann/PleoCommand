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
	 * Constructs a new {@link ConverterException}.
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
	public ConverterException(final Converter sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * Constructs a new {@link ConverterException}.
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
	public ConverterException(final Converter sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
