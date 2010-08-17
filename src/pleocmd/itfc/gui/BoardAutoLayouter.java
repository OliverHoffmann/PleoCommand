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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import pleocmd.ImmutableRectangle;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

final class BoardAutoLayouter {

	private static final int MAX_SPREADING = 1000000;

	private static final int STEPS_WO_CHANGE_1 = 10000;

	private static final int STEPS_WO_CHANGE_2 = 1000;

	private static final int STEPS_BETWEEN_UPDATE = 100;

	private final NavigableMap<Double, List<Layout>> fringe;

	private final Set<Layout> found;

	private final PipeConfigBoard board;

	private boolean considerSpreading;

	private boolean interrupted;

	private int steps;

	private int lastIntersections;

	private int bestSpreading;

	@SuppressWarnings("synthetic-access")
	public BoardAutoLayouter(final PipeConfigBoard board) {
		fringe = new TreeMap<Double, List<Layout>>();
		found = new HashSet<Layout>();
		this.board = board;
		Layout.border1 = board.getPainter().getBorder1(false);
		Layout.border2 = board.getPainter().getBorder2(false);
		Layout.bounds = new Dimension();
		final double s = board.getPainter().getScale();
		Layout.bounds.width = (int) (board.getWidth() / s) - 1;
		Layout.bounds.height = (int) (board.getHeight() / s) - 1;
	}

	public void start() {
		considerSpreading = false;
		final Map<PipePart, ImmutableRectangle> parts = new HashMap<PipePart, ImmutableRectangle>();
		for (final PipePart pp : board.getPainter().getSet())
			parts.put(pp, pp.getGuiPosition());
		Layout res = treeSearch(new Layout(null, parts));

		considerSpreading = true;
		res = treeSearch(res);

		if (res != null) res.accept();
	}

	public void interrupt() {
		interrupted = true;
	}

	public double getProgress() {
		return considerSpreading ? 0.5 + steps / (double) STEPS_WO_CHANGE_2 / 2
				: steps / (double) STEPS_WO_CHANGE_1 / 2;
	}

	public String getProgressText() {
		return String.format(
				"%s phase: %d of %d iterations [%d intersection%s, "
						+ "spreading is %d]", considerSpreading ? "Second"
						: "First", steps, considerSpreading ? STEPS_WO_CHANGE_2
						: STEPS_WO_CHANGE_1, lastIntersections,
				lastIntersections == 1 ? "" : "s", bestSpreading);
	}

	private Layout treeSearch(final Layout root) {
		if (root == null || interrupted) return null;
		steps = 0;
		lastIntersections = 0;
		bestSpreading = Integer.MAX_VALUE;
		fringe.clear();
		found.clear();
		insertInFringe(root, 0);
		Layout lay = root;
		while (!fringe.isEmpty()) {
			lay = removeFromFringe();
			if (lastIntersections != lay.getIntersections()) {
				lastIntersections = lay.getIntersections();
				steps = 0;
			}
			if (bestSpreading > lay.getSpreading()) {
				bestSpreading = lay.getSpreading();
				steps = 0;
			}
			if (steps % STEPS_BETWEEN_UPDATE == 0) {
				// we can't find a better match during phase 1
				if (!considerSpreading && lastIntersections == 0) return lay;
				if (interrupted) break;
				lay.accept();
				board.repaint();
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					interrupted = true;
					break;
				}
			}
			lay.expand(this);
			if (++steps >= (considerSpreading ? STEPS_WO_CHANGE_2
					: STEPS_WO_CHANGE_1)) break;
		}
		return interrupted ? null : lay;
	}

	/**
	 * Inserts a new candidate if it has not already been checked nor is in the
	 * list currently.
	 * 
	 * @param layout
	 *            a possible layout of the board
	 * @param cost
	 *            between 0 and 1 - the higher the cost, the later this layout
	 *            will be tested and other layouts with equal heuristics will be
	 *            preferred but lower cost instead
	 */
	protected void insertInFringe(final Layout layout, final double cost) {
		// already tried this layout or will be trying it soon
		if (found.contains(layout)) return;
		found.add(layout);

		// strategy:
		// 1. want to have least possible crossings
		// 2. want to have best possible spreading
		// 3. want to try deeper paths with more modifications first
		// 4. want to randomize the list generated by expand() a bit
		final double g = -layout.getDepth() / 1000.0; // 0.0001 .. 1
		final double c = cost / 1000.0; // 0 .. 0.0001
		final double h = MAX_SPREADING * layout.getIntersections() // 1000000 ..
				- (considerSpreading ? layout.getSpreading() : 0);
		final double f = g + c + h;

		List<Layout> l = fringe.get(f);
		if (l == null) {
			l = new ArrayList<Layout>(1);
			fringe.put(f, l);
		}
		l.add(layout);
	}

	private Layout removeFromFringe() {
		final Entry<Double, List<Layout>> e = fringe.firstEntry();
		final List<Layout> l = e.getValue();
		final Layout res = l.remove(l.size() - 1);
		if (l.isEmpty()) fringe.remove(e.getKey());
		return res;
	}

	private static final class Layout {

		private static final int MIN_STEP = 5;

		private static final int MAX_STEP = 100;

		private static int border1;

		private static int border2;

		private static Dimension bounds;

		private final Map<PipePart, ImmutableRectangle> parts;

		private final Map<PipePart, Double> candidateValues;

		private final int depth;

		private int intersections = -1;

		private int spreading = -1;

		protected Layout(final Layout org,
				final Map<PipePart, ImmutableRectangle> parts) {
			this.parts = parts;
			candidateValues = new HashMap<PipePart, Double>();
			depth = org == null ? 0 : org.depth + 1;
		}

		protected void accept() {
			for (final Entry<PipePart, ImmutableRectangle> e : parts.entrySet())
				e.getKey().setGuiPosition(e.getValue().createCopy());
		}

		/**
		 * Creates a new layout with simulated moving of one PipePart in one
		 * direction.
		 * 
		 * @param bal
		 *            to this layouter's fringe the new Layout will be added
		 * @param pp
		 *            the PipePart for which so simulate moving
		 * @param rect
		 *            current position of the path in the layout
		 * @param xd
		 *            between 0 and 1
		 * @param yd
		 *            between 0 and 1
		 * @param c
		 *            between >0 and 1
		 */
		private void expandPart(final BoardAutoLayouter bal, final PipePart pp,
				final ImmutableRectangle rect, final double xd,
				final double yd, final double c) {
			final Rectangle r = rect.createCopy();
			r.x += MIN_STEP * (int) (1 + MAX_STEP / MIN_STEP * xd);
			r.y += (int) (yd * MAX_STEP);
			check(r, pp);

			// quick-check if later found.contains() would return true anyway
			if (rect.equalsRect(r)) return;

			// clone list but modify pp's value
			final Map<PipePart, ImmutableRectangle> copy;
			copy = new HashMap<PipePart, ImmutableRectangle>(parts);
			copy.put(pp, new ImmutableRectangle(r));

			// found a (new) candidate
			bal.insertInFringe(new Layout(this, copy), c * Math.random());
		}

		protected void expand(final BoardAutoLayouter bal) {
			for (final Entry<PipePart, ImmutableRectangle> e : parts.entrySet()) {
				// Parts which are involved in many intersections will
				// get a slightly higher priority
				final Double v0 = candidateValues.get(e.getKey());
				final double v = v0 == null ? 1.0 : Math.max(0.01, Math.min(
						1.0, v0));
				expandPart(bal, e.getKey(), e.getValue(), Math.random(), 0, v);
				expandPart(bal, e.getKey(), e.getValue(), -Math.random(), 0, v);
				expandPart(bal, e.getKey(), e.getValue(), 0, Math.random(), v);
				expandPart(bal, e.getKey(), e.getValue(), 0, -Math.random(), v);
			}
		}

		protected int getDepth() {
			return depth;
		}

		private void incCandidateValue(final PipePart pp) {
			final Double v = candidateValues.get(pp);
			candidateValues.put(pp, v == null ? 0.1 : v + 0.1);
		}

		private static final class ConnLine {
			private final Line2D line;
			private final PipePart pp1;
			private final PipePart pp2;

			public ConnLine(final Line2D line, final PipePart pp1,
					final PipePart pp2) {
				this.line = line;
				this.pp1 = pp1;
				this.pp2 = pp2;
			}

			public Line2D getLine() {
				return line;
			}

			public PipePart getPP1() {
				return pp1;
			}

			public PipePart getPP2() {
				return pp2;
			}
		}

		protected int getIntersections() {
			if (intersections != -1) return intersections;
			// intersection of connections with PipeParts and other ones is bad
			int its = 0;
			final List<ConnLine> conns = new ArrayList<ConnLine>();
			for (final Entry<PipePart, ImmutableRectangle> e : parts.entrySet())
				for (final PipePart ppTrg : e.getKey().getConnectedPipeParts()) {
					final Point ps = new Point();
					final Point pt = new Point();
					BoardPainter.calcConnectorPositions(e.getValue(), parts
							.get(ppTrg), ps, pt);
					final Line2D line = new Line2D.Float(ps, pt);
					for (final ConnLine conn : conns) {
						final Line2D line2 = conn.getLine();
						if (conn.getLine().intersectsLine(line)
								&& !ps.equals(line2.getP1())
								&& !ps.equals(line2.getP2())
								&& !pt.equals(line2.getP1())
								&& !pt.equals(line2.getP2())) {
							++its;
							// bad for both ends of both connections
							// which intersect
							incCandidateValue(e.getKey());
							incCandidateValue(ppTrg);
							incCandidateValue(conn.getPP1());
							incCandidateValue(conn.getPP2());
						}
					}
					for (final Entry<PipePart, ImmutableRectangle> e2 : parts
							.entrySet()) {
						boolean intersects;
						if (e2.getKey() == e.getKey() || e2.getKey() == ppTrg) {
							final Rectangle r = e2.getValue().createCopy();
							r.grow(-2, -2);
							intersects = r.intersectsLine(line);
						} else
							intersects = e2.getValue().intersectsLine(line);
						if (intersects) {
							++its;
							// bad for the PipePart and both ends of the
							// connection which intersects
							incCandidateValue(e.getKey());
							incCandidateValue(ppTrg);
							incCandidateValue(e2.getKey());
						}
					}
					conns.add(new ConnLine(line, e.getKey(), ppTrg));
				}
			intersections = its;
			return intersections;
		}

		public int getSpreading() {
			if (spreading != -1) return spreading;
			double spread = .0;
			// the more spread, the better
			for (final Entry<PipePart, ImmutableRectangle> e1 : parts
					.entrySet()) {
				for (final Entry<PipePart, ImmutableRectangle> e2 : parts
						.entrySet())
					if (e1 != e2)
						spread += getDistance(e1.getValue(), e2.getValue());
				spread += getDistance(e1.getValue(), new Line2D.Float(0, 0,
						bounds.width, 0));
				spread += getDistance(e1.getValue(), new Line2D.Float(
						bounds.width, 0, bounds.width, bounds.height));
				spread += getDistance(e1.getValue(), new Line2D.Float(0,
						bounds.height, bounds.width, bounds.height));
				spread += getDistance(e1.getValue(), new Line2D.Float(0, 0, 0,
						bounds.height));
			}
			spreading = Math.min(MAX_SPREADING - 1, (int) (spread + 0.5));
			return spreading;
		}

		private double getDistance(final ImmutableRectangle r1,
				final ImmutableRectangle r2) {
			final int cx1 = r1.getX() + r1.getWidth() / 2;
			final int cy1 = r1.getY() + r1.getHeight() / 2;
			final int cx2 = r2.getX() + r2.getWidth() / 2;
			final int cy2 = r2.getY() + r2.getHeight() / 2;
			final int dx = cx1 - cx2;
			final int dy = cy1 - cy2;
			return Math.log(Math.sqrt(dx * dx + dy * dy));
		}

		private double getDistance(final ImmutableRectangle r1, final Line2D l2) {
			return Math.log(l2.ptLineDist(r1.getX() + r1.getWidth() / 2, r1
					.getY()
					+ r1.getHeight() / 2));
		}

		private void check(final Rectangle r, final PipePart pp) {
			final int xMin;
			final int xMax;
			final int yMin = 1;
			final int yMax = bounds.height;
			if (Input.class.isInstance(pp)) {
				xMin = 1;
				xMax = border1 - 1;
			} else if (Converter.class.isInstance(pp)) {
				xMin = border1 + 1;
				xMax = border2 - 1;
			} else if (Output.class.isInstance(pp)) {
				xMin = border2 + 1;
				xMax = bounds.width;
			} else {
				xMin = 1;
				xMax = bounds.width;
			}
			if (r.x < xMin) r.x = xMin;
			if (r.y < yMin) r.y = yMin;
			if (r.x + r.width > xMax) r.x = xMax - r.width;
			if (r.y + r.height > yMax) r.y = yMax - r.height;
			for (final Entry<PipePart, ImmutableRectangle> e : parts.entrySet())
				if (e.getKey() != pp && e.getValue().intersects(r)) {
					// move r, so it doesn't intersect anymore
					final ImmutableRectangle rO = e.getValue();
					final Rectangle i = rO.intersection(r);
					final int x0 = r.x + r.width / 2;
					final int y0 = r.y + r.height / 2;
					final int x1 = rO.getX() + rO.getWidth() / 2;
					final int y1 = rO.getY() + rO.getHeight() / 2;
					if (i.width < i.height && r.x - i.width >= xMin
							&& r.x + r.width + i.width <= xMax) {
						if (x0 > x1) // move right
							r.translate(i.width, 0);
						else
							// move left
							r.translate(-i.width, 0);
					} else if (y0 > y1) {
						if (r.y + r.height + i.height > yMax)
							// move up instead of down
							r
									.translate(0, i.height - r.height
											- rO.getHeight());
						else
							r.translate(0, i.height); // move down
					} else if (r.y - i.height < yMin)
						// move down instead of up
						r.translate(0, r.height + rO.getHeight() - i.height);
					else
						r.translate(0, -i.height); // move up
					// check bounds again
					// (overlapping is better than being out of bounds)
					if (r.x < xMin) r.x = xMin;
					if (r.y < yMin) r.y = yMin;
					if (r.x + r.width > xMax) r.x = xMax - r.width;
					if (r.y + r.height > yMax) r.y = yMax - r.height;
				}
		}

		@Override
		public boolean equals(final Object other) {
			return other instanceof Layout
					&& parts.equals(((Layout) other).parts);
		}

		@Override
		public int hashCode() {
			return parts.hashCode();
		}

	}

}
