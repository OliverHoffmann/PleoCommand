package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.data.Data;

public final class PassThroughConverter extends Converter {

	public PassThroughConverter() {
		super(new Config());
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public boolean canHandleData(final Data data) {
		return "PMC".equals(data.getSafe(0).asString())
				|| "SC".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Data> convert0(final Data data) {
		final List<Data> res = new ArrayList<Data>(1);
		res.add(data);
		return res;
	}

}
