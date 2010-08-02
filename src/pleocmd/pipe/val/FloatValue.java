package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class FloatValue extends Value {

	static final char TYPE_CHAR = 'F';

	static final ValueType RECOMMENDED_TYPE = ValueType.Float64;

	private double val;

	protected FloatValue(final ValueType type) {
		super(type);
		assert type == ValueType.Int8 || type == ValueType.Int32
				|| type == ValueType.Int64 || type == ValueType.Float32
				|| type == ValueType.Float64;
	}

	@Override
	int readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case Int8:
			val = in.readByte();
			return 1;
		case Int32:
			val = in.readInt();
			return 4;
		case Int64:
			val = in.readLong();
			return 8;
		case Float32:
			val = in.readFloat();
			return 4;
		case Float64:
			val = in.readDouble();
			return 8;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	int writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case Int8:
			out.writeByte((int) val);
			return 1;
		case Int32:
			out.writeInt((int) val);
			return 4;
		case Int64:
			out.writeLong((long) val);
			return 8;
		case Float32:
			out.writeFloat((float) val);
			return 4;
		case Float64:
			out.writeDouble(val);
			return 8;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	void readFromAscii(final byte[] in, final int len) throws IOException {
		val = Double.valueOf(new String(in, 0, len, "US-ASCII"));
	}

	@Override
	int writeToAscii(final DataOutput out) throws IOException {
		final byte[] ba = String.valueOf(val).getBytes("US-ASCII");
		out.write(ba);
		return ba.length;
	}

	@Override
	public String toString() {
		return String.valueOf(val);
	}

	@Override
	public double asDouble() {
		return val;
	}

	@Override
	public String asString() {
		return String.valueOf(val);
	}

	@Override
	boolean mustWriteAsciiAsHex() {
		return false;
	}

	@Override
	public Value set(final String content) {
		val = Double.valueOf(content);
		return this;
	}

	public Value set(final double content) {
		val = content;
		return this;
	}

	@Override
	public void compact() {
		if (val >= Float.MIN_VALUE && val <= Float.MAX_VALUE
				&& Math.abs(val - (float) val) < Double.MIN_NORMAL)
			changeType(ValueType.Float32);
		else
			changeType(ValueType.Float64);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof FloatValue)) return false;
		return val == ((FloatValue) o).val;
	}

	@Override
	public int hashCode() {
		final long lb = Double.doubleToLongBits(val);
		return (int) (lb ^ lb >>> 32);
	}

}
