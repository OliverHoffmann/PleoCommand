package pleocmd.pipe.in;

import java.io.IOException;

import pleocmd.exc.InputException;
import pleocmd.exc.StateException;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Input extends PipePart {

	private int threadReferenceCounter;

	@Override
	protected void configure1() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	protected void init0() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	protected void close0() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	public final String getInputDescription() {
		return "";
	}

	@Override
	public final boolean isConnectionAllowed0(final PipePart trg) {
		return true;
	}

	public final Data readData() throws InputException {
		try {
			ensureInitialized();
			final Data res = readData0();
			if (res != null) // got a valid data block
				getFeedback().incDataSentCount(1);
			return res;
		} catch (final IOException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		} catch (final StateException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		}
	}

	protected abstract Data readData0() throws InputException, IOException;

	/**
	 * Increment the thread-reference counter (number of input threads which
	 * will be using this Input).<br>
	 * Should only be used from {@link Pipe}.
	 * 
	 * @return new reference counter
	 */
	public int incThreadReferenceCounter() {
		return ++threadReferenceCounter;
	}

	/**
	 * Decrement the thread-reference counter (number of input threads which
	 * will be using this Input).<br>
	 * Should only be used from {@link Pipe}.
	 * 
	 * @return new reference counter
	 */
	public int decThreadReferenceCounter() {
		return ++threadReferenceCounter;
	}

}
