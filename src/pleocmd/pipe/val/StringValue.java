package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class StringValue extends Value {

	public static final char TYPE_CHAR = 'S';

	public static final ValueType RECOMMENDED_TYPE = ValueType.NullTermString;

	private static final int[] ASCII_TABLE = { // 
	/**//**/0, 0, 0, 0, 0, 0, 0, 0, // 00 - 07
			0, 0, 0, 0, 0, 0, 0, 0, // 08 - 0F
			0, 0, 0, 0, 0, 0, 0, 0, // 10 - 17
			0, 0, 0, 0, 0, 0, 0, 0, // 18 - 1F
			2, 1, 1, 1, 1, 1, 1, 1, // 20 - 27
			1, 1, 1, 1, 1, 1, 1, 1, // 28 - 2F
			1, 1, 1, 1, 1, 1, 1, 1, // 30 - 37
			1, 1, 3, 1, 1, 1, 1, 1, // 38 - 3F
			1, 1, 1, 1, 1, 1, 1, 1, // 40 - 47
			1, 1, 1, 1, 1, 1, 1, 1, // 48 - 4F
			1, 1, 1, 1, 1, 1, 1, 1, // 50 - 57
			1, 1, 1, 1, 1, 1, 1, 1, // 58 - 5F
			1, 1, 1, 1, 1, 1, 1, 1, // 60 - 67
			1, 1, 1, 1, 1, 1, 1, 1, // 68 - 6F
			1, 1, 1, 1, 1, 1, 1, 1, // 70 - 77
			1, 1, 1, 1, 0, 1, 1, 0, // 78 - 7F
	};

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
		return String.format("'%s'", val);
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

	@Override
	public boolean mustWriteAsciiAsHex() {
		for (int i = 0; i < val.length(); ++i)
			switch (ASCII_TABLE[val.charAt(i)]) {
			case 0:
				// the character is invalid =>
				// we must write the string in hexadecimal form
				return true;
			case 1:
				// this is a valid character
				break;
			case 2:
				// whitespace is disallowed only on the end of the string
				if (i == 0 || i == val.length() - 1) return true;
				break;
			case 3:
				// ':' is disallowed only when ambiguous (position 1 and 2)
				if (i == 1 || i == 2) return true;
				break;
			}
		// all characters are valid
		return false;
	}

}
