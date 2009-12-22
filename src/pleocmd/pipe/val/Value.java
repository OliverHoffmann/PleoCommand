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

	// CS_IGNORE_BEGIN This 4 methods need to be overridable

	public Long asLong() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a long integer value");
	}

	public Double asDouble() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a double floating point value");
	}

	public String asString() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a string value");
	}

	public byte[] asByteArray() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a byte array value");
	}

	// CS_IGNORE_END

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

	public static ValueType detectFromTypeChar(final char c, final int index)
			throws IOException {
		if (c == IntValue.TYPE_CHAR) return IntValue.RECOMMENDED_TYPE;
		if (c == FloatValue.TYPE_CHAR) return FloatValue.RECOMMENDED_TYPE;
		if (c == StringValue.TYPE_CHAR) return StringValue.RECOMMENDED_TYPE;
		if (c == BinaryValue.TYPE_CHAR) return BinaryValue.RECOMMENDED_TYPE;
		throw new IOException(String.format(
				"Invalid type identifier: 0x%02X at position %d", c, index));
	}

	public static int getAsciiTypeChar(final Value value) {
		try {
			return (Character) value.getClass().getDeclaredField("TYPE_CHAR")
					.get(null);
		} catch (final Throwable t) {
			// CS_IGNORE_PREV Catch everything that may go wrong here
			throw new InternalError(String.format(
					"Cannot access field TYPE_CHAR "
							+ "of a subclass of Value: %s", t));
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
