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

import pleocmd.pipe.data.Data;

/**
 * A placeholder for a {@link Value} on a position in a {@link Data} which does
 * not exist. Just returns some safe default value for every conversion
 * operation and never fails during conversion.
 * 
 * @see Data#getSafe(int)
 * @author oliver
 */
public final class DummyValue extends Value {

	public DummyValue() {
		super(ValueType.Data);
	}

	@Override
	int readFromBinary(final DataInput in) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	int writeToBinary(final DataOutput out) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	void readFromAscii(final byte[] in, final int len) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	int writeToAscii(final DataOutput out) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	boolean mustWriteAsciiAsHex() {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public long asLong() {
		return 0;
	}

	@Override
	public double asDouble() {
		return .0;
	}

	@Override
	public String asString() {
		return "";
	}

	@Override
	public byte[] asByteArray() {
		return new byte[0];
	}

	@Override
	public Value set(final String content) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof DummyValue;
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
