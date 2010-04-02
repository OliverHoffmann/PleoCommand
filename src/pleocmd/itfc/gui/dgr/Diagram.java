package pleocmd.itfc.gui.dgr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import pleocmd.itfc.gui.dgr.DiagramDataSet.DiagramType;

public class Diagram extends JPanel {

	private static final long serialVersionUID = -8245547025738665255L;

	protected static final double SCALE_SPEED_MOUSE = 0.01;

	protected static final double MOVE_SPEED_MOUSE = 0.05;

	private static final int BORDER = 4;

	private static final double MIN_GRID_DELTA = 2.0;

	private static final Color[] DEFAULT_COLORS = new Color[] {
			new Color(0xFF, 0x45, 0x00), // orange red
			new Color(0x7F, 0xFF, 0x00), // chartreuse
			new Color(0x63, 0xB8, 0xFF), // steel blue
			new Color(0xFF, 0xD7, 0x00), // gold
			new Color(0xFF, 0x6A, 0x6A), // indian red
			new Color(0x00, 0xFF, 0xFF), // cyan
			new Color(0xFF, 0xF6, 0x8F), // khaki
			new Color(0x7F, 0xFF, 0xD4), // aqua marine
			new Color(0xFF, 0x69, 0xB4), // hot pink
			new Color(0xC1, 0xFF, 0xC1), // dark sea green
			new Color(0xFF, 0xFF, 0xF0), // ivory
			new Color(0x83, 0x6F, 0xFF), // slate blue
			new Color(0xC0, 0xFF, 0x3E), // olive drab
			new Color(0xFF, 0x83, 0xFA), // orchid
			new Color(0xFF, 0xE7, 0xBA), // wheat
			new Color(0xFF, 0xC1, 0xC1), // rosy brown
	};

	private static final double MAKE_DARKER = 1.834;

	private final List<DiagramDataSet> dataSets = new ArrayList<DiagramDataSet>();

	private Color backgroundColor = Color.WHITE;

	private Pen axisPen = new Pen(Color.BLACK);

	private Pen unitPen = new Pen(Color.GRAY);

	private Pen subUnitPen = new Pen(Color.LIGHT_GRAY);

	private final DiagramAxis xAxis = new DiagramAxis(this, "x");

	private final DiagramAxis yAxis = new DiagramAxis(this, "y");

	private double zoom = 1.0;

	private AffineTransform originalTransform;

	private enum AlignH {
		Left, Center, Right;
	}

	private enum AlignV {
		Top, Center, Bottom;
	}

	public Diagram() {
		addMouseMotionListener(new MouseMotionListener() {
			private int oldx;

			private int oldy;

			@Override
			public void mouseMoved(final MouseEvent e) {
				oldx = e.getX();
				oldy = e.getY();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				final int newx = e.getX();
				final int newy = e.getY();
				getXAxis().setOffset(
						getXAxis().getOffset() - (newx - oldx)
								* MOVE_SPEED_MOUSE / getZoom());
				getYAxis().setOffset(
						getYAxis().getOffset() + (newy - oldy)
								* MOVE_SPEED_MOUSE / getZoom());
				oldx = newx;
				oldy = newy;
				repaint();
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				final int clicks = -e.getWheelRotation();
				if (clicks > 0)
					setZoom(getZoom() * (1 + SCALE_SPEED_MOUSE * clicks));
				if (clicks < 0)
					setZoom(getZoom() / (1 - SCALE_SPEED_MOUSE * clicks));
				repaint();
			}
		});
	}

	synchronized void addDataSet(final DiagramDataSet dataSet) {
		dataSets.add(dataSet);
	}

	public List<DiagramDataSet> getDataSets() {
		return Collections.unmodifiableList(dataSets);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public synchronized void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Pen getAxisPen() {
		return axisPen;
	}

	public synchronized void setAxisPen(final Pen axisPen) {
		this.axisPen = axisPen;
	}

	public Pen getUnitPen() {
		return unitPen;
	}

	public synchronized void setUnitPen(final Pen unitPen) {
		this.unitPen = unitPen;
	}

	public Pen getSubUnitPen() {
		return subUnitPen;
	}

	public synchronized void setSubUnitPen(final Pen subUnitPen) {
		this.subUnitPen = subUnitPen;
	}

	public DiagramAxis getXAxis() {
		return xAxis;
	}

	public DiagramAxis getYAxis() {
		return yAxis;
	}

	public double getZoom() {
		return zoom;
	}

	public synchronized void setZoom(final double zoom) {
		this.zoom = zoom;
	}

	@Override
	protected synchronized void paintComponent(final Graphics g) {
		final Rectangle clip = g.getClipBounds();
		final BufferedImage img = new BufferedImage(clip.width, clip.height,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = img.createGraphics();
		originalTransform = g2.getTransform();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setClip(0, 0, clip.width, clip.height);
		g2.setColor(backgroundColor);
		g2.fillRect(0, 0, clip.width, clip.height);
		g2.setFont(getFont().deriveFont(10f));

		final double[] minMax = new double[2];
		calculateAxis1(xAxis, true, minMax);
		final double minX = minMax[0];
		final double maxX = minMax[1];
		calculateAxis1(yAxis, false, minMax);
		final double minY = minMax[0];
		final double maxY = minMax[1];
		final int unitWidth1 = g2.getFontMetrics().stringWidth(
				String.format("%.2f %s", maxY, xAxis.getUnitName()));
		final int unitWidth2 = g2.getFontMetrics().stringWidth(
				String.format("%.2f %s", minY, xAxis.getUnitName()));
		final int unitWidth = Math.max(unitWidth1, unitWidth2);
		final int unitHeight = g2.getFontMetrics().getHeight();

		g2.translate(unitWidth, getHeight() - unitHeight);
		g2.scale(1, -1);
		final int w = getWidth() - 1 - unitWidth - BORDER;
		final int h = getHeight() - 1 - unitHeight - BORDER;

		final double[] pixels = new double[2];
		calculateAxis2(yAxis, h, minX, maxX, pixels);
		final double pixelPerUnitX = pixels[0];
		final double pixelPerSubX = pixels[1];
		calculateAxis2(xAxis, w, minY, maxY, pixels);
		final double pixelPerUnitY = pixels[0];
		final double pixelPerSubY = pixels[1];
		drawAxis(g2, yAxis, true, pixelPerUnitX, pixelPerSubX, h, w, unitWidth);
		drawAxis(g2, xAxis, false, pixelPerUnitY, pixelPerSubY, w, h,
				unitHeight);
		axisPen.assignTo(g2);
		g2.drawLine(0, 0, w, 0);
		g2.drawLine(0, 0, 0, h);

		// draw data-sets
		g2.clipRect(0, 0, w + BORDER, h + BORDER);
		int idx = 0;
		final double y0c = yAxis.isReversed() ? h - yAxis.getOffset()
				* pixelPerUnitX : yAxis.getOffset() * pixelPerUnitX;
		for (final DiagramDataSet ds : dataSets) {
			if (!ds.isValid()) continue;
			ds.prepare();
			double xold = 0;
			double yold = 0;
			boolean first = true;
			final Pen pen = detectPen(ds, idx);
			pen.assignTo(g2);
			for (final Point2D.Double pt : ds.getPoints()) {
				final double xu = pt.x / ds.getValuePerUnitX()
						+ xAxis.getOffset();
				final double yu = pt.y / ds.getValuePerUnitY()
						+ yAxis.getOffset();
				final double x = xu * pixelPerUnitY;
				final double y = yu * pixelPerUnitX;
				final double xc = xAxis.isReversed() ? w - x : x;
				final double yc = yAxis.isReversed() ? h - y : y;
				switch (ds.getType()) {
				default:
				case LineDiagram:
					if (!first)
						g2.drawLine((int) xold, (int) yold, (int) xc, (int) yc);
					break;
				case BarDiagram:
					g2.fillRect((int) xc - 1, (int) y0c, 3, (int) (yc - y0c));
					break;
				case ScatterPlotDiagram:
					g2.drawOval((int) xc - 1, (int) yc - 1, 2, 2);
					break;
				case IntersectionDiagram:
					g2.fillRect((int) xc - 1, 0, 3, h);
					final int xp = (int) (xc + pen.getStroke().getLineWidth() + 3);
					final String str = String.format("%f", pt.y);
					final Rectangle bounds = new Rectangle(xp, 2, w - xp, h - 4);
					drawText(g2, bounds, AlignH.Left, AlignV.Center, str);
					if (h >= 200) {
						drawText(g2, bounds, AlignH.Left, AlignV.Top, str);
						drawText(g2, bounds, AlignH.Left, AlignV.Bottom, str);
					}
					break;
				}
				xold = xc;
				yold = yc;
				first = false;
			}
			++idx;
		}

		// draw legend
		g2.setFont(getFont().deriveFont(18f));
		final int fh = Math.abs(g2.getFontMetrics().getHeight());
		final int legendHeight = fh * dataSets.size() + 2;
		int legendWidth = 0;
		for (final DiagramDataSet ds : dataSets)
			legendWidth = Math.max(legendWidth, g2.getFontMetrics()
					.stringWidth(ds.getLabel()));
		legendWidth += 4;
		g2.setTransform(originalTransform);
		g2.translate(getWidth() - BORDER - legendWidth + 1, BORDER + 1);
		final Rectangle legendRect = new Rectangle(0, 0, legendWidth,
				legendHeight);
		g2.setPaint(new Color(backgroundColor.getRed(), backgroundColor
				.getGreen(), backgroundColor.getBlue(), 128));
		g2.fill(legendRect);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(2.0f));
		g2.draw(legendRect);
		g2.clip(legendRect);
		idx = 0;
		legendRect.width -= 2;
		for (final DiagramDataSet ds : dataSets) {
			detectPen(ds, idx).assignTo(g2);
			legendRect.y += fh;
			legendRect.height -= fh;
			drawText(g2, legendRect, AlignH.Right, AlignV.Bottom, ds.getLabel());
			++idx;
		}

		g.drawImage(img, clip.x, clip.y, null);
	}

	private void drawText(final Graphics2D g2, final Rectangle bounds,
			final AlignH alignH, final AlignV alignV, final String str) {
		final float w;
		final float h;
		if (alignH == AlignH.Left && alignV == AlignV.Bottom) {
			w = 0;
			h = 0;
		} else {
			final Rectangle2D sb = g2.getFontMetrics().getStringBounds(str, g2);
			w = (float) sb.getWidth();
			h = Math.abs(g2.getFontMetrics().getAscent());
			// must be absolute because of possible negative scaling
		}
		final float x;
		final float y;
		switch (alignH) {
		case Left:
			x = bounds.x;
			break;
		case Center:
			x = bounds.x + (bounds.width - w) / 2;
			break;
		case Right:
			x = bounds.x + bounds.width - w;
			break;
		default:
			throw new IllegalArgumentException("AlignH constant unknown");
		}
		switch (alignV) {
		case Top:
			y = bounds.y + bounds.height - h;
			break;
		case Center:
			y = bounds.y + (bounds.height - h) / 2;
			break;
		case Bottom:
			y = bounds.y;
			break;
		default:
			throw new IllegalArgumentException("AlignV constant unknown");
		}
		final AffineTransform at = g2.getTransform();
		final Point2D.Float p = (Point2D.Float) at.transform(new Point2D.Float(
				x, y), new Point2D.Float());
		g2.setTransform(originalTransform);
		g2.drawString(str, p.x, p.y);
		g2.setTransform(at);
	}

	private void calculateAxis1(final DiagramAxis axis, final boolean isXAxis,
			final double[] minMax) {
		minMax[0] = axis.getMin();
		minMax[1] = axis.getMax();
		if (minMax[1] >= Double.MAX_VALUE || minMax[0] <= Double.MIN_VALUE) {
			double low = Double.MAX_VALUE, high = Double.MIN_VALUE;
			for (final DiagramDataSet ds : dataSets) {
				if (isXAxis && ds.getType() == DiagramType.IntersectionDiagram)
					continue;
				if (!ds.isValid()) continue;
				for (final Point2D.Double pt : ds.getPoints()) {
					double val;
					if (isXAxis)
						val = pt.y / ds.getValuePerUnitY() + axis.getOffset();
					else
						val = pt.x / ds.getValuePerUnitX() - axis.getOffset();
					low = Math.min(low, val);
					high = Math.max(high, val);
				}
			}
			if (minMax[0] <= Double.MIN_VALUE) minMax[0] = low;
			if (minMax[1] >= Double.MAX_VALUE) minMax[1] = high;
		}
	}

	private void calculateAxis2(final DiagramAxis axis, final int availPixels,
			final double min, final double max, final double[] pixels) {
		pixels[0] = availPixels / (max - min) * zoom;
		pixels[1] = axis.getSubsPerUnit() > 0 ? pixels[0]
				/ axis.getSubsPerUnit() : 0;
	}

	private void drawAxisLine(final Graphics2D g2, final boolean vertical,
			final double pos, final int len) {
		if (vertical)
			g2.drawLine(1, (int) pos, len - 1, (int) pos);
		else
			g2.drawLine((int) pos, 1, (int) pos, len - 1);
	}

	private void drawAxisText(final Graphics2D g2, final boolean vertical,
			final double pos, final int unitSpace, final double unitSize,
			final double val, final String unitName) {
		final double begin = pos - unitSize / 2;
		drawText(g2, vertical ? new Rectangle(-unitSpace, (int) begin,
				unitSpace, (int) unitSize) : new Rectangle((int) begin,
				-unitSpace, (int) unitSize, unitSpace), vertical ? AlignH.Right
				: AlignH.Center, vertical ? AlignV.Center : AlignV.Top, String
				.format("%.2f %s", val, unitName));
	}

	private void drawAxis(final Graphics2D g2, final DiagramAxis axis,
			final boolean vertical, final double pixelPerUnit,
			final double pixelPerSub, final int axisLen,
			final int axisThickness, final int unitSpace) {
		if (pixelPerUnit <= MIN_GRID_DELTA) return;
		for (double i = 0; i <= axisLen + BORDER; i += pixelPerUnit) {
			unitPen.assignTo(g2);
			final double ic = axis.isReversed() ? axisLen - i : i;
			drawAxisLine(g2, vertical, ic, axisThickness);
			drawAxisText(g2, vertical, ic, unitSpace, pixelPerUnit, i
					/ pixelPerUnit - axis.getOffset(), axis.getUnitName());
			if (pixelPerSub > MIN_GRID_DELTA && i <= axisLen
					&& axis.getSubsPerUnit() > 1) {
				subUnitPen.assignTo(g2);
				for (int s = 1; s < axis.getSubsPerUnit(); ++s)
					drawAxisLine(g2, vertical, axis.isReversed() ? ic - s
							* pixelPerSub : ic + s * pixelPerSub, axisThickness);
			}
		}
	}

	private Pen detectPen(final DiagramDataSet dds, final int idx) {
		if (!dds.isPenAutomatic()) return dds.getPen();
		Color color = backgroundColor;
		int v = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		if (idx >= DEFAULT_COLORS.length) {
			v = v < 64 || v > 192 ? 255 - v : v < 128 ? 255 : 0;
			return new Pen(new Color(v, v, v), 2.0f);
		}
		color = DEFAULT_COLORS[idx];
		if (v > 128) // use darker colors
			color = new Color((int) (color.getRed() / MAKE_DARKER),
					(int) (color.getGreen() / MAKE_DARKER), (int) (color
							.getBlue() / MAKE_DARKER));
		return new Pen(color, 2.0f);
	}
}
