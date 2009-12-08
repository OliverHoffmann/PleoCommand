package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.cmd.Command;

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

	public final void writeCommand(final Command command)
			throws OutputException {
		switch (getState()) {
		case Initialized:
			try {
				writeCommand0(command);
			} catch (final IOException e) {
				throw new OutputException(this, false, "Cannot write command",
						e);
			}
			break;
		default:
			throw new OutputException(this, true, "Not initialized");
		}
	}

	protected abstract void writeCommand0(Command command)
			throws OutputException, IOException;

}
