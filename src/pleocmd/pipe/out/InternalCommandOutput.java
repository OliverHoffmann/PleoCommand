package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.exc.OutputException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.data.Data;

public final class InternalCommandOutput extends Output {

	public InternalCommandOutput() {
		super(new Config());
	}

	@Override
	protected void configure0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void init0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void close0() throws OutputException, IOException {
		// nothing to do
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException,
			InterruptedException {
		if ("SC".equals(data.getSafe(0).asString())) {
			final String v2 = data.get(1).asString();
			if ("SLEEP".equals(v2)) Thread.sleep(data.getSafe(2).asLong());
		}
	}
}
