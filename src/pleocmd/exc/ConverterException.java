package pleocmd.exc;

import pleocmd.pipe.cvt.Converter;

public final class ConverterException extends PipeException {

	private static final long serialVersionUID = -2310364749103278117L;

	public ConverterException(final Converter sender, final boolean permanent,
			final String message) {
		super(sender, permanent, message);
	}

}
