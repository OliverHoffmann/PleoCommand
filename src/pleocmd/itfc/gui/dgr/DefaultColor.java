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

package pleocmd.itfc.gui.dgr;

import java.awt.Color;

final class DefaultColor extends Color {

	private static final long serialVersionUID = -3241104615913127514L;

	private static final double MAKE_DARKER = 1.834;

	private final String name;

	public DefaultColor(final int r, final int g, final int b, final String name) {
		super(r, g, b);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Color makeDarker() {
		return new Color((int) (getRed() / MAKE_DARKER),
				(int) (getGreen() / MAKE_DARKER),
				(int) (getBlue() / MAKE_DARKER));
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
