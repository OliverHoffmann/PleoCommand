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

import pleocmd.pipe.val.FloatValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class SingleFloatData extends SingleValueData {

	public static final String IDENT = "float";

	public SingleFloatData(final double value, final long user,
			final Data parent, final byte priority, final long time) {
		super(IDENT, asValue(value), user, parent, priority, time);
	}

	public SingleFloatData(final double value, final long user,
			final Data parent) {
		super(IDENT, asValue(value), user, parent);
	}

	private static Value asValue(final double value) {
		final Value val = Value.createForType(ValueType.Float64);
		((FloatValue) val).set(value);
		return val;
	}

	public static boolean isSingleFloatData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static double getValue(final Data data) {
		return SingleValueData.getValueRaw(data).asDouble();
	}

	public static long getUser(final Data data) {
		return SingleValueData.getUser(data);
	}

	/**
	 * Creates a new {@link SingleFloatData} with a new value - the user data
	 * will just be copied.
	 * 
	 * @param val
	 *            value for the new {@link SingleFloatData}
	 * @param parent
	 *            the original {@link Data} which should be compatible to
	 *            {@link SingleFloatData}, i.e. {@link #isSingleFloatData(Data)}
	 *            should return true.
	 * @return new {@link SingleFloatData}
	 */
	public static Data create(final double val, final Data parent) {
		return new SingleFloatData(val, getUser(parent), parent);
	}

}
