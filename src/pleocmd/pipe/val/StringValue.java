package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class StringValue extends Value {

	private String val;

	protected StringValue(final ValueType type) {
		super(type);
		assert type == ValueType.UTFString || type == ValueType.NullTermString;
	}

	@Override
	public void readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case UTFString:
			val = in.readUTF();
			break;
		case NullTermString:
			int cap = 32;
			int len = 0;
			byte[] buf = new byte[cap];
			while (true) {
				final byte b = in.readByte();
				if (b == 0) break;
				if (cap == len) {
					cap *= 2;
					final byte[] buf2 = new byte[cap];
					System.arraycopy(buf, 0, buf2, 0, len);
					buf = buf2;
				}
				buf[len++] = b;
			}
			val = new String(buf, 0, len, "ISO-8859-1");
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case UTFString:
			out.writeUTF(val);
			break;
		case NullTermString:
			out.write(val.getBytes("ISO-8859-1"));
			out.write((byte) 0);
			break;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void readFromAscii(final byte[] in, final int len)
			throws IOException {
		val = new String(in, 0, len, "ISO-8859-1");
	}

	@Override
	public void writeToAscii(final DataOutput out) throws IOException {
		out.write(val.getBytes("ISO-8859-1"));
	}

	@Override
	public String toString() {
		return '\"' + val + '\"';
	}

	@Override
	public String asString() {
		return val;
	}

	@Override
	public byte[] asByteArray() {
		try {
			return val.getBytes("ISO-8859-1");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Internal error: Default character-set not supported!");
		}
	}

	public static boolean isValidChar(final byte b) {
		return b >= 0x20 && b <= 0x7E || b == 0x09;
	}

}
