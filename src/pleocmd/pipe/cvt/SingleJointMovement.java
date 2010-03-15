package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.exc.InternalException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class SingleJointMovement extends Converter {

	enum OutOfRangeBehavior {
		CutOff, FitToRange
	}

	enum Transformation {
		Linear
	}

	private static final int ANGLE_UNDEFINED = 1000000;

	private final ConfigInt cfgChannelNumber;
	private final ConfigInt cfgJointNumber;
	private final ConfigInt cfgMinAngleMovement;

	private int currentAngle;

	public SingleJointMovement() {
		addConfig(cfgChannelNumber = new ConfigInt("Channel-Number", 0, 0, 31));
		addConfig(cfgJointNumber = new ConfigInt("Joint-Number", 9, 0, 13));
		addConfig(cfgMinAngleMovement = new ConfigInt("Minimal Angle-Movement",
				3, 0, 50));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() {
		currentAngle = ANGLE_UNDEFINED;
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public boolean canHandleData(final Data data) {
		return "BCIChannel".equals(data.getSafe(0).asString())
				&& data.getSafe(1).asLong() == cfgChannelNumber.getContent()
				&& (data.getSafe(3).asLong() & BCICodomainAdaption.FEATURE_CODOMAIN_ADAPTED) != 0;
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		final List<Data> res = new ArrayList<Data>(1);
		final List<Value> vals = new ArrayList<Value>(2);
		try {
			final int val = (int) data.get(2).asDouble();
			if (Math.abs(currentAngle - val) < cfgMinAngleMovement.getContent())
				return res; // ignore small movements
			currentAngle = val;
			vals.add(Value.createForType(ValueType.NullTermString).set("PMC"));
			vals.add(Value.createForType(ValueType.NullTermString).set(
					String.format("JOINT MOVE %d %d", cfgJointNumber
							.getContent(), val)));
		} catch (final IOException e) {
			throw new InternalException(e);
		}
		res.add(new Data(vals, data));
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Single Joint Movement";
		case Description:
			return "Produces a JOINT MOVE command for the Pleo for one joint and"
					+ "a codomain-adapted value of the given BCI channel";
		case Configuration:
			return "1: Number of codomain-adapted BCI channel\n"
					+ "2: Number of Pleo joint (motor) to move\n"
					+ "3: Minimal angle - movements below this will be ignored";
		default:
			return "???";
		}
	}

}
