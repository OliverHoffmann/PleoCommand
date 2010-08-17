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

import pleocmd.pipe.val.IntValue;
import pleocmd.pipe.val.StringValue;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

class SingleValueData extends Data {

	private static List<Value> l;

	protected SingleValueData(final String ident, final Value value,
			final long user, final Data parent, final byte priority,
			final long time) {
		super(l = new ArrayList<Value>(3), parent, priority, time, CTOR_DIRECT);
		init(ident, value, user);
	}

	protected SingleValueData(final String ident, final Value value,
			final long user, final Data parent) {
		super(l = new ArrayList<Value>(3), parent, CTOR_DIRECT);
		init(ident, value, user);
	}

	private static void init(final String ident, final Value value,
			final long user) {
		final Value valIdent = Value.createForType(ValueType.NullTermString);
		((StringValue) valIdent).set(ident);
		final Value valUser = Value.createForType(ValueType.Int64);
		((IntValue) valUser).set(user);
		l.add(valIdent);
		l.add(value);
		l.add(valUser);
	}

	public static Value getValueRaw(final Data data) {
		return data.get(1);
	}

	public static long getUser(final Data data) {
		return data.size() < 3 ? 0 : data.get(2).asLong();
	}

}
