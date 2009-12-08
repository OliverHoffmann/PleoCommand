package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class FloatValue extends Value {

	private double val;

	protected FloatValue(final ValueType type) {
		super(type);
		assert type == ValueType.Int8 || type == ValueType.Int32
				|| type == ValueType.Int64 || type == ValueType.Float32
				|| type == ValueType.Float64;
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
		case Float32:
			val = in.readFloat();
			break;
		case Float64:
			val = in.readDouble();
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
			out.writeLong((long) val);
			break;
		case Float32:
			out.writeFloat((float) val);
			break;
		case Float64:
			out.writeDouble(val);
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void readFromAscii(final byte[] in, final int len)
			throws IOException {
		val = Double.valueOf(new String(in, 0, len, "US-ASCII"));
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
	public Double asDouble() {
		return val;
	}

	@Override
	public String asString() {
		return String.valueOf(val);
	}

	public static boolean isValidChar(final byte b) {
		return IntValue.isValidChar(b) || b == '.' || b == 'E' || b == 'e';
	}

}
