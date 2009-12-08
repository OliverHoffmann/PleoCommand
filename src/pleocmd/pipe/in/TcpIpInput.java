package pleocmd.pipe.in;

import pleocmd.pipe.Config;
import pleocmd.pipe.Data;

public final class TcpIpInput extends Input {

	public TcpIpInput() {
		super(new Config());
	}

	@Override
	protected void configured0() {
	}

	@Override
	protected void init0() {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	protected boolean canReadData0() {
		return false;
	}

	@Override
	protected Data readData0() {
		return null;
	}

}
