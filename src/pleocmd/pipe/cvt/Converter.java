package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.exc.StateException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Converter extends PipePart {

	@Override
	protected abstract void configure0() throws ConverterException, IOException;

	@Override
	protected abstract void init0() throws ConverterException, IOException;

	@Override
	protected abstract void close0() throws ConverterException, IOException;

	@Override
	public boolean isConnectionAllowed0(final PipePart trg) {
		return true;
	}

	public final List<Data> convert(final Data data) throws ConverterException {
		try {
			ensureInitialized();
			return convert0(data);
		} catch (final IOException e) {
			throw new ConverterException(this, false, e,
					"Cannot convert data block");
		} catch (final StateException e) {
			throw new ConverterException(this, true, e,
					"Cannot convert data block");
		}
	}

	protected abstract List<Data> convert0(final Data data)
			throws ConverterException, IOException;

}
