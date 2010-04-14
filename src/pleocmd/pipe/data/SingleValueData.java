package pleocmd.pipe.data;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.val.FloatValue;
import pleocmd.pipe.val.IntValue;
import pleocmd.pipe.val.StringValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public class SingleValueData extends Data {

	public static final String IDENT = "Single";

	private static List<Value> l;

	public SingleValueData(final double value, final long user,
			final Data parent, final byte priority, final long time) {
		super(l = new ArrayList<Value>(3), parent, priority, time, CTOR_DIRECT);
		Value val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(IDENT);
		l.add(val);
		val = Value.createForType(ValueType.Float64);
		((FloatValue) val).set(value);
		l.add(val);
		val = Value.createForType(ValueType.Int64);
		((IntValue) val).set(user);
		l.add(val);
	}

	public SingleValueData(final double value, final long user,
			final Data parent) {
		super(l = new ArrayList<Value>(3), parent, CTOR_DIRECT);
		Value val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(IDENT);
		l.add(val);
		val = Value.createForType(ValueType.Float64);
		((FloatValue) val).set(value);
		l.add(val);
		val = Value.createForType(ValueType.Int64);
		((IntValue) val).set(user);
		l.add(val);
	}

	public static boolean isSingleValueData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static double getValue(final Data data) {
		return data.get(1).asDouble();
	}

	public static long getUser(final Data data) {
		return data.get(2).asLong();
	}

	/**
	 * Creates a new {@link SingleValueData} with a new value - the user data
	 * will just be copied.
	 * 
	 * @param val
	 *            value for the new {@link SingleValueData}
	 * @param parent
	 *            the original {@link Data} which should be compatible to
	 *            {@link SingleValueData}, i.e. {@link #isSingleValueData(Data)}
	 *            should return true.
	 * @return new {@link SingleValueData}
	 */
	public static Data create(final double val, final Data parent) {
		return new SingleValueData(val, getUser(parent), parent);
	}

}
