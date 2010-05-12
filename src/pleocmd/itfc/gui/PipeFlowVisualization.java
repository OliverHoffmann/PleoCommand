package pleocmd.itfc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import pleocmd.pipe.PipePart;

public final class PipeFlowVisualization extends Thread {

	static final int STEPS = 10;

	static final float FLOW_WIDTH = 3;

	static final int FLOW_LEN = 4;

	static final BasicStroke FLOW_STROKE = new BasicStroke(FLOW_WIDTH);

	private final PipeConfigBoard board;

	private final Deque<PipeFlow> queue;

	private boolean modified;

	private final class PipeFlow {
		private final PipePart src;
		private final PipePart dst;
		private final Double origin;
		private double theta;
		private double delta;
		private double offset;
		private int steps;

		public PipeFlow(final PipePart src, final PipePart dst) {
			this.src = src;
			this.dst = dst;
			origin = new Double();
			steps = STEPS;
		}

		public boolean draw(final Graphics2D g2) {
			offset += delta;
			g2.translate(origin.x, origin.y);
			g2.rotate(theta);
			g2.setColor(Color.BLUE);
			g2.setStroke(FLOW_STROKE);
			g2.drawLine((int) -offset, 0, (int) (-offset - FLOW_LEN), 0);
			return --steps > 0;
		}

		public Double getOrigin() {
			return origin;
		}

		public void setTheta(final double theta) {
			this.theta = theta;
		}

		public void setParams(final double len) {
			delta = (len - FLOW_LEN) / STEPS;
			offset = (STEPS - steps) * delta;
		}

		public PipePart getSrc() {
			return src;
		}

		public PipePart getDst() {
			return dst;
		}

	}

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
		synchronized (this) {
			queue.add(pf);
		}
	}

	@Override
	public void run() {
		boolean needRepainting = false;
		while (true) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				break;
			}
			synchronized (this) {
				if (needRepainting) board.repaint();
				if (modified) {
					modified = false;
					for (final PipeFlow pf : queue)
						relayout(pf);
				}
				List<PipeFlow> remove = null;
				for (final PipeFlow pf : queue) {
					final Graphics2D g2 = (Graphics2D) board.getGraphics();
					if (g2 == null) break;
					if (!pf.draw(g2)) {
						if (remove == null) remove = new ArrayList<PipeFlow>();
						remove.add(pf);
					}
				}
				needRepainting = !queue.isEmpty();
				if (remove != null) for (final PipeFlow pf : remove)
					queue.remove(pf);
			}
		}
		queue.clear();
	}

	private void relayout(final PipeFlow pf) {
		final double scale = board.getPainter().getScale();
		final Point sp = new Point();
		final Point dp = new Point();
		BoardPainter.calcConnectorPositions(pf.getSrc().getGuiPosition(), pf
				.getDst().getGuiPosition(), sp, dp);
		pf.getOrigin().x = sp.x * scale;
		pf.getOrigin().y = sp.y * scale;
		pf.setTheta(Math.atan2(sp.y - dp.y, sp.x - dp.x));
		final int xd = sp.x - dp.x;
		final int yd = sp.y - dp.y;
		pf.setParams(Math.sqrt(xd * xd + yd * yd) * scale);
	}

	public synchronized void modified() {
		modified = true;
	}

}
