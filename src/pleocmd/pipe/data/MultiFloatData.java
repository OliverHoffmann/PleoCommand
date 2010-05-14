package pleocmd.pipe.data;

import pleocmd.pipe.val.FloatValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class MultiFloatData extends MultiValueData {

	public static final String IDENT = "float[ ]";

	public MultiFloatData(final double[] values, final Data parent,
			final byte priority, final long time) {
		super(IDENT, asValue(values), parent, priority, time);
	}

	public MultiFloatData(final double[] values, final Data parent) {
		super(IDENT, asValue(values), parent);
	}

	public MultiFloatData(final Data parent) {
		super(IDENT, parent.toArray(new Value[parent.size()]), parent);
	}

	private static Value[] asValue(final double[] values) {
		final Value[] res = new Value[values.length];
		for (int i = 0; i < values.length; ++i) {
			res[i] = Value.createForType(ValueType.Float64);
			((FloatValue) res[i]).set(values[i]);
		}
		return res;
	}

	public static boolean isMultiFloatData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static int getValueCount(final Data data) {
		return MultiValueData.getValueCount(data);
	}

	public static double getValue(final Data data, final int index) {
		return MultiValueData.getValueRaw(data, index).asDouble();
	}

}
