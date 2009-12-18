package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class IntValue extends Value {

	public static final char TYPE_CHAR = 'I';

	public static final ValueType RECOMMENDED_TYPE = ValueType.Int64;

	private long val;

	protected IntValue(final ValueType type) {
		super(type);
		assert type == ValueType.Int8 || type == ValueType.Int32
				|| type == ValueType.Int64;
	}

	@Override
	public void readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case Int8:
			val = in.readByte();
			break;
		case Int32:
			val = in.readInt();
			break;
		case Int64:
			val = in.readLong();
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case Int8:
			out.writeByte((int) val);
			break;
		case Int32:
			out.writeInt((int) val);
			break;
		case Int64:
			out.writeLong(val);
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void readFromAscii(final byte[] in, final int len)
			throws IOException {
		val = Long.valueOf(new String(in, 0, len, "US-ASCII"));
		/*
		 * val = 0; int i = 0; boolean isNeg = false; if (!in.isEmpty() &&
		 * in.get(0) == '-') { isNeg = true; ++i; } if (i == in.size()) throw
		 * new IOException("Missing characters for decimal value"); for (; i <
		 * in.size(); ++i) { val *= 10; final byte b = in.get(i); if (b < '0' ||
		 * b > '9') throw new
		 * IOException("Invalid character for decimal value"); val += b - '0'; }
		 * if (isNeg) val = -val;
		 */
	}

	@Override
	public void writeToAscii(final DataOutput out) throws IOException {
		out.write(String.valueOf(val).getBytes("US-ASCII"));
	}

	@Override
	public String toString() {
		return String.valueOf(val);
	}

	@Override
	public Long asLong() {
		return val;
	}

	@Override
	public Double asDouble() {
		return Double.valueOf(val);
	}

	@Override
	public String asString() {
		return String.valueOf(val);
	}

	@Override
	public boolean mustWriteAsciiAsHex() {
		return false;
	}

}
