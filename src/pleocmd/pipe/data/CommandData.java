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

public class CommandData extends Data {

	private static List<Value> l;

	public CommandData(final String command, final String argument,
			final Data parent, final byte priority, final long time) {
		super(l = new ArrayList<Value>(2), parent, priority, time, CTOR_DIRECT);
		Value val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(command);
		l.add(val);
		val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(argument);
		l.add(val);
	}

	public CommandData(final String command, final String argument,
			final Data parent) {
		super(l = new ArrayList<Value>(2), parent, CTOR_DIRECT);
		Value val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(command);
		l.add(val);
		val = Value.createForType(ValueType.NullTermString);
		((StringValue) val).set(argument);
		l.add(val);
	}

	public static boolean isCommandData(final Data data) {
		return data.size() >= 2 && data.get(0) instanceof StringValue;
	}

	public static boolean isCommandData(final Data data, final String command) {
		return data.size() >= 2 && data.get(0) instanceof StringValue
				&& command.equals(data.get(0).asString());
	}

	public static String getCommand(final Data data) {
		return data.get(0).asString();
	}

	public static String getArgument(final Data data) {
		return data.get(1).asString();
	}

}
