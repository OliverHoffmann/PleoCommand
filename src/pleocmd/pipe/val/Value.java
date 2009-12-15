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
			return new BinaryValue(type);
		default:
			return null;
		}
	}

	public static ValueType detectFromTypeChar(final char c) throws IOException {
		if (c == IntValue.TYPE_CHAR) return IntValue.RECOMMENDED_TYPE;
		if (c == FloatValue.TYPE_CHAR) return FloatValue.RECOMMENDED_TYPE;
		if (c == StringValue.TYPE_CHAR) return StringValue.RECOMMENDED_TYPE;
		if (c == BinaryValue.TYPE_CHAR) return BinaryValue.RECOMMENDED_TYPE;
		throw new IOException(String.format("Invalid type identifier: 0x%02X",
				c));
	}

	public static int getAsciiTypeChar(final Value value) {
		try {
			return (Character) value.getClass().getDeclaredField("TYPE_CHAR")
					.get(null);
		} catch (final Throwable t) {
			throw new RuntimeException(
					"Internal error: Cannot access field TYPE_CHAR "
							+ "of a subclass of Value", t);
		}
	}

	public abstract void readFromBinary(final DataInput in) throws IOException;

	public abstract void writeToBinary(final DataOutput out) throws IOException;

	public abstract void readFromAscii(final byte[] in, int len)
			throws IOException;

	public abstract void writeToAscii(DataOutput out) throws IOException;

	@Override
	public abstract String toString();

	public abstract boolean mustWriteAsciiAsHex();

}
