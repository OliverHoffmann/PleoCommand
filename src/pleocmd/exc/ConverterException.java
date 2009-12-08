package pleocmd.exc;

import pleocmd.pipe.cvt.Converter;

public final class ConverterException extends PipeException {

	public ConverterException(final Converter sender, final boolean permanent,
			final String message) {
		super(sender, permanent, message);
	}

}
