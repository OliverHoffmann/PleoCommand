package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import pleocmd.pipe.Data;

public final class BinaryValue extends Value {

	public static final char TYPE_CHAR = 'B';

	public static final ValueType RECOMMENDED_TYPE = ValueType.Data;

	private byte[] val;

	protected BinaryValue(final ValueType type) {
		super(type);
		assert type == ValueType.Data;
	}

	@Override
	public void readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case Data:
			final int len = in.readInt();
			val = new byte[len];
			in.readFully(val);
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case Data:
			out.writeInt(val.length);
			out.write(val);
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void readFromAscii(final byte[] in, final int len)
			throws IOException {
		val = new byte[len];
		System.arraycopy(in, 0, val, 0, len);
	}

	@Override
	public void writeToAscii(final DataOutput out) throws IOException {
		out.write(val);
	}

	@Override
	public String toString() {
		return Data.toHexString(val);
	}

	@Override
	public byte[] asByteArray() {
		return val;
	}

	@Override
	public boolean mustWriteAsciiAsHex() {
		return true;
	}

}