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

public final class IntValue extends Value {

	static final char TYPE_CHAR = 'I';

	static final ValueType RECOMMENDED_TYPE = ValueType.Int64;

	private long val;

	protected IntValue(final ValueType type) {
		super(type);
		assert type == ValueType.Int8 || type == ValueType.Int32
				|| type == ValueType.Int64;
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
			out.writeLong(val);
			return 8;
		default:
			throw new RuntimeException("Invalid type for this class");
		}
	}

	@Override
	void readFromAscii(final byte[] in, final int len) throws IOException {
		// work around for java bug: Long.valueOf can't handle "\+[0-9]+"
		if (in.length > 0 && in[0] == '+')
			val = Long.valueOf(new String(in, 1, len - 1, "US-ASCII"));
		else
			val = Long.valueOf(new String(in, 0, len, "US-ASCII"));
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
	public long asLong() {
		return val;
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
		val = Long.valueOf(content);
		return this;
	}

	public Value set(final long content) {
		val = content;
		return this;
	}

	@Override
	public void compact() {
		if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE)
			changeType(ValueType.Int8);
		else if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE)
			changeType(ValueType.Int32);
		else
			changeType(ValueType.Int64);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof IntValue)) return false;
		return val == ((IntValue) o).val;
	}

	@Override
	public int hashCode() {
		return (int) (val ^ val >>> 32);
	}

}
