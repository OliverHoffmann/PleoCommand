package pleocmd.pipe;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import pleocmd.Log;
import pleocmd.pipe.val.BinaryDataValue;
import pleocmd.pipe.val.DummyValue;
import pleocmd.pipe.val.FloatValue;
import pleocmd.pipe.val.IntValue;
import pleocmd.pipe.val.StringValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class Data extends AbstractList<Value> {

	private static final byte[] HEX_TABLE = new byte[] { '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private final int flags;

	private final List<Value> values;

	private Data(final int flags, final List<Value> content) {
		Log.detail("New Data with " + content.size() + " values - flags: 0x"
				+ Integer.toHexString(flags));
		this.flags = flags;
		values = content;
	}

	@Override
	public Value get(final int index) {
		return values.get(index);
	}

	public Value getSafe(final int index) {
		return index < 0 || index >= values.size() ? new DummyValue() : values
				.get(index);
	}

	@Override
	public int size() {
		return values.size();
	}

	/**
	 * Reads binary data from stream. Data must have the following format:<br>
	 * <table>
	 * <tr>
	 * <th align=left>Description</th>
	 * <th align=left>Size</th>
	 * </tr>
	 * <tr>
	 * <td>Flags</td>
	 * <td>5 Bits</td>
	 * </tr>
	 * <tr>
	 * <td>Number of Fields (N-1)</td>
	 * <td>3 Bits</td>
	 * </tr>
	 * <tr>
	 * <td>Type of Field 1</td>
	 * <td>3 Bits</td>
	 * </tr>
	 * <tr>
	 * <td>...</td>
	 * </tr>
	 * <tr>
	 * <td>Type of Field 8</td>
	 * <td>3 Bits</td>
	 * </tr>
	 * <tr>
	 * <td>Content of Field 1</td>
	 * <td>Depends on Type</td>
	 * </tr>
	 * <tr>
	 * <td>...</td>
	 * </tr>
	 * <tr>
	 * <td>Content of Field N</td>
	 * <td>Depends on Type</td>
	 * </tr>
	 * </table>
	 * <table>
	 * <tr>
	 * <th align=left>Type Index</th>
	 * <th align=left>Description</th>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>8-Bit signed Integer</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>32-Bit signed Integer</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>64-Bit signed Integer</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>32-Bit Floating Point (Single)</td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td>64-Bit Floating Point (Double)</td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td>UTF-8 encoded String</td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td>Null-terminated (Ascii) String</td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td>4 Byte Length + arbitrary Data</td>
	 * </tr>
	 * </table>
	 * so it's at least 4 byte (for a block without any fields).
	 * 
	 * @param in
	 *            Input Stream with binary data
	 * @return Data read from stream.
	 */
	public static Data createFromBinary(final DataInput in) throws IOException {
		final int hdr = in.readInt();
		final int flags = hdr >> 27 & 0x1F;
		final int cnt = (hdr >> 24 & 0x07) + 1;
		Log.detail("Header is " + Integer.toHexString(hdr) + " => flags: 0x"
				+ Integer.toHexString(flags) + " cnt: " + cnt);
		final List<Value> content = new ArrayList<Value>(cnt);
		for (int i = 0; i < cnt; ++i) {
			final ValueType type = ValueType.values()[hdr >> i * 3 & 0x07];
			assert type.getID() == (hdr >> i * 3 & 0x07);
			final Value val = Value.createForType(type);
			if (val == null)
				throw new IOException(
						"Internal error: Type out of range 0 - 0x07");
			val.readFromBinary(in);
			content.add(val);
		}
		return new Data(flags, content);
	}

	/**
	 * Reads ASCII data from stream. Data must consist of fields (separated by
	 * '|') and followed by '\n'. Whitespaces are allowed and will be ignored.
	 * Fields may be prefixed by a type identifier followed by ':'. Allowed type
	 * identifiers are 'I', 'F', 'S' and 'B' for int, float, string or binary
	 * values, optionally followed by modifier 'x' for hexadecimal data.<br>
	 * Examples:<br>
	 * 25|7.3|Hello<br>
	 * I:3|F:2.0|S:Some String<br>
	 * S: 12345678 | 100 | F: 100 | Bx: F0DD35007E | Sx: 48454C4C4F<br>
	 * 
	 * @param in
	 *            Input Stream with text data
	 * @return Data read from stream.
	 */
	public static Data createFromAscii(final DataInput in) throws IOException {
		final List<Value> content = new ArrayList<Value>();
		int buflen = 0;
		int bufcap = 8;
		byte[] buf = new byte[bufcap];
		ValueType type = null;
		boolean isHex = false;
		while (true) {
			final byte b = in.readByte();
			switch (b) {
			case '\n':
			case '|':
				// trim whitespaces
				while (buflen > 0 && buf[buflen - 1] == ' ')
					--buflen;
				if (type == null) {
					// autodetect type and hex
					isHex = false;
					switch (detectDataType(buf, buflen)) {
					case 1:
						type = ValueType.Int64;
						break;
					case 2:
						type = ValueType.Float64;
						break;
					case 3:
						isHex = true;
						type = ValueType.Data;
						System.err.println("TODO!!!");
						// TODO
						break;
					case 4:
						type = ValueType.NullTermString;
						break;
					case 5:
						type = ValueType.Data;
						break;
					default:
						throw new RuntimeException(
								"Internal error: detectDataType()"
										+ " returned wrong value");
					}
				}
				// create fitting value
				final Value val = Value.createForType(type);
				if (val == null)
					throw new IOException("Internal error: Invalid value type");
				if (isHex) {
					Log.detail("Converting hex data with length " + buflen);
					if (buflen % 2 != 0)
						throw new IOException(
								"Internal error: Broken hexadecimal data");
					final byte[] buf2 = new byte[buflen / 2];
					for (int i = 0, j = 0; i < buflen;) {
						final int d1 = Character.digit(buf[i++], 16);
						final int d2 = Character.digit(buf[i++], 16);
						if (d1 == -1 || d2 == -1)
							throw new IOException(
									"Internal error: Broken hexadecimal data");
						buf2[j++] = (byte) (d1 << 4 | d2);
					}
					val.readFromAscii(buf2, buf2.length);
				} else
					val.readFromAscii(buf, buflen);
				content.add(val);

				// check if this was the end of the data block
				if (b == '\n') return new Data(0, content);

				// if not, prepare for the next value
				type = null;
				isHex = false;
				buflen = 0;
				break;
			case ':':
				if (type == null && (buflen == 1 || buflen == 2)) {
					switch (buf[0]) {
					case 'I':
						type = ValueType.Int64;
						break;
					case 'F':
						type = ValueType.Float64;
						break;
					case 'S':
						type = ValueType.NullTermString;
						break;
					case 'B':
						type = ValueType.Data;
						break;
					default:
						throw new IOException(String.format(
								"Invalid type identifier: 0x%02X", buf[0]));
					}
					if (buflen == 2) {
						if (buf[1] != 'x')
							throw new IOException(String.format(
									"Invalid type modifier: 0x%02X", buf[1]));
						isHex = true;
					} else
						isHex = false;
					buflen = 0;
					Log.detail("Forced type " + type + " hex: " + isHex);
					break;
				}
				// treat (second) ':' on other positions as a normal character
				//$FALL-THROUGH$
			default:
				if (b == ' ' && buflen == 0) // ignore this whitespace
					break;
				if (buflen == bufcap) {
					bufcap *= 2;
					final byte[] buf2 = new byte[bufcap];
					System.arraycopy(buf, 0, buf2, 0, buflen);
					buf = buf2;
				}
				buf[buflen++] = b;
			}
		}
	}

	/**
	 * @param data
	 * @param len
	 * @return 1 till 5 for Integer, Double, Hex, String or Binary
	 */
	private static int detectDataType(final byte[] data, final int len) {
		int res = 0;
		Log.detail("Autodetecting data type of " + len + " bytes");
		for (int i = 0; i < len; ++i) {
			final byte b = data[i];
			if (IntValue.isValidChar(b))
				res = Math.max(res, 1);
			else if (FloatValue.isValidChar(b))
				res = Math.max(res, 2);
			else if (len % 2 == 0
					&& (b >= '0' && b <= '9' || b >= 'A' && b <= 'F' || b >= 'a'
							&& b <= 'f'))
				res = Math.max(res, 3);
			else if (StringValue.isValidChar(b))
				res = Math.max(res, 4);
			else if (BinaryDataValue.isValidChar(b)) res = Math.max(res, 5);
		}
		Log.detail("Autodetecting resulted in " + res);
		return res == 0 ? 5 : res;
	}

	public void writeToBinary(final DataOutput out) throws IOException {
		Log.detail("Writing data to binary output stream");
		if (values.size() > 8)
			throw new IOException(
					"Cannot handle more than 8 values for binary data");

		// write header
		int hdr = (flags & 0x1F) << 27 | (values.size() - 1 & 0x07) << 24;
		for (int i = 0; i < values.size(); ++i)
			hdr |= (values.get(i).getType().getID() & 0x07) << i * 3;
		out.writeInt(hdr);

		// write the field content
		for (final Value value : values)
			value.writeToBinary(out);
	}

	public void writeToAscii(final DataOutput out) throws IOException {
		Log.detail("Writing data to ASCII output stream");
		if (flags != 0)
			throw new IOException("Cannot write flags for ASCII data");

		boolean first = true;
		for (final Value value : values) {
			// write delimiter if needed
			if (!first) {
				out.writeByte(' ');
				out.writeByte('|');
				out.writeByte(' ');
			}
			first = false;

			// write the field type identifier (and modifier if needed)
			boolean hex;
			switch (value.getType()) {
			case Int8:
			case Int32:
			case Int64:
				out.writeByte('I');
				hex = false;
				break;
			case Float32:
			case Float64:
				out.writeByte('F');
				hex = false;
				break;
			case UTFString:
			case NullTermString:
				out.writeByte('S');
				hex = true; // TODO should depend on content
				break;
			default:
				out.writeByte('B');
				hex = true;
			}
			if (hex) out.writeByte('x');
			out.writeByte(':');
			out.writeByte(' ');

			// write the field content in decimal or hex
			if (hex) {
				final ByteArrayOutputStream buf = new ByteArrayOutputStream();
				value.writeToAscii(new DataOutputStream(buf));
				final byte[] ba = buf.toByteArray();
				for (final byte b : ba) {
					out.write(HEX_TABLE[b >> 4 & 0x0F]);
					out.write(HEX_TABLE[b & 0x0F]);
				}
			} else
				value.writeToAscii(out);
		}

		// write the final block delimiter
		out.writeByte('\n');
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("[0x%02X]", flags));
		for (final Value value : values) {
			sb.append(" - ");
			sb.append(value);
		}
		return sb.toString();
	}

}
