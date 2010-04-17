package pleocmd.pipe.data;

import pleocmd.pipe.val.IntValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class SingleBoolData extends SingleValueData {

	public static final String IDENT = "bool";

	public SingleBoolData(final boolean value, final long user,
			final Data parent, final byte priority, final long time) {
		super(IDENT, asValue(value), user, parent, priority, time);
	}

	public SingleBoolData(final boolean value, final long user,
			final Data parent) {
		super(IDENT, asValue(value), user, parent);
	}

	private static Value asValue(final boolean value) {
		final Value val = Value.createForType(ValueType.Int8);
		((IntValue) val).set(value ? 1 : 0);
		return val;
	}

	public static boolean isSingleBoolData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static boolean getValue(final Data data) {
		return SingleValueData.getValueRaw(data).asLong() != 0;
	}

	public static long getUser(final Data data) {
		return SingleValueData.getUser(data);
	}

	/**
	 * Creates a new {@link SingleBoolData} with a new value - the user data
	 * will just be copied.
	 * 
	 * @param val
	 *            value for the new {@link SingleBoolData}
	 * @param parent
	 *            the original {@link Data} which should be compatible to
	 *            {@link SingleBoolData}, i.e. {@link #isSingleBoolData(Data)}
	 *            should return true.
	 * @return new {@link SingleBoolData}
	 */
	public static Data create(final boolean val, final Data parent) {
		return new SingleBoolData(val, getUser(parent), parent);
	}

}
