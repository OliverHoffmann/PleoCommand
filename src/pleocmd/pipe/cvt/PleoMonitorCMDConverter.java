package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.cmd.Command;
import pleocmd.pipe.cmd.PleoMonitorCommand;

public final class PleoMonitorCMDConverter extends Converter {

	public PleoMonitorCMDConverter() {
		super(new Config());
	}

	@Override
	protected void configured0() {
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
	public boolean canHandleData(final Data data) {
		return "PMC".equals(data.getSafe(0).asString())
				|| "pmc".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Command> convertToCommand0(final Data data) {
		final List<Command> res = new ArrayList<Command>(1);
		res.add(new PleoMonitorCommand(data, data.getSafe(1).asString()));
		return res;
	}

}
