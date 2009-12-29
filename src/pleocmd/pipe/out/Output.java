package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.exc.OutputException;
import pleocmd.exc.StateException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Output extends PipePart {

	@Override
	protected abstract void configure0() throws OutputException, IOException;

	@Override
	protected abstract void init0() throws OutputException, IOException;

	@Override
	protected abstract void close0() throws OutputException, IOException;

	public final boolean write(final Data data) throws OutputException {
		try {
			ensureInitialized();
			return write0(data);
		} catch (final IOException e) {
			throw new OutputException(this, false, e,
					"Cannot write data block '%s'", data);
		} catch (final StateException e) {
			throw new OutputException(this, true, e,
					"Cannot write data block '%s'", data);
		}
	}

	protected abstract boolean write0(Data data) throws OutputException,
			IOException;

}
