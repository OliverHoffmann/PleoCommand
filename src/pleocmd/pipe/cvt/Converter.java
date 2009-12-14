package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.PipePart;

/**
 * @author oliver
 */
public abstract class Converter extends PipePart {

	public Converter(final Config config) {
		super(config);
	}

	@Override
	protected abstract void configured0() throws ConverterException,
			IOException;

	@Override
	protected abstract void init0() throws ConverterException, IOException;

	@Override
	protected abstract void close0() throws ConverterException, IOException;

	public abstract boolean canHandleData(final Data data)
			throws ConverterException;

	public final List<Data> convert(final Data data)
			throws ConverterException {
		switch (getState()) {
		case Initialized:
			return convert0(data);
		default:
			throw new ConverterException(this, true, "Not initialized");
		}
	}

	protected abstract List<Data> convert0(final Data data)
			throws ConverterException;

}
