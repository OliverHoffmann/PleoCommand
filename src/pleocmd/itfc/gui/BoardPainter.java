package pleocmd.itfc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import pleocmd.ImmutableRectangle;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

final class BoardPainter {

	/**
	 * Configuration icon.
	 */
	static final Icon ICON_CONF = IconLoader.getIcon("configure");

	/**
	 * Visualization icon.
	 */
	static final Icon ICON_DGR = IconLoader.getIcon("games-difficult");

	/**
	 * The Stroke used to print one Pipe-Flow symbol
	 */
	public static final BasicStroke FLOW_STROKE = new BasicStroke(
			BoardConfiguration.CFG_FLOW_WIDTH.getContent().floatValue(),
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1);

	// Miscellaneous

	private final Set<PipePart> set;

	private final Map<PipePart, String> saneConfigCache;

	private final Dimension bounds = new Dimension();

	private int border1;

	private int border2;

	private double scale = 1.0;

	private int grayVal = 128;

	private Pipe pipe;

	public BoardPainter() {
		set = new HashSet<PipePart>();
		saneConfigCache = new HashMap<PipePart, String>();
	}

	// CS_IGNORE_BEGIN class meant as struct

	public static class PaintParameters {
		public Graphics g;
		public PipePart currentPart;
		public PipePart underCursor;
		public Rectangle currentConnection;
		public PipePart currentConnectionsTarget;
		public boolean currentConnectionValid;
		public BoardAutoLayouter layouter;
		public boolean modifyable;
		public Collection<PipeFlow> pipeflow;
	}

	// CS_IGNORE_END

	public void paint(final PaintParameters p) {
		if (pipe == null || scale <= Double.MIN_NORMAL) return;
		final Rectangle clip = p.g.getClipBounds();
		if (clip == null) return;
		final BufferedImage img = new BufferedImage(clip.width, clip.height,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setClip(0, 0, clip.width, clip.height);

		final Rectangle clipOrg = new Rectangle((int) (clip.x / scale),
				(int) (clip.y / scale), (int) (clip.width / scale),
				(int) (clip.height / scale));

		final long start = System.currentTimeMillis();
		if (BoardConfiguration.CFG_PAINT_DEBUG.getContent()) {
			grayVal = (grayVal - 118) % 64 + 128;
			g2.setColor(new Color(grayVal, grayVal, grayVal));
		} else
			g2.setColor(BoardConfiguration.CFG_BACKGROUND.getContent());
		g2.fillRect(0, 0, clip.width, clip.height);
		g2.translate(-clip.x, -clip.y);
		g2.scale(scale, scale);
		drawMovementHint(g2, p.currentPart);
		final long time1 = System.currentTimeMillis();

		if (clip.x < border1) drawOrderingHint(g2);
		final long time2 = System.currentTimeMillis();
		drawSectionBorders(g2);
		final long time3 = System.currentTimeMillis();
		final int cnt4 = drawPipeParts(g2, clipOrg, p.currentPart,
				p.underCursor, p.modifyable);
		final long time4 = System.currentTimeMillis();
		final int cnt5 = drawConnections(g2, clipOrg, p.currentPart,
				p.currentConnection == null ? null : new ImmutableRectangle(
						p.currentConnection), p.underCursor,
				p.currentConnectionsTarget, p.currentConnectionValid);
		final long time5 = System.currentTimeMillis();
		drawAutoLayoutInfo(g2, p.layouter);
		drawPipeFlow(g2, p.pipeflow);

		if (BoardConfiguration.CFG_PAINT_DEBUG.getContent()) {
			g2.translate(clip.x / scale, clip.y / scale);
			drawDebugTime(g2, time1 - start, -1, "Background", 1);
			drawDebugTime(g2, time2 - time1, -1, "Hint", 2);
			drawDebugTime(g2, time3 - time2, -1, "Border", 3);
			drawDebugTime(g2, time4 - time3, cnt4, "Parts", 4);
			drawDebugTime(g2, time5 - time4, cnt5, "Conn's", 5);
		}

		p.g.drawImage(img, clip.x, clip.y, null);
	}

	private void drawDebugTime(final Graphics2D g2, final long elapsed,
			final int count, final String name, final int pos) {
		final Font f = g2.getFont();
		g2.setFont(f.deriveFont(10f));
		g2.setColor(Color.GREEN);
		final String str = count == -1 ? name : String.format("%d %s", count,
				name);
		g2.drawString(String.format("%d ms for %s", elapsed, str), 0, 10 * pos);
		g2.setFont(f);
	}

	private void drawMovementHint(final Graphics2D g2,
			final PipePart currentPart) {
		final List<? extends PipePart> list;
		final int x0;
		final int x1;
		if (currentPart instanceof Input) {
			list = pipe.getInputList();
			x0 = 0;
			x1 = border1;
		} else if (currentPart instanceof Converter) {
			list = pipe.getConverterList();
			x0 = border1;
			x1 = border2;
		} else if (currentPart instanceof Output) {
			list = pipe.getOutputList();
			x0 = border2;
			x1 = (int) (bounds.width / scale);
		} else
			return;
		final int idx = list.indexOf(currentPart);
		final PipePart before = idx > 0 ? list.get(idx - 1) : null;
		final PipePart after = idx < list.size() - 1 ? list.get(idx + 1) : null;

		final int y0 = before == null ? 0 : before.getGuiPosition().getY()
				+ before.getGuiPosition().getHeight() + 1;
		final int y1 = after == null ? (int) (bounds.height / scale) : after
				.getGuiPosition().getY() - 1;
		final Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
		if (BoardConfiguration.CFG_PAINT_DEBUG.getContent())
			g2.setColor(new Color(grayVal, grayVal + 16, grayVal));
		else
			g2.setColor(BoardConfiguration.CFG_MOVEMENT_HINT.getContent());
		g2.fill(r);
	}

	private void drawOrderingHint(final Graphics2D g2) {
		final double w = border1;
		final double h = bounds.height / scale;
		final int tw = (int) (w * BoardConfiguration.CFG_ORDER_HINT_TRUNK_WIDTH
				.getContent());
		final int th = (int) (h * BoardConfiguration.CFG_ORDER_HINT_TRUNK_HEIGHT
				.getContent());
		final int aw = (int) (w * BoardConfiguration.CFG_ORDER_HINT_ARROW_WIDTH
				.getContent());
		final int ah = (int) (h * BoardConfiguration.CFG_ORDER_HINT_ARROW_HEIGHT
				.getContent());
		final int cw = tw + 2 * aw;
		final int ch = th + ah;
		final int ow = (int) ((w - cw) / 2);
		final int oh = (int) ((h - ch) / 2);
		final Polygon p = new Polygon();
		p.addPoint(ow + aw, oh);
		p.addPoint(ow + aw + tw, oh);
		p.addPoint(ow + aw + tw, oh + th);
		p.addPoint(ow + aw + tw + aw, oh + th);
		p.addPoint(ow + aw + tw / 2, oh + th + ah);
		p.addPoint(ow, oh + th);
		p.addPoint(ow + aw, oh + th);
		p.addPoint(ow + aw, oh);

		if (BoardConfiguration.CFG_SHADOW_ORDERHINT.getContent()
				&& BoardConfiguration.CFG_SHADOW_DEPTH.getContent() > 0) {
			final AffineTransform at = g2.getTransform();
			g2.translate(BoardConfiguration.CFG_SHADOW_DEPTH.getContent(),
					BoardConfiguration.CFG_SHADOW_DEPTH.getContent());
			g2.setColor(BoardConfiguration.CFG_SHADOW_COLOR.getContent());
			g2.fillPolygon(p);
			g2.setTransform(at);
		}

		g2.setColor(BoardConfiguration.CFG_ORDER_HINT_BACK.getContent());
		g2.fillPolygon(p);
	}

	private void drawSectionBorders(final Graphics2D g2) {
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
		g2.setColor(BoardConfiguration.CFG_SECT_BORDER.getContent());
		final int h = (int) (bounds.height / scale + 0.5);
		g2.drawLine(border1, 0, border1, h);
		g2.drawLine(border2, 0, border2, h);
	}

	private int drawPipeParts(final Graphics2D g2, final Rectangle clip,
			final PipePart currentPart, final PipePart underCursor,
			final boolean modifyable) {
		int cnt = 0;
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL, 0, null, 0));
		for (final PipePart pp : set)
			cnt += drawPipePart(g2, pp, pp == underCursor, clip, currentPart,
					modifyable);
		return cnt;
	}

	private int drawConnections(final Graphics2D g2, final Rectangle clip,
			final PipePart currentPart,
			final ImmutableRectangle currentConnection,
			final PipePart underCursor,
			final PipePart currentConnectionsTarget,
			final boolean currentConnectionValid) {
		int cnt = 0;
		for (final PipePart src : set)
			for (final PipePart trg : src.getConnectedPipeParts()) {
				final boolean sel = currentPart == src
						&& currentConnectionsTarget == trg;
				if (saneConfigCache.get(src) == null)
					g2
							.setColor(sel ? BoardConfiguration.CFG_CONNECTION_SEL_OK
									.getContent()
									: BoardConfiguration.CFG_CONNECTION_OK
											.getContent());
				else
					g2.setColor(sel ? BoardConfiguration.CFG_CONNECTION_SEL_BAD
							.getContent()
							: BoardConfiguration.CFG_CONNECTION_BAD
									.getContent());
				cnt += drawConnection(g2, src.getGuiPosition(), trg
						.getGuiPosition(), src, trg, clip);
			}
		if (currentConnection != null && currentConnectionsTarget == null) {
			g2
					.setColor(currentConnectionValid ? BoardConfiguration.CFG_CONNECTION_SEL_OK
							.getContent()
							: BoardConfiguration.CFG_CONNECTION_SEL_BAD
									.getContent());
			cnt += drawConnection(g2, currentPart.getGuiPosition(),
					currentConnection, currentPart, underCursor, clip);
		}
		return cnt;
	}

	private int drawPipePart(final Graphics2D g2, final PipePart part,
			final boolean hover, final Rectangle clip,
			final PipePart currentPart, final boolean modifyable) {
		final ImmutableRectangle rect = part.getGuiPosition();
		if (BoardConfiguration.CFG_SHADOW_RECTS.getContent()
				&& BoardConfiguration.CFG_SHADOW_DEPTH.getContent() > 0) {
			final Rectangle r = rect.createCopy();
			r.width += BoardConfiguration.CFG_SHADOW_DEPTH.getContent();
			r.height += BoardConfiguration.CFG_SHADOW_DEPTH.getContent();
			if (!r.intersects(clip)) return 0;
		} else if (!rect.intersects(clip)) return 0;

		final Color outerClr;
		if (saneConfigCache.get(part) == null)
			outerClr = part == currentPart ? BoardConfiguration.CFG_OUTER_SEL_OK
					.getContent()
					: BoardConfiguration.CFG_OUTER_OK.getContent();
		else
			outerClr = part == currentPart ? BoardConfiguration.CFG_OUTER_SEL_BAD
					.getContent()
					: BoardConfiguration.CFG_OUTER_BAD.getContent();

		if (BoardConfiguration.CFG_SHADOW_RECTS.getContent()
				&& BoardConfiguration.CFG_SHADOW_DEPTH.getContent() > 0) {
			final AffineTransform at = g2.getTransform();
			g2.translate(BoardConfiguration.CFG_SHADOW_DEPTH.getContent(),
					BoardConfiguration.CFG_SHADOW_DEPTH.getContent());
			g2.setColor(BoardConfiguration.CFG_SHADOW_COLOR.getContent());
			rect.fill(g2);
			g2.setTransform(at);
		}

		g2.setColor(BoardConfiguration.CFG_RECT_BACKGROUND.getContent());
		rect.fill(g2);

		g2.setColor(outerClr);
		rect.draw(g2);

		final String s = BoardConfiguration.CFG_DRAW_SHORTCONFIG.getContent() ? part
				.getShortConfigDescr()
				: part.getName();
		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);

		if (hover) {
			final Rectangle inner = rect.createCopy();
			inner.grow(-BoardConfiguration.CFG_ICON_WIDTH.getContent()
					- BoardConfiguration.CFG_INNER_WIDTH.getContent(),
					-BoardConfiguration.CFG_INNER_HEIGHT.getContent());
			g2.setColor(modifyable ? BoardConfiguration.CFG_INNER_MODIFIABLE
					.getContent() : BoardConfiguration.CFG_INNER_READONLY
					.getContent());
			g2.fill(inner);
		}

		// restrict drawing to inside the rectangle
		final Shape shape = g2.getClip();
		rect.asClip(g2);

		// draw main icon
		Icon mainIcon = part.getIcon();
		if (mainIcon == null) {
			final BufferedImage img = new BufferedImage(rect.getHeight(), rect
					.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Icon cfgImage = part.getConfigImage();
			if (cfgImage instanceof ImageIcon) {
				final Graphics2D g = img.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.drawImage(((ImageIcon) cfgImage).getImage(), 0, 0, rect
						.getHeight(), rect.getHeight(), null);
				mainIcon = new ImageIcon(img);
			} else
				mainIcon = IconLoader.getMissingIcon();
		}
		mainIcon.paintIcon(null, g2, rect.getX(), rect.getY()
				+ (rect.getHeight() - mainIcon.getIconHeight()) / 2);

		// draw name of PipePart
		g2.setColor(outerClr);
		g2.drawString(s, (float) (rect.getX() + (rect.getWidth() - sb
				.getWidth()) / 2), (float) (rect.getY() + sb.getHeight()));

		// draw clickable icons
		// TODO ENH hover should be per icon here
		drawIcon(g2, hover, rect, ICON_CONF,
				BoardConfiguration.CFG_ICON_CONF_POS.getContent(), !modifyable
						|| part.getGuiConfigs().isEmpty(), false, modifyable);
		drawIcon(g2, hover, rect, ICON_DGR, BoardConfiguration.CFG_ICON_DGR_POS
				.getContent(), false, part.isVisualize(), modifyable);

		// restore old clip
		g2.setClip(shape);
		return 1;
	}

	/**
	 * Draws an icon to the image
	 * 
	 * @param g2
	 *            {@link Graphics2D} of the image
	 * @param hover
	 *            if true, an outline will be drawn
	 * @param rect
	 *            position of the image in which to align the icon
	 * @param icon
	 *            {@link Icon} to draw
	 * @param pos
	 *            the position inside the rectangle as follow<br>
	 *            [ 1 2 3 ..... -2 -1 0 ]
	 * @param disabled
	 *            if true, icon is drawn in disabled state
	 * @param selected
	 *            if true, icon is drawn in selected state
	 * @param modifyable
	 *            determines the color of the icon's background
	 */
	private void drawIcon(final Graphics2D g2, final boolean hover,
			final ImmutableRectangle rect, final Icon icon, final int pos,
			final boolean disabled, final boolean selected,
			final boolean modifyable) {
		final Rectangle b = getIconBounds(rect, icon, pos);
		if (hover) {
			g2.setColor(modifyable ? BoardConfiguration.CFG_INNER_MODIFIABLE
					.getContent() : BoardConfiguration.CFG_INNER_READONLY
					.getContent());
			g2.fill(b);
		}
		Icon ico = disabled ? (selected ? UIManager.getLookAndFeel()
				.getDisabledSelectedIcon(null, icon) : UIManager
				.getLookAndFeel().getDisabledIcon(null, icon)) : icon;
		if (ico == null) ico = icon;
		ico.paintIcon(null, g2, b.x, b.y);
		if (hover)
			g2
					.setColor(selected ? BoardConfiguration.CFG_ICON_OUTLINE_SEL_HOVER
							.getContent()
							: BoardConfiguration.CFG_ICON_OUTLINE_HOVER
									.getContent());
		else
			g2.setColor(selected ? BoardConfiguration.CFG_ICON_OUTLINE_SEL
					.getContent() : BoardConfiguration.CFG_ICON_OUTLINE
					.getContent());
		g2.draw3DRect(b.x, b.y, b.width, b.height, !selected);
	}

	private int drawConnection(final Graphics2D g2,
			final ImmutableRectangle srcRect, final ImmutableRectangle trgRect,
			final PipePart src, final PipePart trg, final Rectangle clip) {
		if (!srcRect.union(trgRect.createCopy()).intersects(clip)) return 0;

		final Point srcPoint = new Point();
		final Point trgPoint = new Point();
		calcConnectorPositions(srcRect, trgRect, srcPoint, trgPoint);
		final Polygon arrow = getArrowPolygon(srcPoint.x, srcPoint.y,
				trgPoint.x, trgPoint.y);

		if (BoardConfiguration.CFG_SHADOW_CONNECTIONS.getContent()
				&& BoardConfiguration.CFG_SHADOW_DEPTH.getContent() > 0) {
			final Color clr = g2.getColor();
			final AffineTransform at = g2.getTransform();
			g2.translate(BoardConfiguration.CFG_SHADOW_DEPTH.getContent() / 2,
					BoardConfiguration.CFG_SHADOW_DEPTH.getContent() / 2);
			g2.setColor(BoardConfiguration.CFG_SHADOW_COLOR.getContent());
			g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
			g2.fillPolygon(arrow);
			drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x,
					trgPoint.y, BoardConfiguration.CFG_SHADOW_CONNECTIONS_LABEL
							.getContent() ? src.getOutputDescription() : "");
			drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x,
					srcPoint.y, trg == null
							|| !BoardConfiguration.CFG_SHADOW_CONNECTIONS_LABEL
									.getContent() ? "" : trg
							.getInputDescription());
			g2.setTransform(at);
			g2.setColor(clr);
		}

		g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
		g2.fillPolygon(arrow);

		drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y,
				src.getOutputDescription());
		drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x, srcPoint.y,
				trg == null ? "" : trg.getInputDescription());
		return 1;
	}

	private void drawConnectorLabel(final Graphics2D g2, final int sx,
			final int sy, final int tx, final int ty, final String str) {
		if (str.isEmpty()) return;

		// draw in image
		final Rectangle sb = g2.getFontMetrics().getStringBounds(str, g2)
				.getBounds();
		final int sw = (int) (sb.width * scale);
		final int sh = (int) (sb.height * scale);
		if (sw <= 0 || sh <= 0) return;
		final BufferedImage img = new BufferedImage(sw, sh,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imgG2D = img.createGraphics();
		imgG2D.scale(scale, scale);
		imgG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (BoardConfiguration.CFG_PAINT_DEBUG.getContent()) {
			imgG2D.setColor(Color.YELLOW);
			imgG2D.fillRect(0, 0, sb.width, sb.height);
		}
		imgG2D.setColor(g2.getColor());
		imgG2D.setFont(g2.getFont());
		imgG2D.drawString(str, 0, -sb.y);

		// make sure text is never bottom-up and correctly positioned
		double d = Math.atan2(sy - ty, sx - tx);
		int xoffs = 16;
		if (d > Math.PI / 2)
			d -= Math.PI;
		else if (d < -Math.PI / 2.0)
			d += Math.PI;
		else
			xoffs = (int) (-sb.width * scale - xoffs);

		// draw rotated image
		final Shape shape = g2.getClip();
		final AffineTransform at = g2.getTransform();

		// only use translation from original transformation
		// scaling is already done in imgG2D
		g2.setTransform(new AffineTransform());
		g2.translate(at.getTranslateX() + sx * scale, at.getTranslateY() + sy
				* scale);
		g2.rotate(d);
		final int len = (int) (Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
				* (sy - ty)) * scale);
		if (xoffs < 0)
			g2.clipRect(-len / 2, 0, len / 2, sh);
		else
			g2.clipRect(0, 0, len / 2, sh);
		g2.drawImage(img, new AffineTransformOp(new AffineTransform(),
				AffineTransformOp.TYPE_BILINEAR), xoffs, 0);
		g2.setTransform(at);
		g2.setClip(shape);
	}

	private void drawAutoLayoutInfo(final Graphics2D g2,
			final BoardAutoLayouter layouter) {
		if (layouter == null) return;
		final int w = bounds.width * 2 / 3;
		final int h = 20;
		final int x = (bounds.width - w) / 2;
		final int y = (bounds.height - h) / 2;
		g2.setColor(Color.LIGHT_GRAY);
		g2.fill3DRect(x, y, w, h, false);
		g2.setColor(new Color(128, 128, 255));
		g2.fill3DRect(x + 1, y + 1, (int) (w * layouter.getProgress()) - 1,
				h - 2, true);
		g2.setColor(Color.BLACK);
		final String s = layouter.getProgressText();
		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);
		g2.drawString(s, (int) ((bounds.width - sb.getWidth()) / 2), (int) (y
				+ h - (h - sb.getHeight()) / 2));
	}

	private void drawPipeFlow(final Graphics2D g2,
			final Collection<PipeFlow> pipeflow) {
		if (pipeflow != null) synchronized (pipeflow) {
			g2.setStroke(FLOW_STROKE);
			for (final PipeFlow pf : pipeflow)
				pf.draw(g2);
		}
	}

	public static void calcConnectorPositions(final ImmutableRectangle srcRect,
			final ImmutableRectangle trgRect, final Point srcPos,
			final Point trgPos) {
		// calculate center of source and target
		final int csx = srcRect.getX() + srcRect.getWidth() / 2;
		final int csy = srcRect.getY() + srcRect.getHeight() / 2;
		final int ctx = trgRect.getX() + trgRect.getWidth() / 2;
		final int cty = trgRect.getY() + trgRect.getHeight() / 2;

		// determine side of rectangle for the connection
		final int dcx = ctx - csx;
		final int dcy = cty - csy;
		if (dcx > Math.abs(dcy)) {
			// target is right of source
			if (srcPos != null)
				srcPos.setLocation(srcRect.getX() + srcRect.getWidth(), csy);
			if (trgPos != null) trgPos.setLocation(trgRect.getX(), cty);
		} else if (dcy >= Math.abs(dcx)) {
			// target is below source
			if (srcPos != null)
				srcPos.setLocation(csx, srcRect.getY() + srcRect.getHeight());
			if (trgPos != null) trgPos.setLocation(ctx, trgRect.getY());
		} else if (dcx < 0 && Math.abs(dcx) > Math.abs(dcy)) {
			// target is left of source
			if (srcPos != null) srcPos.setLocation(srcRect.getX(), csy);
			if (trgPos != null)
				trgPos.setLocation(trgRect.getX() + trgRect.getWidth(), cty);
		} else {
			// target is above source
			if (srcPos != null) srcPos.setLocation(csx, srcRect.getY());
			if (trgPos != null)
				trgPos.setLocation(ctx, trgRect.getY() + trgRect.getHeight());
		}
	}

	// from http://www.java-forums.org/awt-swing/
	// 5842-how-draw-arrow-mark-using-java-swing.html
	public static Polygon getArrowPolygon(final int sx, final int sy,
			final int tx, final int ty) {
		final Polygon polygon = new Polygon();
		final double d = Math.atan2(sx - tx, sy - ty);
		// tip
		polygon.addPoint(tx, ty);
		// wing 1
		polygon.addPoint(tx
				+ xCor(BoardConfiguration.CFG_CONN_ARROW_HEAD.getContent(),
						d + .5), ty
				+ yCor(BoardConfiguration.CFG_CONN_ARROW_HEAD.getContent(),
						d + .5));
		// on line
		polygon.addPoint(tx
				+ xCor(BoardConfiguration.CFG_CONN_ARROW_WING.getContent(), d),
				ty
						+ yCor(BoardConfiguration.CFG_CONN_ARROW_WING
								.getContent(), d));
		// wing 2
		polygon.addPoint(tx
				+ xCor(BoardConfiguration.CFG_CONN_ARROW_HEAD.getContent(),
						d - .5), ty
				+ yCor(BoardConfiguration.CFG_CONN_ARROW_HEAD.getContent(),
						d - .5));
		// back to tip, close polygon
		polygon.addPoint(tx, ty);
		return polygon;
	}

	private static int xCor(final int len, final double dir) {
		return (int) (len * Math.sin(dir));
	}

	private static int yCor(final int len, final double dir) {
		return (int) (len * Math.cos(dir));
	}

	/**
	 * Gets the bounding rectangle around an icon.
	 * 
	 * @param rect
	 *            position of the image in which to align the icon
	 * @param icon
	 *            {@link Icon} which would be drawn
	 * @param pos
	 *            the position inside the rectangle as follow<br>
	 *            [ 1 2 3 ..... -2 -1 0 ]
	 * @return the bounding rectangle
	 */
	private static Rectangle getIconBounds(final ImmutableRectangle rect,
			final Icon icon, final int pos) {
		final boolean alignRight = pos <= 0;
		final Rectangle b = new Rectangle();
		b.width = icon.getIconWidth();
		b.height = icon.getIconHeight();
		if (alignRight)
			b.x = rect.getX() + rect.getWidth() - (1 - pos)
					* BoardConfiguration.CFG_ICON_WIDTH.getContent();
		else
			b.x = rect.getX() + pos
					* BoardConfiguration.CFG_ICON_WIDTH.getContent() - b.width;
		b.y = rect.getY() + rect.getHeight()
				- BoardConfiguration.CFG_ICON_WIDTH.getContent();
		return b;
	}

	public static Object getPipePartElement(final PipePart pp,
			final Point position) {
		final Rectangle inner = pp.getGuiPosition().createCopy();
		inner.grow(-BoardConfiguration.CFG_ICON_WIDTH.getContent()
				- BoardConfiguration.CFG_INNER_WIDTH.getContent(),
				-BoardConfiguration.CFG_INNER_HEIGHT.getContent());
		final Rectangle ibC = getIconBounds(pp.getGuiPosition(), ICON_CONF,
				BoardConfiguration.CFG_ICON_CONF_POS.getContent());
		final Rectangle ibD = getIconBounds(pp.getGuiPosition(), ICON_DGR,
				BoardConfiguration.CFG_ICON_DGR_POS.getContent());
		if (ibC.contains(position))
			return ICON_CONF;
		else if (ibD.contains(position))
			return ICON_DGR;
		else if (inner.contains(position))
			return new Point(position.x - pp.getGuiPosition().getX(),
					position.y - pp.getGuiPosition().getY());
		else
			return null;
	}

	public void setPipe(final Pipe pipe, final Graphics g,
			final boolean allowMoving) {
		this.pipe = pipe;
		set.clear();
		for (final PipePart pp : pipe.getInputList())
			addToSet(pp, g, allowMoving);
		for (final PipePart pp : pipe.getConverterList())
			addToSet(pp, g, allowMoving);
		for (final PipePart pp : pipe.getOutputList())
			addToSet(pp, g, allowMoving);
		updateSaneConfigCache0();
	}

	public Pipe getPipe() {
		return pipe;
	}

	void addToSet(final PipePart pp, final Graphics g, final boolean allowMoving) {
		set.add(pp);
		if (allowMoving) {
			final int txtWidth = Math.max((int) g.getFontMetrics()
					.getStringBounds(pp.getName(), g).getWidth(), (int) g
					.getFontMetrics().getStringBounds(pp.getShortConfigDescr(),
							g).getWidth());
			final Rectangle r = pp.getGuiPosition().createCopy();
			r.height = g.getFontMetrics().getHeight()
					+ BoardConfiguration.CFG_ICON_WIDTH.getContent() + 2;
			r.width = Math.min(BoardConfiguration.CFG_MAX_RECT_WIDTH
					.getContent(), Math.max(BoardConfiguration.CFG_ICON_WIDTH
					.getContent()
					* BoardConfiguration.CFG_ICON_MAX.getContent(), txtWidth
					+ r.height * 2));
			check(r, pp);
			pp.setGuiPosition(r);
		}
	}

	public Set<PipePart> getSet() {
		return set;
	}

	public Map<PipePart, String> getSaneConfigCache() {
		return saneConfigCache;
	}

	private boolean updateSaneConfigCache0() {
		final Map<PipePart, String> sane = pipe.getSanePipeParts();
		if (saneConfigCache.equals(sane)) return false;
		saneConfigCache.clear();
		saneConfigCache.putAll(sane);
		return true;
	}

	public boolean updateSaneConfigCache() {
		final boolean modified = updateSaneConfigCache0();
		if (modified) pipe.modified();
		return modified;
	}

	public Dimension getPreferredSize() {
		// get lower right corner
		int x = 0;
		int y = 0;
		for (final PipePart pp : set) {
			final ImmutableRectangle r = pp.getGuiPosition();
			x = Math.max(x, r.getX() + r.getWidth());
			y = Math.max(y, r.getY() + r.getHeight());
		}

		// add some free space and consider scaling
		return new Dimension(Math.max(BoardConfiguration.CFG_PREFSIZE_MIN
				.getContent(),
				(int) (x * scale + BoardConfiguration.CFG_PREFSIZE_FREE
						.getContent())), Math.max(
				BoardConfiguration.CFG_PREFSIZE_MIN.getContent(), (int) (y
						* scale + BoardConfiguration.CFG_PREFSIZE_FREE
						.getContent())));
	}

	public Dimension getBounds() {
		return bounds;
	}

	public void setBounds(final int width, final int height,
			final boolean allowMoving) {
		bounds.setSize(width, height);
		border1 = Math.min(width
				/ BoardConfiguration.CFG_SECTION_FRAG.getContent(),
				BoardConfiguration.CFG_MAX_RECT_WIDTH.getContent()
						+ BoardConfiguration.CFG_SECTION_SPACE.getContent());
		border2 = width - border1;
		if (allowMoving) for (final PipePart pp : set) {
			final Rectangle r = pp.getGuiPosition().createCopy();
			check(r, pp);
			pp.setGuiPosition(r);
		}
	}

	public int getBorder1(final boolean considerScaling) {
		return considerScaling ? (int) (border1 * scale) : border1;
	}

	public int getBorder2(final boolean considerScaling) {
		return considerScaling ? (int) (border2 * scale) : border2;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(final double scale) {
		this.scale = scale;
	}

	void check(final Rectangle r, final PipePart pp) {
		final int xMin;
		final int xMax;
		final int yMin = 1;
		final int yMax = (int) (bounds.height / scale) - 1;
		if (Input.class.isInstance(pp)) {
			xMin = 1;
			xMax = border1 - 1;
		} else if (Converter.class.isInstance(pp)) {
			xMin = border1 + 1;
			xMax = border2 - 1;
		} else if (Output.class.isInstance(pp)) {
			xMin = border2 + 1;
			xMax = (int) (bounds.getWidth() / scale) - 1;
		} else {
			xMin = 1;
			xMax = (int) (bounds.getWidth() / scale) - 1;
		}
		if (r.x < xMin) r.x = xMin;
		if (r.y < yMin) r.y = yMin;
		if (r.x + r.getWidth() > xMax) r.x = xMax - r.width;
		if (r.y + r.height > yMax) r.y = yMax - r.height;
		for (final PipePart other : set)
			if (other != pp && other.getGuiPosition().intersects(r)) {
				// move r, so it doesn't intersect anymore
				final ImmutableRectangle rO = other.getGuiPosition();
				final Rectangle i = rO.intersection(r);
				final int x0 = r.x + r.width / 2;
				final int y0 = r.y + r.height / 2;
				final int x1 = rO.getX() + rO.getWidth() / 2;
				final int y1 = rO.getY() + rO.getHeight() / 2;
				if (i.getWidth() < i.height && r.x - i.getWidth() >= xMin
						&& r.x + r.getWidth() + i.getWidth() <= xMax) {
					if (x0 > x1) // move right
						r.translate(i.width, 0);
					else
						// move left
						r.translate(-i.width, 0);
				} else if (y0 > y1) {
					if (r.getY() + r.height + i.height > yMax)
						// move up instead of down
						r.translate(0, i.height - r.height - rO.getHeight());
					else
						r.translate(0, i.height); // move down
				} else if (r.getY() - i.height < yMin)
					// move down instead of up
					r.translate(0, r.height + rO.getHeight() - i.height);
				else
					r.translate(0, -i.height); // move up
				// check bounds again
				// (overlapping is better than being out of bounds)
				if (r.x < xMin) r.x = xMin;
				if (r.getY() < yMin) r.y = yMin;
				if (r.x + r.getWidth() > xMax) r.x = xMax - r.width;
				if (r.getY() + r.height > yMax) r.y = yMax - r.height;
			}
	}

}
