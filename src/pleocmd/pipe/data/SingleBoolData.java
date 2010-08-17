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

package pleocmd.pipe.data;

import pleocmd.pipe.val.IntValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class SingleBoolData extends SingleValueData {

	public static final String IDENT = "bool";

	public SingleBoolData(final boolean value, final long user,
			final Data parent, final byte priority, final long time) {
		super(IDENT, asValue(value), user, parent, priority, time);
	}

	public SingleBoolData(final boolean value, final long user,
			final Data parent) {
		super(IDENT, asValue(value), user, parent);
	}

	private static Value asValue(final boolean value) {
		final Value val = Value.createForType(ValueType.Int8);
		((IntValue) val).set(value ? 1 : 0);
		return val;
	}

	public static boolean isSingleBoolData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static boolean getValue(final Data data) {
		return SingleValueData.getValueRaw(data).asLong() != 0;
	}

	public static long getUser(final Data data) {
		return SingleValueData.getUser(data);
	}

	/**
	 * Creates a new {@link SingleBoolData} with a new value - the user data
	 * will just be copied.
	 * 
	 * @param val
	 *            value for the new {@link SingleBoolData}
	 * @param parent
	 *            the original {@link Data} which should be compatible to
	 *            {@link SingleBoolData}, i.e. {@link #isSingleBoolData(Data)}
	 *            should return true.
	 * @return new {@link SingleBoolData}
	 */
	public static Data create(final boolean val, final Data parent) {
		return new SingleBoolData(val, getUser(parent), parent);
	}

}
