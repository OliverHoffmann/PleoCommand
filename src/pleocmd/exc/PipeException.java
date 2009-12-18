package pleocmd.exc;


/**
 * This is the central {@link Exception} class for all classes in the
 * <b>pleocmd.pipe</b> package.
 * 
 * @author oliver
 */
public class PipeException extends Exception {

	private static final long serialVersionUID = -1800715731102540337L;

	private final Object sender;

	private final boolean permanent;

	/**
	 * Constructs a new {@link PipeException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#closeAll()} has
	 *            been called).
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public PipeException(final Object sender, final boolean permanent,
			final String message, final Object... args) {
		super(args.length == 0 ? message : String.format(message, args));
		this.sender = sender;
		this.permanent = permanent;
	}

	/**
	 * Constructs a new {@link PipeException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#closeAll()} has
	 *            been called).
	 * @param cause
	 *            another {@link Exception} which caused this one
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public PipeException(final Object sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(args.length == 0 ? message : String.format(message, args), cause);
		this.sender = sender;
		this.permanent = permanent;
	}

	/**
	 * The source instance which caused this exception or <b>null</b> if the
	 * source was not a subclass of {@link PipePart}.
	 * 
	 * @return source or <b>null</b>
	 */
	public final Object getSender() {
		return sender;
	}

	/**
	 * @return true if the cause for this {@link Exception} has a permanent
	 *         reason. The sender then will never be called again during pipe
	 *         processing (until {@link pleocmd.pipe.Pipe#closeAll()} has been
	 *         called).
	 */
	public final boolean isPermanent() {
		return permanent;
	}

}
