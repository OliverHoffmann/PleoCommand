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

package pleocmd;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

public final class ImmutableRectangle {

	private final Rectangle r;

	public ImmutableRectangle(final Rectangle r) {
		this.r = r;
	}

	public int getX() {
		return r.x;
	}

	public int getY() {
		return r.y;
	}

	public int getWidth() {
		return r.width;
	}

	public int getHeight() {
		return r.height;
	}

	public Rectangle createCopy() {
		return new Rectangle(r);
	}

	public boolean equalsRect(final Rectangle rect) {
		return r.equals(rect);
	}

	public boolean contains(final Point p) {
		return r.contains(p);
	}

	public boolean intersects(final Rectangle other) {
		return r.intersects(other);
	}

	public boolean intersectsLine(final Line2D line) {
		return r.intersectsLine(line);
	}

	public Rectangle intersection(final Rectangle other) {
		return r.intersection(other);
	}

	public Rectangle union(final Rectangle other) {
		return r.union(other);
	}

	public void asClip(final Graphics g) {
		g.clipRect(r.x, r.y, r.width, r.height);
	}

	public void draw(final Graphics g) {
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	public void fill(final Graphics g) {
		g.fillRect(r.x, r.y, r.width, r.height);
	}

}
