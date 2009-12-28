package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.data.Data;

public final class PassThroughConverter extends Converter {

	public PassThroughConverter() {
		constructed();
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

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Pass-Through Converter";
		case Description:
			return "Just passes through any commands of type 'PMC' and 'SC' "
					+ "without processing them any further";
		case Configuration:
			return "";
		default:
			return "???";
		}
	}

}
