package pleocmd.exc;

import pleocmd.pipe.cvt.Converter;

public final class ConverterException extends PipeException {

	private static final long serialVersionUID = -2310364749103278117L;

	public ConverterException(final Converter sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	public ConverterException(final Converter sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
