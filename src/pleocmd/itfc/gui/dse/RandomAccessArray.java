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

package pleocmd.itfc.gui.dse;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class RandomAccessArray implements RandomAccess {

	private byte[] ba;

	private int pos;

	private int length;

	private final DataInput dataIn;

	private final DataOutput dataOut;

	public RandomAccessArray(final InputStream in, final long length)
			throws IOException {
		if (length < 0 || length > Integer.MAX_VALUE)
			throw new IOException("Invalid length: " + length);
		this.length = (int) length;
		ba = new byte[this.length];
		if (this.length > 0 && in.read(ba) != this.length)
			throw new IOException("Failed to read from InputStream");

		dataIn = new DataInputStream(new InputStream() {
			@Override
			public int read() throws IOException {
				return RandomAccessArray.this.read();
			}
		});
		dataOut = new DataOutputStream(new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				RandomAccessArray.this.write(b);
			}
		});
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public long getFilePointer() {
		return pos;
	}

	@Override
	public void seek(final long newPos) throws IOException {
		if (newPos < 0 || newPos > Integer.MAX_VALUE)
			throw new IOException("Invalid position: " + newPos);
		pos = (int) newPos;
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public int read() throws IOException {
		if (pos < 0 || pos >= length)
			throw new IOException("Invalid position: " + pos);
		return ba[pos++] & 0xFF;
	}

	@Override
	public void write(final int b) throws IOException {
		if (pos < 0) throw new IOException("Invalid position: " + pos);
		if (pos < length)
			ba[pos++] = (byte) b;
		else {
			if (pos >= ba.length) {
				// make about 33% larger than needed
				// but at least 100 bytes on small arrays
				final int cap = (int) ((pos + 100) * 1.333);
				final byte[] newba = new byte[cap];
				// we only need to keep the valid bytes
				System.arraycopy(ba, 0, newba, 0, length);
				ba = newba;
			}
			// fill the grown area with zeroes
			for (int i = length; i < pos; ++i)
				ba[i] = 0;
			// finally set the new byte and length of file
			ba[pos++] = (byte) b;
			length = pos;
		}
	}

	@Override
	public DataInput getDataInput() {
		return dataIn;
	}

	@Override
	public DataOutput getDataOutput() {
		return dataOut;
	}

}
