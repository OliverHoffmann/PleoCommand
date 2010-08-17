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

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.val.StringValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

class MultiValueData extends Data {

	private static List<Value> l;

	protected MultiValueData(final String ident, final Value[] values,
			final Data parent, final byte priority, final long time) {
		super(l = new ArrayList<Value>(1 + values.length), parent, priority,
				time, CTOR_DIRECT);
		init(ident, values);
	}

	protected MultiValueData(final String ident, final Value[] values,
			final Data parent) {
		super(l = new ArrayList<Value>(1 + values.length), parent, CTOR_DIRECT);
		init(ident, values);
	}

	private static void init(final String ident, final Value[] values) {
		final Value valIdent = Value.createForType(ValueType.NullTermString);
		((StringValue) valIdent).set(ident);
		l.add(valIdent);
		for (final Value v : values)
			l.add(v);
	}

	public static int getValueCount(final Data data) {
		return data.size() - 1;
	}

	public static Value getValueRaw(final Data data, final int index) {
		return data.get(index + 1);
	}

}
