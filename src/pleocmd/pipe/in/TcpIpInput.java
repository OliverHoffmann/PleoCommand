package pleocmd.pipe.in;

import pleocmd.pipe.data.Data;

public final class TcpIpInput extends Input {

	public TcpIpInput() {
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
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

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "TCP/IP Input";
		case Description:
			return "TODO";
		case Configuration:
			return "TODO";
		default:
			return "???";
		}
	}

}
