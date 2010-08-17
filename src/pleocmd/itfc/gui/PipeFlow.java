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

package pleocmd.itfc.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;
import java.util.HashMap;
import java.util.Map;

import pleocmd.pipe.PipePart;

final class PipeFlow {

	private static final int STEPS = 10;

	private static final int FLOW_LEN = 4;

	private static final Map<Integer, Integer> COLORS = new HashMap<Integer, Integer>();

	private final PipePart src;
	private final PipePart dst;
	private final Double origin;
	private double theta;
	private double delta;
	private double offset;
	private int steps;

	private final Color color;

	PipeFlow(final PipePart src, final PipePart dst) {
		this.src = src;
		this.dst = dst;
		origin = new Double();
		final int hc = src.hashCode() ^ dst.hashCode();
		Integer lastColor = COLORS.get(hc);
		if (lastColor == null)
			lastColor = 63;
		else
			lastColor = (lastColor - 56) % 192 + 64;
		COLORS.put(hc, lastColor);
		color = new Color(0, 0, lastColor);
		steps = STEPS;
	}

	public void draw(final Graphics2D g2) {
		final AffineTransform at = g2.getTransform();
		g2.translate(origin.x, origin.y);
		g2.rotate(theta);
		g2.setColor(color);
		g2.drawLine((int) -offset, 0, (int) (-offset - FLOW_LEN), 0);
		g2.setTransform(at);
	}

	public boolean nextStep() {
		offset += delta;
		return --steps > 0;
	}

	Double getOrigin() {
		return origin;
	}

	void setTheta(final double theta) {
		this.theta = theta;
	}

	void setParams(final double len) {
		delta = (len - FLOW_LEN) / STEPS;
		offset = (STEPS - steps) * delta;
	}

	PipePart getSrc() {
		return src;
	}

	PipePart getDst() {
		return dst;
	}

}
