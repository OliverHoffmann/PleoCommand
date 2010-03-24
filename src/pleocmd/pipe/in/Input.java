package pleocmd.pipe.in;

import java.io.IOException;

import pleocmd.exc.InputException;
import pleocmd.exc.StateException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Input extends PipePart {

	@Override
	protected abstract void configure0() throws InputException, IOException;

	@Override
	protected abstract void init0() throws InputException, IOException;

	@Override
	protected abstract void close0() throws InputException, IOException;

	@Override
	public String getInputDescription() {
		return "";
	}

	public final Data readData() throws InputException {
		try {
			ensureInitialized();
			return readData0();
		} catch (final IOException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		} catch (final StateException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		}
	}

	protected abstract Data readData0() throws InputException, IOException;

}
