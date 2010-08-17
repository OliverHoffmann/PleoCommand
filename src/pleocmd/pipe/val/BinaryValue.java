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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

final class BinaryValue extends Value {

	static final char TYPE_CHAR = 'B';

	static final ValueType RECOMMENDED_TYPE = ValueType.Data;

	private byte[] val;

	protected BinaryValue(final ValueType type) {
		super(type);
		assert type == ValueType.Data;
	}

	@Override
	public int readFromBinary(final DataInput in) throws IOException {
		switch (getType()) {
		case Data:
			final int len = in.readInt();
			try {
				val = new byte[len];
			} catch (final OutOfMemoryError e) {
				throw new IOException("Possibly invalid binary data size: "
						+ len, e);
			}
			in.readFully(val);
			return len + 4;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public int writeToBinary(final DataOutput out) throws IOException {
		switch (getType()) {
		case Data:
			out.writeInt(val.length);
			out.write(val);
			return val.length + 4;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	public void readFromAscii(final byte[] in, final int len) {
		val = new byte[len];
		System.arraycopy(in, 0, val, 0, len);
	}

	@Override
	public int writeToAscii(final DataOutput out) throws IOException {
		out.write(val);
		return val.length;
	}

	@Override
	public String toString() {
		return DataAsciiConverter.toHexString(val, val.length);
	}

	@Override
	public byte[] asByteArray() {
		return val;
	}

	@Override
	public boolean mustWriteAsciiAsHex() {
		return true;
	}

	@Override
	public Value set(final String content) throws UnsupportedEncodingException {
		val = content.getBytes("ISO-8859-1");
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof BinaryValue)) return false;
		return Arrays.equals(val, ((BinaryValue) o).val);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(val);
	}

}
