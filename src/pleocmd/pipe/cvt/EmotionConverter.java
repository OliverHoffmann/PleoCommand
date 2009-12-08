package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.pipe.Config;
import pleocmd.pipe.Data;
import pleocmd.pipe.cmd.Command;

public final class EmotionConverter extends Converter {

	public EmotionConverter() {
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
		return false;
	}

	@Override
	protected List<Command> convertToCommand0(final Data data) {
		return null;
	}

}
