package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.PipePart;

/**
 * @author oliver
 */
public abstract class Output extends PipePart {

	public Output(final Config config) {
		super(config);
	}

	@Override
	protected abstract void configured0() throws OutputException, IOException;

	@Override
	protected abstract void init0() throws OutputException, IOException;

	@Override
	protected abstract void close0() throws OutputException, IOException;

	public final void write(final Data data) throws OutputException {
		switch (getState()) {
		case Initialized:
			try {
				write0(data);
			} catch (final IOException e) {
				throw new OutputException(this, false,
						"Cannot write data block", e);
			}
			break;
		default:
			throw new OutputException(this, true, "Not initialized");
		}
	}

	protected abstract void write0(Data data) throws OutputException,
			IOException;

}
