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
import java.io.DataOutput;
import java.io.IOException;

public interface RandomAccess {

	long length() throws IOException;

	long getFilePointer() throws IOException;

	void seek(long pos) throws IOException;

	void close() throws IOException;

	int read() throws IOException;

	void write(int b) throws IOException;

	DataInput getDataInput();

	DataOutput getDataOutput();

}
