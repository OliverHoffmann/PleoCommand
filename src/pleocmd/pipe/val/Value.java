package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Value {

	private final ValueType type;

	protected Value(final ValueType type) {
		this.type = type;
	}

	public final ValueType getType() {
		return type;
	}

	public Long asLong() {
		return null;
	}

	public Double asDouble() {
		return null;
	}

	public String asString() {
		return null;
	}

	public byte[] asByteArray() {
		return null;
	}

	public static Value createForType(final ValueType type) {
		switch (type) {
		case Int8:
		case Int32:
		case Int64:
			return new IntValue(type);
		case Float32:
		case Float64:
			return new FloatValue(type);
		case UTFString:
		case NullTermString:
			return new StringValue(type);
		case Data:
			return new BinaryDataValue(type);
		default:
			return null;
		}
	}

	public abstract void readFromBinary(final DataInput in) throws IOException;

	public abstract void writeToBinary(final DataOutput out) throws IOException;

	public abstract void readFromAscii(final byte[] in, int len)
			throws IOException;

	public abstract void writeToAscii(DataOutput out) throws IOException;

	@Override
	public abstract String toString();

}
