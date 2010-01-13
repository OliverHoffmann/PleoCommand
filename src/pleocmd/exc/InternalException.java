package pleocmd.exc;

public class InternalException extends RuntimeException {

	private static final long serialVersionUID = -5339939772785000962L;

	public InternalException(final Throwable cause) {
		super("Caught an exception which should never occur", cause);
	}

	public InternalException(final Enum<?> unexpectedEnum) {
		this("Encountered an unexpected enum: '%s' of type '%s'",
				unexpectedEnum, unexpectedEnum == null ? null : unexpectedEnum
						.getDeclaringClass().getSimpleName());
	}

	public InternalException(final Throwable cause, final String msg,
			final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args), cause);
	}

	public InternalException(final String msg, final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args));
	}

}
