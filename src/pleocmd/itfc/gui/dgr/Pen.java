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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

final class Pen {

	private final Color color;

	private final BasicStroke stroke;

	public Pen(final Color color, final BasicStroke stroke) {
		this.color = color;
		this.stroke = stroke;
	}

	public Pen(final Color color, final float width) {
		this.color = color;
		stroke = new BasicStroke(width);
	}

	public Pen(final Color color) {
		this.color = color;
		stroke = new BasicStroke(1.0f);
	}

	public Color getColor() {
		return color;
	}

	public BasicStroke getStroke() {
		return stroke;
	}

	public void assignTo(final Graphics2D g2d) {
		g2d.setColor(color);
		g2d.setStroke(stroke);
	}

}
