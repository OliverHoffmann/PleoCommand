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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import pleocmd.pipe.PipePart;

public final class PipeFlowVisualization extends Thread {

	private final PipeConfigBoard board;

	private final Deque<PipeFlow> queue;

	private boolean modified;

	private boolean cancelled;

	PipeFlowVisualization(final PipeConfigBoard board) {
		super("PipeFlow-Visualization-Thread");
		this.board = board;
		queue = new LinkedList<PipeFlow>();
		setDaemon(true);
		start();
	}

	public void addPipeFlow(final PipePart src, final PipePart dst) {
		final PipeFlow pf = new PipeFlow(src, dst);
		relayout(pf);
		synchronized (queue) {
			queue.add(pf);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				break;
			}
			synchronized (queue) {
				if (cancelled) break;
				if (modified) {
					modified = false;
					for (final PipeFlow pf : queue)
						relayout(pf);
				}
				List<PipeFlow> remove = null;
				for (final PipeFlow pf : queue)
					if (!pf.nextStep()) {
						if (remove == null) remove = new ArrayList<PipeFlow>();
						remove.add(pf);
					}
				if (remove != null) for (final PipeFlow pf : remove)
					queue.remove(pf);
				board.setPipeflow(queue);
			}
			board.repaint();
		}
		queue.clear();
	}

	private void relayout(final PipeFlow pf) {
		final Point sp = new Point();
		final Point dp = new Point();
		BoardPainter.calcConnectorPositions(pf.getSrc().getGuiPosition(), pf
				.getDst().getGuiPosition(), sp, dp);
		pf.getOrigin().x = sp.x;
		pf.getOrigin().y = sp.y;
		pf.setTheta(Math.atan2(sp.y - dp.y, sp.x - dp.x));
		final int xd = sp.x - dp.x;
		final int yd = sp.y - dp.y;
		pf.setParams(Math.sqrt(xd * xd + yd * yd));
	}

	public void modified() {
		synchronized (queue) {
			modified = true;
		}
	}

	public void cancel() {
		synchronized (queue) {
			cancelled = true;
		}
	}

}
