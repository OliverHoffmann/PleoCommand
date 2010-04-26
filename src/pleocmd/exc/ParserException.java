package pleocmd.exc;

public class ParserException extends Exception {

	private static final long serialVersionUID = -6607865991043852010L;

	public ParserException(final Throwable cause, final String msg,
			final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args), cause);
	}

	public ParserException(final String msg, final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args));
	}

}
