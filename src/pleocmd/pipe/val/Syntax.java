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

import java.awt.Color;

public final class Syntax {

	public static enum Type {
		// from http://en.wikipedia.org/wiki/Web_colors
		Error(255, 0, 0), // red
		Flags(250, 140, 0), // dark orange
		FlagPrio(255, 99, 71), // tomato
		FlagTime(255, 215, 0), // gold
		FlagVeryLong(255, 0, 255), // magenta
		FieldDelim(70, 130, 180), // steel blue
		IntField(0, 100, 0), // dark green
		FloatField(46, 139, 87), // sea green
		StringField(0, 0, 0), // black
		DataField(112, 128, 144), // slate gray
		HexField(47, 79, 79), // dark slate gray
		TypeIdent(138, 43, 226); // blue violet

		private final Color color;

		private Type(final int r, final int g, final int b) {
			color = new Color(r, g, b);
		}

		public Color getColor() {
			return color;
		}

		@Override
		public String toString() {
			return super.toString() + " - " + color;
		}

	}

	private final Type type;

	private int position;

	public Syntax(final Type type, final int position) {
		this.type = type;
		this.position = position;
	}

	public Type getType() {
		return type;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return String.format("%03d: %s", position, type);
	}

	public void duplicatePosition() {
		position *= 2;
	}

}
