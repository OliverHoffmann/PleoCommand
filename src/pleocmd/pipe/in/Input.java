package pleocmd.pipe.in;

import java.io.IOException;

import pleocmd.exc.InputException;
import pleocmd.exc.PipeException;
import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.PipePart;

/**
 * @author oliver
 */
public abstract class Input extends PipePart {

	public Input(final Config config) {
		super(config);
	}

	@Override
	protected abstract void configured0() throws InputException, IOException;

	@Override
	protected abstract void init0() throws InputException, IOException;

	@Override
	protected abstract void close0() throws InputException, IOException;

	public final boolean canReadData() throws InputException {
		try {
			ensureInitialized();
			return canReadData0();
		} catch (final IOException e) {
			throw new InputException(this, false, e,
					"Cannot check for available data blocks");
		} catch (final PipeException e) {
			throw new InputException(this, true, e,
					"Cannot check for available data blocks");
		}
	}

	protected abstract boolean canReadData0() throws InputException,
			IOException;

	public final Data readData() throws InputException {
		try {
			ensureInitialized();
			return readData0();
		} catch (final IOException e) {
			throw new InputException(this, false, e, "Cannot read data block");
		} catch (final PipeException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		}
	}

	protected abstract Data readData0() throws InputException, IOException;

}
