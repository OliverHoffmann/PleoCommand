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

public final class MultiFloatData extends MultiValueData {

	public static final String IDENT = "list of float";

	public MultiFloatData(final double[] values, final Data parent,
			final byte priority, final long time) {
		super(IDENT, asValue(values), parent, priority, time);
	}

	public MultiFloatData(final double[] values, final Data parent) {
		super(IDENT, asValue(values), parent);
	}

	public MultiFloatData(final Data parent) {
		super(IDENT, parent.toArray(new Value[parent.size()]), parent);
	}

	private static Value[] asValue(final double[] values) {
		final Value[] res = new Value[values.length];
		for (int i = 0; i < values.length; ++i) {
			res[i] = Value.createForType(ValueType.Float64);
			((FloatValue) res[i]).set(values[i]);
		}
		return res;
	}

	public static boolean isMultiFloatData(final Data data) {
		return IDENT.equals(data.getSafe(0).asString());
	}

	public static int getValueCount(final Data data) {
		return MultiValueData.getValueCount(data);
	}

	public static double getValue(final Data data, final int index) {
		return MultiValueData.getValueRaw(data, index).asDouble();
	}

}
