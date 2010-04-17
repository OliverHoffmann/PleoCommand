package pleocmd.pipe.data;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.val.StringValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public class MultiValueData extends Data {

	private static List<Value> l;

	protected MultiValueData(final String ident, final Value[] values,
			final Data parent, final byte priority, final long time) {
		super(l = new ArrayList<Value>(1 + values.length), parent, priority,
				time, CTOR_DIRECT);
		init(ident, values);
	}

	protected MultiValueData(final String ident, final Value[] values,
			final Data parent) {
		super(l = new ArrayList<Value>(1 + values.length), parent, CTOR_DIRECT);
		init(ident, values);
	}

	private static void init(final String ident, final Value[] values) {
		final Value valIdent = Value.createForType(ValueType.NullTermString);
		((StringValue) valIdent).set(ident);
		l.add(valIdent);
		for (final Value v : values)
			l.add(v);
	}

	public static int getValueCount(final Data data) {
		return data.size() - 1;
	}

	public static Value getValueRaw(final Data data, final int index) {
		return data.get(index + 1);
	}

}
