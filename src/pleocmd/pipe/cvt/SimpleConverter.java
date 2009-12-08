package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.cmd.Command;
import pleocmd.pipe.cmd.PleoMonitorCommand;

public final class SimpleConverter extends Converter {

	public SimpleConverter() {
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
		return "DO".equals(data.getSafe(0).asString())
				|| "do".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Command> convertToCommand0(final Data data) {
		final List<Command> res = new ArrayList<Command>(1);
		String s = data.getSafe(1).asString();
		if (s == null) return res;
		s = s.toUpperCase();
		if (s.equals("HEAD-NOD"))
			res.add(new PleoMonitorCommand(data, "JOINT RANGE 12 2 10 -10"));
		else if (s.equals("HEAD-SHAKE"))
			res.add(new PleoMonitorCommand(data, "JOINT RANGE 11 2 30 -30"));
		// TODO replace with a table (written from file)
		return res;
	}

}
