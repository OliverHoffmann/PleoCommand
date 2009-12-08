package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public final class BinaryDataValue extends Value {

	private byte[] val;

	protected BinaryDataValue(final ValueType type) {
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
		return Arrays.toString(val);
	}

	@Override
	public byte[] asByteArray() {
		return val;
	}

	public static boolean isValidChar(final byte b) {
		return true;
	}

}
