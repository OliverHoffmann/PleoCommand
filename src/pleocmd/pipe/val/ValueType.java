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

public enum ValueType {

	Int8(0), Int32(1), Int64(2), Float32(3), Float64(4), UTFString(5), NullTermString(
			6), Data(7);

	private int id;

	private ValueType(final int id) {
		this.id = id;
	}

	/**
	 * @return ID of this {@link ValueType} used in binary streams
	 */
	public int getID() {
		return id;
	}

}
