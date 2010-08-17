// This file is part of PleoCommand:
// Interactively control Pleo with psychobiological parameters
//
// Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Boston, USA.

package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;

import pleocmd.exc.InternalException;

public final class StringValue extends Value {

	static final char TYPE_CHAR = 'S';

	static final ValueType RECOMMENDED_TYPE = ValueType.NullTermString;

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
			0, 0, 0, 0, 0, 0, 0, 0, // 80 - 87
			0, 0, 0, 0, 0, 0, 0, 0, // 88 - 8F
			0, 0, 0, 0, 0, 0, 0, 0, // 90 - 97
			0, 0, 0, 0, 0, 0, 0, 0, // 98 - 9F
			0, 0, 0, 0, 0, 0, 0, 0, // A0 - A7
			0, 0, 0, 0, 0, 0, 0, 0, // A8 - AF
			0, 0, 0, 0, 0, 0, 0, 0, // B0 - B7
			0, 0, 0, 0, 0, 0, 0, 0, // B8 - BF
			0, 0, 0, 0, 0, 0, 0, 0, // C0 - C7
			0, 0, 0, 0, 0, 0, 0, 0, // C8 - CF
			0, 0, 0, 0, 0, 0, 0, 0, // D0 - D7
			0, 0, 0, 0, 0, 0, 0, 0, // D8 - DF
			0, 0, 0, 0, 0, 0, 0, 0, // E0 - E7
			0, 0, 0, 0, 0, 0, 0, 0, // E8 - EF
			0, 0, 0, 0, 0, 0, 0, 0, // F0 - F7
			0, 0, 0, 0, 0, 0, 0, 0, // F8 - FF
	};

	private String val;

	protected StringValue(final ValueType type) {
		super(type);
		assert type == ValueType.UTFString || type == ValueType.NullTermString;
	}

	@Override
	int readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case UTFString: {
			final int len = in.readUnsignedShort();
			val = readUTF(in, len);
			return len + 2;
		}
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
			return len + 1;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	int writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case UTFString:
			return writeUTF(val, out);
		case NullTermString:
			final byte[] ba = val.getBytes("ISO-8859-1");
			out.write(ba);
			out.write((byte) 0);
			return ba.length + 1;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	void readFromAscii(final byte[] in, final int len) {
		try {
			val = new String(in, 0, len, "ISO-8859-1");
		} catch (final UnsupportedEncodingException e) {
			throw new InternalException(e);
		}
	}

	@Override
	int writeToAscii(final DataOutput out) throws IOException {
		final byte[] ba = val.getBytes("ISO-8859-1");
		out.write(ba);
		return ba.length;
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
			throw new InternalException(
					"Character-Set ISO-8859-1 not supported!");
		}
	}

	@Override
	boolean mustWriteAsciiAsHex() {
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

	@Override
	public Value set(final String content) {
		val = content;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof StringValue)) return false;
		return val.equals(((StringValue) o).val);
	}

	@Override
	public int hashCode() {
		return val.hashCode();
	}

	// copied from DataInputStream but removed reading utflen
	private static String readUTF(final DataInput in, final int utflen)
			throws IOException {
		final byte[] bytearr = new byte[utflen];
		final char[] chararr = new char[utflen];

		int c, char2, char3;
		int count = 0;
		int charArrCount = 0;

		in.readFully(bytearr, 0, utflen);

		while (count < utflen) {
			c = bytearr[count] & 0xff;
			if (c > 127) break;
			count++;
			chararr[charArrCount++] = (char) c;
		}

		while (count < utflen) {
			c = bytearr[count] & 0xff;
			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				/* 0xxxxxxx */
				count++;
				chararr[charArrCount++] = (char) c;
				break;
			case 12:
			case 13:
				/* 110x xxxx 10xx xxxx */
				count += 2;
				if (count > utflen)
					throw new UTFDataFormatException(
							"malformed input: partial character at end");
				char2 = bytearr[count - 1];
				if ((char2 & 0xC0) != 0x80)
					throw new UTFDataFormatException(
							"malformed input around byte " + count);
				chararr[charArrCount++] = (char) ((c & 0x1F) << 6 | char2 & 0x3F);
				break;
			case 14:
				/* 1110 xxxx 10xx xxxx 10xx xxxx */
				count += 3;
				if (count > utflen)
					throw new UTFDataFormatException(
							"malformed input: partial character at end");
				char2 = bytearr[count - 2];
				char3 = bytearr[count - 1];
				if ((char2 & 0xC0) != 0x80 || (char3 & 0xC0) != 0x80)
					throw new UTFDataFormatException(
							"malformed input around byte " + (count - 1));
				chararr[charArrCount++] = (char) ((c & 0x0F) << 12
						| (char2 & 0x3F) << 6 | (char3 & 0x3F) << 0);
				break;
			default:
				/* 10xx xxxx, 1111 xxxx */
				throw new UTFDataFormatException("malformed input around byte "
						+ count);
			}
		}
		// The number of chars produced may be less than utflen
		return new String(chararr, 0, charArrCount);
	}

	// copied from DataOutputStream (is not public there)
	private static int writeUTF(final String str, final DataOutput out)
			throws IOException {
		final int strlen = str.length();
		int utflen = 0;
		int c, count = 0;

		/* use charAt instead of copying String to char array */
		for (int i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if (c >= 0x0001 && c <= 0x007F)
				utflen++;
			else if (c > 0x07FF)
				utflen += 3;
			else
				utflen += 2;
		}

		if (utflen > 65535)
			throw new UTFDataFormatException("encoded string too long: "
					+ utflen + " bytes");

		final byte[] bytearr = new byte[utflen + 2];

		bytearr[count++] = (byte) (utflen >>> 8 & 0xFF);
		bytearr[count++] = (byte) (utflen >>> 0 & 0xFF);

		int i = 0;
		for (i = 0; i < strlen; i++) {
			c = str.charAt(i);
			if (!(c >= 0x0001 && c <= 0x007F)) break;
			bytearr[count++] = (byte) c;
		}

		for (; i < strlen; i++) {
			c = str.charAt(i);
			if (c >= 0x0001 && c <= 0x007F)
				bytearr[count++] = (byte) c;
			else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | c >> 12 & 0x0F);
				bytearr[count++] = (byte) (0x80 | c >> 6 & 0x3F);
				bytearr[count++] = (byte) (0x80 | c >> 0 & 0x3F);
			} else {
				bytearr[count++] = (byte) (0xC0 | c >> 6 & 0x1F);
				bytearr[count++] = (byte) (0x80 | c >> 0 & 0x3F);
			}
		}
		out.write(bytearr, 0, utflen + 2);
		return utflen + 2;
	}

}
