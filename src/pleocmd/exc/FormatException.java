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

package pleocmd.exc;

import java.util.List;

import pleocmd.pipe.val.Syntax;
import pleocmd.pipe.val.Syntax.Type;

public final class FormatException extends Exception {

	private static final long serialVersionUID = -2642142547584763520L;

	private final int index;

	public FormatException(final List<Syntax> syntaxList, final int index,
			final String msg, final Object... args) {
		super(String.format("At position %d: ", index)
				+ String.format(msg, args));
		this.index = index;
		if (syntaxList != null) syntaxList.add(new Syntax(Type.Error, index));
	}

	public FormatException(final int index, final String msg,
			final Object... args) {
		super(String.format("At position %d: ", index)
				+ String.format(msg, args));
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
