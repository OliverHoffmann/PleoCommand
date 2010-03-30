package pleocmd.itfc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import pleocmd.Log;
import pleocmd.cfg.ConfigValue;
import pleocmd.exc.InternalException;
import pleocmd.exc.PipeException;
import pleocmd.exc.StateException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

// TODO auto ordering of PipeParts via A* algorithm
public final class PipeConfigBoard extends JPanel {

	public static final int DEF_RECT_WIDTH = 150;

	public static final int DEF_RECT_HEIGHT = 20;

	private static final boolean PAINT_DEBUG = false;

	private static final long serialVersionUID = -4525676341777864359L;

	private static final Color BACKGROUND = Color.LIGHT_GRAY;

	private static final Color ORDER_HINT_BACK = new Color(255, 255, 128);

	private static final Color SECT_BORDER = Color.BLACK;

	private static final Color RECT_BACKGROUND = new Color(255, 255, 255);

	private static final Color INNER_MODIFYABLE = new Color(200, 200, 255);

	private static final Color INNER_READONLY = Color.LIGHT_GRAY;

	private static final Color OUTER_OK = Color.BLACK;

	private static final Color OUTER_BAD = Color.RED;

	private static final Color OUTER_SEL_OK = Color.BLUE;

	private static final Color OUTER_SEL_BAD = Color.MAGENTA;

	private static final Color CONNECTION_OK = Color.BLACK;

	private static final Color CONNECTION_BAD = Color.RED;

	private static final Color CONNECTION_SEL_OK = Color.BLUE;

	private static final Color CONNECTION_SEL_BAD = Color.MAGENTA;

	private static final Color SHADOW_COLOR = Color.GRAY;

	private static final int SHADOW_DEPTH = 4;

	private static final boolean SHADOW_ORDERHINT = true;

	private static final boolean SHADOW_RECTS = true;

	private static final boolean SHADOW_CONNECTIONS = false;

	private static final int ARROW_TIP = 14;

	private static final int ARROW_WING = 8;

	private static final int INNER_WIDTH = 8;

	private static final int INNER_HEIGHT = 4;

	private static final double LINE_CLICK_DIST = 10;

	private static final int SECTION_FRAC = 5;

	private static final int SECTION_SPACE = 20;

	private static final double ORDER_HINT_TRUNK_WIDTH = 0.3;

	private static final double ORDER_HINT_TRUNK_HEIGHT = 0.65;

	private static final double ORDER_HINT_ARROW_WIDTH = 0.3;

	private static final double ORDER_HINT_ARROW_HEIGHT = 0.3;

	private static final int GROW_LABEL_REDRAW = 14;

	private final JPopupMenu menuInput;

	private final JPopupMenu menuConverter;

	private final JPopupMenu menuOutput;

	private int idxMenuAdd;

	private final Set<PipePart> set;

	private final Dimension bounds = new Dimension();

	private int border1;

	private int border2;

	private Point handlePoint;

	private PipePart currentPart;

	private Rectangle currentConnection;

	private PipePart currentConnectionsTarget;

	private boolean currentConnectionValid;

	private PipePart underCursor;

	private int grayVal = 128;

	private int idxMenuConfPart;

	private int idxMenuDelPart;

	private int idxMenuDelPartConn;

	private int idxMenuDelConn;

	private final Set<PipePart> saneConfigCache;

	private boolean modifyable;

	public PipeConfigBoard() {
		menuInput = createMenu("Input", Input.class);
		menuConverter = createMenu("Converter", Converter.class);
		menuOutput = createMenu("Output", Output.class);

		set = new HashSet<PipePart>();
		for (final PipePart pp : Pipe.the().getInputList())
			set.add(pp);
		for (final PipePart pp : Pipe.the().getConverterList())
			set.add(pp);
		for (final PipePart pp : Pipe.the().getOutputList())
			set.add(pp);

		saneConfigCache = new HashSet<PipePart>();
		updateSaneConfigCache();

		updateState();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.isPopupTrigger())
					showPopup(e);
				else {
					updateCurrent(e.getPoint());
					if (e.getModifiers() == InputEvent.BUTTON1_MASK
							&& e.getClickCount() == 2) configureCurrentPart();
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				// if (e.isPopupTrigger()) showPopup(e);
				releaseCurrent();
			}

			private void showPopup(final MouseEvent e) {
				updateCurrent(e.getPoint());
				showMenu(PipeConfigBoard.this, e.getX(), e.getY());
			}
		});

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (e.getModifiers() != InputEvent.BUTTON1_MASK) return;
				PipeConfigBoard.this.mouseDragged(e.getPoint());
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				PipeConfigBoard.this.mouseMoved(e.getPoint());
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				updateBounds(getWidth(), getHeight());
			}
		});
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	private JPopupMenu createMenu(final String name,
			final Class<? extends PipePart> clazz) {
		final JPopupMenu menu = new JPopupMenu();

		idxMenuAdd = menu.getSubElements().length;
		final JMenu menuAdd = new JMenu("Add " + name);
		menu.add(menuAdd);
		for (final Class<? extends PipePart> pp : PipePartDetection.ALL_PIPEPART)
			if (clazz.isAssignableFrom(pp)) {
				final JMenuItem item = new JMenuItem(pp.getSimpleName());
				menuAdd.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						addPipePart(pp);
					}
				});
			}
		menu.addSeparator();

		idxMenuConfPart = menu.getSubElements().length;
		final JMenuItem itemConfPart = new JMenuItem("Configure This PipePart");
		itemConfPart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				configureCurrentPart();
			}
		});
		menu.add(itemConfPart);

		idxMenuDelPart = menu.getSubElements().length;
		final JMenuItem itemDelPart = new JMenuItem("Delete This PipePart");
		itemDelPart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				removeCurrentPart();
			}
		});
		menu.add(itemDelPart);

		idxMenuDelPartConn = menu.getSubElements().length;
		final JMenuItem itemDelPartConn = new JMenuItem(
				"Delete Connections Of This PipePart");
		itemDelPartConn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				removeCurrentPartsConnections();
			}
		});
		menu.add(itemDelPartConn);

		idxMenuDelConn = menu.getSubElements().length;
		final JMenuItem itemDelConn = new JMenuItem("Delete This Connection");
		itemDelConn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				removeCurrentConnection();
			}
		});
		menu.add(itemDelConn);

		return menu;
	}

	protected void removeCurrentConnection() {
		if (currentPart != null && currentConnectionsTarget != null
				&& ensureModifyable()) {
			try {
				currentPart.disconnectFromPipePart(currentConnectionsTarget);
			} catch (final StateException e) {
				Log.error(e, "Cannot delete connection");
			}
			currentConnection = null;
			currentConnectionsTarget = null;
			currentConnectionValid = false;
			updateSaneConfigCache();
			repaint();
		}
	}

	protected void removeCurrentPartsConnections() {
		if (currentPart != null && ensureModifyable()) {
			final Set<PipePart> copy = new HashSet<PipePart>(currentPart
					.getConnectedPipeParts());
			try {
				for (final PipePart pp : copy)
					currentPart.disconnectFromPipePart(pp);
			} catch (final StateException e) {
				Log.error(e, "Cannot delete connections");
			}
			currentConnection = null;
			currentConnectionsTarget = null;
			currentConnectionValid = false;
			updateSaneConfigCache();
			repaint();
		}
	}

	protected void removeCurrentPart() {
		if (currentPart != null && ensureModifyable()) {
			try {
				for (final PipePart srcPP : set)
					if (srcPP.getConnectedPipeParts().contains(currentPart))
						srcPP.disconnectFromPipePart(currentPart);
			} catch (final StateException e) {
				Log.error(e, "Cannot delete connections");
			}
			set.remove(currentPart);
			try {
				if (currentPart instanceof Input)
					Pipe.the().removeInput((Input) currentPart);
				else if (currentPart instanceof Converter)
					Pipe.the().removeConverter((Converter) currentPart);
				else if (currentPart instanceof Output)
					Pipe.the().removeOutput((Output) currentPart);
				else
					throw new InternalException(
							"Invalid sub-class of PipePart '%s'", currentPart);
			} catch (final StateException e) {
				Log.error(e, "Cannot remove PipePart '%s'", currentPart);
			}
			currentPart = null;
			currentConnection = null;
			currentConnectionsTarget = null;
			currentConnectionValid = false;
			handlePoint = null;
			updateSaneConfigCache();
			repaint();
		}
	}

	protected void configureCurrentPart() {
		if (currentPart != null && !currentPart.getGuiConfigs().isEmpty()
				&& ensureModifyable()) {
			createConfigureDialog("Configure", currentPart, null);
			repaint();
		}
	}

	protected Set<PipePart> getSet() {
		return set;
	}

	protected void updateSaneConfigCache() {
		final Set<PipePart> sane = new HashSet<PipePart>();
		final Set<PipePart> visited = new HashSet<PipePart>();
		final Set<PipePart> deadLocked = new HashSet<PipePart>();
		for (final PipePart pp : set)
			if (pp instanceof Input)
				topDownCheck(pp, sane, visited, deadLocked);
		sane.removeAll(deadLocked);
		if (!saneConfigCache.equals(sane)) {
			saneConfigCache.clear();
			saneConfigCache.addAll(sane);
			repaint();
		}
	}

	/**
	 * Checks if the given {@link PipePart} is sane. Sane means that the
	 * {@link PipePart} can be reached from an {@link Input}, has a path to an
	 * {@link Output}, doesn't contain a dead-lock in it's path and is correctly
	 * configured.
	 * 
	 * @param pp
	 *            the {@link PipePart} to check
	 * @param sane
	 *            a set of sane PipeParts
	 * @param visited
	 *            a set of already visited {@link PipePart}s during the current
	 *            recursion (handled like a kind of stack) to detect dead-locks.
	 * @param deadLocked
	 *            a set of already detected dead-locks.
	 * @return true if an {@link Output} can be reached from the
	 *         {@link PipePart}.
	 */
	private boolean topDownCheck(final PipePart pp, final Set<PipePart> sane,
			final Set<PipePart> visited, final Set<PipePart> deadLocked) {
		if (visited.contains(pp)) {
			deadLocked.add(pp);
			return false;
		}
		boolean outputReached = pp instanceof Output;
		visited.add(pp);
		for (final PipePart ppSub : pp.getConnectedPipeParts())
			outputReached |= topDownCheck(ppSub, sane, visited, deadLocked);
		visited.remove(pp);
		if (outputReached && pp.isConfigurationSane()) sane.add(pp);
		return outputReached;
	}

	@Override
	public void paintComponent(final Graphics g) {
		final Rectangle clip = g.getClipBounds();
		final BufferedImage img = new BufferedImage(clip.width, clip.height,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setClip(0, 0, clip.width, clip.height);
		g2.translate(-clip.x, -clip.y);

		if (PAINT_DEBUG) {
			final long start = System.currentTimeMillis();

			grayVal = (grayVal - 118) % 64 + 128;
			g2.setColor(new Color(grayVal, grayVal, grayVal));
			g2.fillRect(clip.x, clip.y, clip.width, clip.height);
			final long time1 = System.currentTimeMillis();

			if (clip.x < border1) drawOrderingHint(g2);
			final long time2 = System.currentTimeMillis();
			drawSectionBorders(g2);
			final long time3 = System.currentTimeMillis();
			drawPipeParts(g2);
			final long time4 = System.currentTimeMillis();
			drawConnections(g2);
			final long time5 = System.currentTimeMillis();

			g2.translate(clip.x, clip.y);
			drawDebugTime(g2, time1 - start, 1);
			drawDebugTime(g2, time2 - time1, 2);
			drawDebugTime(g2, time3 - time2, 3);
			drawDebugTime(g2, time4 - time3, 4);
			drawDebugTime(g2, time5 - time4, 5);
		} else {
			g2.setColor(BACKGROUND);
			g2.fillRect(clip.x, clip.y, clip.width, clip.height);
			if (clip.x < border1) drawOrderingHint(g2);
			drawSectionBorders(g2);
			drawPipeParts(g2);
			drawConnections(g2);
		}
		g.drawImage(img, clip.x, clip.y, null);
	}

	private void drawDebugTime(final Graphics2D g2, final long elapsed,
			final int pos) {
		final Font f = g2.getFont();
		g2.setFont(f.deriveFont(10f));
		g2.setColor(Color.GREEN);
		g2.drawString(String.valueOf(elapsed), 0, 10 * pos);
		g2.setFont(f);
	}

	private void drawOrderingHint(final Graphics2D g2) {
		final int tw = (int) (border1 * ORDER_HINT_TRUNK_WIDTH);
		final int th = (int) (bounds.height * ORDER_HINT_TRUNK_HEIGHT);
		final int aw = (int) (border1 * ORDER_HINT_ARROW_WIDTH);
		final int ah = (int) (bounds.height * ORDER_HINT_ARROW_HEIGHT);
		final int cw = tw + 2 * aw;
		final int ch = th + ah;
		final int ow = (border1 - cw) / 2;
		final int oh = (bounds.height - ch) / 2;
		final Polygon p = new Polygon();
		p.addPoint(ow + aw, oh);
		p.addPoint(ow + aw + tw, oh);
		p.addPoint(ow + aw + tw, oh + th);
		p.addPoint(ow + aw + tw + aw, oh + th);
		p.addPoint(ow + aw + tw / 2, oh + th + ah);
		p.addPoint(ow, oh + th);
		p.addPoint(ow + aw, oh + th);
		p.addPoint(ow + aw, oh);

		if (SHADOW_ORDERHINT && SHADOW_DEPTH > 0) {
			final AffineTransform at = g2.getTransform();
			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
			g2.setColor(SHADOW_COLOR);
			g2.fillPolygon(p);
			g2.setTransform(at);
		}

		g2.setColor(ORDER_HINT_BACK);
		g2.fillPolygon(p);
	}

	private void drawSectionBorders(final Graphics2D g2) {
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 2, 2 }, 0));
		g2.setColor(SECT_BORDER);
		g2.drawLine(border1, 0, border1, bounds.height);
		g2.drawLine(border2, 0, border2, bounds.height);
	}

	private void drawPipeParts(final Graphics2D g2) {
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL, 0, null, 0));
		final Rectangle clip = new Rectangle( //
				-(int) g2.getTransform().getTranslateX(), //
				-(int) g2.getTransform().getTranslateY(), //
				(int) g2.getClipBounds().getWidth(), //
				(int) g2.getClipBounds().getHeight());
		for (final PipePart pp : set)
			drawPipePart(g2, pp, pp == underCursor, clip);
	}

	private void drawConnections(final Graphics2D g2) {
		final Rectangle clip = new Rectangle( //
				-(int) g2.getTransform().getTranslateX(), //
				-(int) g2.getTransform().getTranslateY(), //
				(int) g2.getClipBounds().getWidth(), //
				(int) g2.getClipBounds().getHeight());
		for (final PipePart src : set)
			for (final PipePart trg : src.getConnectedPipeParts()) {
				final boolean sel = currentPart == src
						&& currentConnectionsTarget == trg;
				if (saneConfigCache.contains(src))
					g2.setColor(sel ? CONNECTION_SEL_OK : CONNECTION_OK);
				else
					g2.setColor(sel ? CONNECTION_SEL_BAD : CONNECTION_BAD);
				drawConnection(g2, src.getGuiPosition(), trg.getGuiPosition(),
						src, trg, clip);
			}
		if (currentConnection != null && currentConnectionsTarget == null) {
			g2.setColor(currentConnectionValid ? CONNECTION_SEL_OK
					: CONNECTION_SEL_BAD);
			drawConnection(g2, currentPart.getGuiPosition(), currentConnection,
					currentPart, underCursor, clip);
		}
	}

	private void drawPipePart(final Graphics2D g2, final PipePart part,
			final boolean visibleInner, final Rectangle clip) {
		final Rectangle rect = part.getGuiPosition();
		if (!rect.intersects(clip)) return;

		final Color outerClr;
		if (saneConfigCache.contains(part))
			outerClr = part == currentPart ? OUTER_SEL_OK : OUTER_OK;
		else
			outerClr = part == currentPart ? OUTER_SEL_BAD : OUTER_BAD;

		if (SHADOW_RECTS && SHADOW_DEPTH > 0) {
			final AffineTransform at = g2.getTransform();
			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
			g2.setColor(SHADOW_COLOR);
			g2.fillRect(rect.x, rect.y, rect.width, rect.height);
			g2.setTransform(at);
		}

		g2.setColor(RECT_BACKGROUND);
		g2.fillRect(rect.x, rect.y, rect.width, rect.height);

		g2.setColor(outerClr);
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);

		final String s = part.getClass().getSimpleName();
		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);

		if (visibleInner) {
			final Rectangle inner = new Rectangle(rect);
			inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
			g2.setColor(modifyable ? INNER_MODIFYABLE : INNER_READONLY);
			g2.fillRect(inner.x, inner.y, inner.width, inner.height);
		}

		final Shape shape = g2.getClip();
		g2.clipRect(rect.x, rect.y, rect.width, rect.height);
		g2.setColor(outerClr);
		g2.drawString(s, (float) (rect.x + (rect.width - sb.getWidth()) / 2),
				(float) (rect.y + sb.getHeight() + (rect.height - sb
						.getHeight()) / 2));
		g2.setClip(shape);
	}

	private static void drawConnection(final Graphics2D g2,
			final Rectangle srcRect, final Rectangle trgRect,
			final PipePart src, final PipePart trg, final Rectangle clip) {
		if (!srcRect.union(trgRect).intersects(clip)) return;

		final Point srcPoint = new Point();
		final Point trgPoint = new Point();
		calcConnectorPositions(srcRect, trgRect, srcPoint, trgPoint);
		final Polygon arrow = getArrowPolygon(srcPoint.x, srcPoint.y,
				trgPoint.x, trgPoint.y);

		if (SHADOW_CONNECTIONS && SHADOW_DEPTH > 0) {
			final Color clr = g2.getColor();
			final AffineTransform at = g2.getTransform();
			g2.translate(SHADOW_DEPTH / 2, SHADOW_DEPTH / 2);
			g2.setColor(SHADOW_COLOR);
			g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
			g2.fillPolygon(arrow);
			drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x,
					trgPoint.y, src.getOutputDescription());
			drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x,
					srcPoint.y, trg == null ? "" : trg.getInputDescription());
			g2.setTransform(at);
			g2.setColor(clr);
		}

		g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
		g2.fillPolygon(arrow);

		drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y,
				src.getOutputDescription());
		drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x, srcPoint.y,
				trg == null ? "" : trg.getInputDescription());
	}

	private static void drawConnectorLabel(final Graphics2D g2, final int sx,
			final int sy, final int tx, final int ty, final String str) {
		if (str.isEmpty()) return;

		// draw in image
		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(str, g2);
		final BufferedImage img = new BufferedImage((int) sb.getWidth(),
				(int) sb.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D imgG2D = img.createGraphics();
		imgG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		imgG2D.setColor(g2.getColor());
		imgG2D.setFont(g2.getFont());
		imgG2D.drawString(str, 0, img.getHeight());

		// make sure text is never bottom-up and correctly positioned
		double d = Math.atan2(sy - ty, sx - tx);
		int xoffs = 16;
		if (d > Math.PI / 2)
			d -= Math.PI;
		else if (d < -Math.PI / 2.0)
			d += Math.PI;
		else
			xoffs = -img.getWidth() - xoffs;

		// draw rotated image
		final Shape shape = g2.getClip();
		final AffineTransform at = g2.getTransform();
		g2.translate(sx, sy);
		g2.rotate(d);
		final int len = (int) Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
				* (sy - ty));
		if (xoffs < 0)
			g2.clipRect(-len / 2, 0, len / 2, img.getHeight());
		else
			g2.clipRect(0, 0, len / 2, img.getHeight());
		g2.drawImage(img, new AffineTransformOp(new AffineTransform(),
				AffineTransformOp.TYPE_BILINEAR), xoffs, 0);
		g2.setTransform(at);
		g2.setClip(shape);
	}

	private static void calcConnectorPositions(final Rectangle srcRect,
			final Rectangle trgRect, final Point srcPos, final Point trgPos) {
		// calculate center of source and target
		final int csx = srcRect.x + srcRect.width / 2;
		final int csy = srcRect.y + srcRect.height / 2;
		final int ctx = trgRect.x + trgRect.width / 2;
		final int cty = trgRect.y + trgRect.height / 2;

		// determine side of rectangle for the connection
		final int dcx = ctx - csx;
		final int dcy = cty - csy;
		if (dcx > Math.abs(dcy)) {
			// target is right of source
			if (srcPos != null)
				srcPos.setLocation(srcRect.x + srcRect.width, csy);
			if (trgPos != null) trgPos.setLocation(trgRect.x, cty);
		} else if (dcy >= Math.abs(dcx)) {
			// target is below source
			if (srcPos != null)
				srcPos.setLocation(csx, srcRect.y + srcRect.height);
			if (trgPos != null) trgPos.setLocation(ctx, trgRect.y);
		} else if (dcx < 0 && Math.abs(dcx) > Math.abs(dcy)) {
			// target is left of source
			if (srcPos != null) srcPos.setLocation(srcRect.x, csy);
			if (trgPos != null)
				trgPos.setLocation(trgRect.x + trgRect.width, cty);
		} else {
			// target is above source
			if (srcPos != null) srcPos.setLocation(csx, srcRect.y);
			if (trgPos != null)
				trgPos.setLocation(ctx, trgRect.y + trgRect.height);
		}
	}

	// from http://www.java-forums.org/awt-swing/
	// 5842-how-draw-arrow-mark-using-java-swing.html
	private static Polygon getArrowPolygon(final int sx, final int sy,
			final int tx, final int ty) {
		final Polygon p = new Polygon();
		final double d = Math.atan2(sx - tx, sy - ty);
		// tip
		p.addPoint(tx, ty);
		// wing 1
		p.addPoint(tx + xCor(ARROW_TIP, d + .5), ty + yCor(ARROW_TIP, d + .5));
		// on line
		p.addPoint(tx + xCor(ARROW_WING, d), ty + yCor(ARROW_WING, d));
		// wing 2
		p.addPoint(tx + xCor(ARROW_TIP, d - .5), ty + yCor(ARROW_TIP, d - .5));
		// back to tip, close polygon
		p.addPoint(tx, ty);
		return p;
	}

	private static int xCor(final int len, final double dir) {
		return (int) (len * Math.sin(dir));
	}

	private static int yCor(final int len, final double dir) {
		return (int) (len * Math.cos(dir));
	}

	protected void addPipePart(final Class<? extends PipePart> part) {
		if (!ensureModifyable()) return;
		try {
			final PipePart pp = part.newInstance();
			createConfigureDialog("Add", pp, new Runnable() {
				@Override
				public void run() {
					check(pp.getGuiPosition(), pp);
					try {
						if (pp instanceof Input)
							Pipe.the().addInput((Input) pp);
						else if (pp instanceof Converter)
							Pipe.the().addConverter((Converter) pp);
						else if (pp instanceof Output)
							Pipe.the().addOutput((Output) pp);
						else
							throw new InternalException(
									"Invalid sub-class of PipePart '%s'", pp);
						getSet().add(pp);
					} catch (final StateException e) {
						Log.error(e, "Cannot add new PipePart");
					}
					updateSaneConfigCache();
					repaint();
				}
			});
		} catch (final InstantiationException e) {
			Log.error(e);
		} catch (final IllegalAccessException e) {
			Log.error(e);
		}
	}

	/**
	 * Should be invoked during a Drag&Drop operation if this section is the
	 * source of the operation. Updates the position of the current connection,
	 * if any, or otherwise tries to move the current remembered
	 * {@link PipePart} to the given position.
	 * 
	 * @param p
	 *            current cursor position
	 */
	protected void mouseDragged(final Point p) {
		if (currentPart == null) return;
		if (currentConnection != null) {
			// move connector instead of pipe-part
			if (!ensureModifyable()) return;
			if (currentConnectionsTarget != null) {
				try {
					currentPart
							.disconnectFromPipePart(currentConnectionsTarget);
				} catch (final StateException e) {
					Log.error(e, "Cannot delete connection");
				}
				updateSaneConfigCache();
				currentConnectionsTarget = null;
			}

			Rectangle r = new Rectangle(currentPart.getGuiPosition());
			r = r.union(currentConnection);

			currentConnection.setLocation(p.x - handlePoint.x, p.y
					- handlePoint.y);
			currentConnection.setSize(0, 0);
			check(currentConnection, null);
			currentConnectionValid = false;
			for (final PipePart pp : set)
				if (pp.getGuiPosition().contains(
						currentConnection.getLocation())) {
					currentConnectionValid = currentPart
							.isConnectionAllowed(pp);
					break;
				}
			mouseMoved(p);

			Rectangle2D.union(r, currentConnection, r);
			// need to take care of labels
			r.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
			repaint(r);
		} else {
			// move pipe-part
			Rectangle r = new Rectangle(currentPart.getGuiPosition());
			unionConnectionTargets(r);
			unionConnectionSources(r);
			currentPart.getGuiPosition().setLocation(p.x - handlePoint.x,
					p.y - handlePoint.y);
			check(currentPart.getGuiPosition(), currentPart);
			r = r.union(currentPart.getGuiPosition());
			unionConnectionTargets(r);
			unionConnectionSources(r);
			// need to take care of labels
			r.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
			repaint(r);
		}
	}

	private void unionConnectionSources(final Rectangle r) {
		for (final PipePart srcPP : set)
			if (srcPP.getConnectedPipeParts().contains(currentPart))
				unionConnection(r, srcPP);
	}

	private void unionConnectionTargets(final Rectangle r) {
		for (final PipePart trgPP : currentPart.getConnectedPipeParts())
			unionConnection(r, trgPP);
	}

	private void unionConnection(final Rectangle r, final PipePart pp) {
		final Point pt = new Point();
		calcConnectorPositions(currentPart.getGuiPosition(), pp
				.getGuiPosition(), null, pt);
		Rectangle2D.union(r, new Rectangle(pt.x, pt.y, 0, 0), r);
	}

	protected void check(final Rectangle r, final PipePart pp) {
		final int xMin;
		final int xMax;
		final int yMin = 1;
		final int yMax = bounds.height - 1;
		if (Input.class.isInstance(pp)) {
			xMin = 1;
			xMax = border1 - 1;
		} else if (Converter.class.isInstance(pp)) {
			xMin = border1 + 1;
			xMax = border2 - 1;
		} else if (Output.class.isInstance(pp)) {
			xMin = border2 + 1;
			xMax = bounds.width - 1;
		} else {
			xMin = 1;
			xMax = bounds.width - 1;
		}
		if (r.x < xMin) r.x = xMin;
		if (r.y < yMin) r.y = yMin;
		if (r.x + r.width > xMax) r.x = xMax - r.width;
		if (r.y + r.height > yMax) r.y = yMax - r.height;
		for (final PipePart other : set)
			if (other != pp && other.getGuiPosition().intersects(r)) {
				// move r, so it doesn't intersect anymore
				final Rectangle rO = other.getGuiPosition();
				final Rectangle i = r.intersection(rO);
				final int x0 = r.x + r.width / 2;
				final int y0 = r.y + r.height / 2;
				final int x1 = rO.x + rO.width / 2;
				final int y1 = rO.y + rO.height / 2;
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
						r.translate(0, i.height - r.height - rO.height);
					else
						r.translate(0, i.height); // move down
				} else if (r.y - i.height < yMin)
					// move down instead of up
					r.translate(0, r.height + rO.height - i.height);
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

	/**
	 * Should be called whenever the mouse is moved over this section.
	 * 
	 * @param p
	 *            current cursor position
	 */
	protected void mouseMoved(final Point p) {
		PipePart found = null;
		for (final PipePart pp : set)
			if (pp.getGuiPosition().contains(p)) {
				found = pp;
				break;
			}
		if (underCursor != found) {
			if (underCursor != null) repaint(underCursor.getGuiPosition());
			underCursor = found;
			if (underCursor != null) repaint(underCursor.getGuiPosition());
		}
	}

	/**
	 * Remembers the {@link PipePart} and the connector which is under the given
	 * cursor position for later use in {@link #mouseDragged(Point)} and during
	 * {@link #paintComponent(Graphics)}.
	 * 
	 * @param p
	 *            current cursor position
	 */
	protected void updateCurrent(final Point p) {
		// invoked on click or when a drag&drop operation starts
		updateCurrent0(p);
		repaint();
	}

	private void updateCurrent0(final Point p) {
		// check all pipe-parts
		for (final PipePart pp : set)
			if (pp.getGuiPosition().contains(p)) {
				currentPart = pp;
				final Rectangle inner = new Rectangle(pp.getGuiPosition());
				inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
				if (inner.contains(p)) {
					currentConnection = null;
					handlePoint = new Point(p.x - pp.getGuiPosition().x, p.y
							- pp.getGuiPosition().y);
				} else {
					currentConnection = new Rectangle(p.x, p.y, 0, 0);
					handlePoint = new Point(0, 0);
				}

				currentConnectionsTarget = null;
				currentConnectionValid = false;
				return;
			}

		// check all connections
		for (final PipePart srcPP : set)
			for (final PipePart trgPP : srcPP.getConnectedPipeParts()) {
				final Point ps = new Point();
				final Point pt = new Point();
				calcConnectorPositions(srcPP.getGuiPosition(), trgPP
						.getGuiPosition(), ps, pt);
				if (Line2D.ptSegDistSq(ps.x, ps.y, pt.x, pt.y, p.x, p.y) < LINE_CLICK_DIST
						|| getArrowPolygon(ps.x, ps.y, pt.x, pt.y).contains(p)) {
					currentPart = srcPP;
					currentConnection = new Rectangle(p.x, p.y, 0, 0);
					currentConnectionsTarget = trgPP;
					currentConnectionValid = false;
					handlePoint = new Point(p.x - pt.x, p.y - pt.y);
					return;
				}
			}

		currentPart = null;
		currentConnection = null;
		currentConnectionsTarget = null;
		currentConnectionValid = false;
		handlePoint = null;
	}

	/**
	 * Forgets about the currently remembered {@link PipePart} and connection,
	 * if any. If a connection has been remembered and is currently pointing to
	 * a valid {@link PipePart}, the remembered {@link PipePart} is connected to
	 * the one the connection is pointing to, even if it's outside of this
	 * section.
	 */
	protected void releaseCurrent() {
		// invoked on click or when a drag&drop operation is finished
		if (currentConnection != null && currentConnectionsTarget == null
				&& ensureModifyable()) {
			final Point p = new Point(currentConnection.getLocation());
			for (final PipePart pp : set)
				if (pp.getGuiPosition().contains(p)
						&& currentPart.isConnectionAllowed(pp)) {
					try {
						currentPart.connectToPipePart(pp);
					} catch (final StateException e) {
						Log.error(e, "Cannot create connection");
					}
					updateSaneConfigCache();
					break;
				}
		}
		// currentPart = null;
		currentConnection = null;
		currentConnectionValid = false;
		// currentConnectionsTarget = null;
		handlePoint = null;
		repaint();
	}

	/**
	 * Sorts all {@link PipePart}s according to their location on the board,
	 * from top to down, from left to right.
	 * 
	 * @param <T>
	 *            subclass of {@link PipePart} (compile-time)
	 * @param clazz
	 *            subclass of {@link PipePart} (run-time)
	 * @return sorted list of {@link PipePart}s
	 */
	@SuppressWarnings("unchecked")
	public <T extends PipePart> List<T> getSortedParts(final Class<T> clazz) {
		final List<T> res = new ArrayList<T>();
		for (final PipePart pp : set)
			if (clazz.isInstance(pp)) res.add((T) pp);
		Collections.sort(res, new Comparator<T>() {
			@Override
			public int compare(final T pp1, final T pp2) {
				final Rectangle r1 = pp1.getGuiPosition();
				final Rectangle r2 = pp2.getGuiPosition();
				int cmp = r1.y - r2.y;
				if (cmp == 0) cmp = r1.x - r2.x;
				return cmp;
			}
		});
		return res;
	}

	protected void updateBounds(final int width, final int height) {
		bounds.setSize(width, height);
		final int maxWidth = DEF_RECT_WIDTH + SECTION_SPACE;
		border1 = Math.min(width / SECTION_FRAC, maxWidth);
		border2 = width - Math.min(width / SECTION_FRAC, maxWidth);
		for (final PipePart pp : set)
			check(pp.getGuiPosition(), pp);
		repaint();
	}

	protected void showMenu(final Component invoker, final int x, final int y) {
		if (x <= border1)
			showMenu(menuInput, invoker, x, y);
		else if (x >= border2)
			showMenu(menuOutput, invoker, x, y);
		else
			showMenu(menuConverter, invoker, x, y);
	}

	protected void showMenu(final JPopupMenu menu, final Component invoker,
			final int x, final int y) {
		final MenuElement[] items = menu.getSubElements();
		((JMenuItem) items[idxMenuAdd]).setEnabled(modifyable
				&& currentPart == null);
		((JMenuItem) items[idxMenuConfPart]).setEnabled(modifyable
				&& currentPart != null && currentConnection == null
				&& !currentPart.getGuiConfigs().isEmpty());
		((JMenuItem) items[idxMenuDelPart]).setEnabled(modifyable
				&& currentPart != null && currentConnection == null);
		((JMenuItem) items[idxMenuDelPartConn]).setEnabled(modifyable
				&& currentPart != null && currentConnection == null
				&& !currentPart.getConnectedPipeParts().isEmpty());
		((JMenuItem) items[idxMenuDelConn]).setEnabled(modifyable
				&& currentConnection != null);
		menu.show(invoker, x, y);
	}

	protected void createConfigureDialog(final String prefix,
			final PipePart pp, final Runnable runIfOK) {
		// no need to configure if no values assigned
		if (pp.getGroup().isEmpty()) {
			if (runIfOK != null) runIfOK.run();
			return;
		}

		final JDialog dlg = new JDialog();
		dlg.setTitle(String.format("%s %s", prefix, pp));
		final Layouter lay = new Layouter(dlg);
		for (final ConfigValue v : pp.getGuiConfigs()) {
			// each config-value gets its own JPanel so they don't
			// interfere with each other.
			// LBL1 SUB1
			// LBL2 SUB2
			// LBL3 SUB3
			// BUTTONS
			final JPanel sub = new JPanel();
			final Layouter laySub = new Layouter(sub);
			final String compLabel = v.getLabel() + ":";
			final JLabel lbl = new JLabel(compLabel, SwingConstants.RIGHT);
			lbl.setVerticalAlignment(SwingConstants.TOP);
			lay.add(lbl, false);
			lay.addWholeLine(sub, false);
			v.insertGUIComponents(laySub);
		}

		final JPanel buttons = new JPanel();
		final Layouter lb = new Layouter(buttons);

		lay.addVerticalSpacer();
		lay.addWholeLine(buttons, false);

		lb.addButton(Button.Help, Layouter.help(dlg, "PartConfigureDialog"));
		lb.addSpacer();
		dlg.getRootPane().setDefaultButton(
				lb.addButton(Button.Ok, new Runnable() {
					@Override
					public void run() {
						if (saveConfigChanges(pp)) {
							dlg.dispose();
							if (runIfOK != null) runIfOK.run();
						}
					}
				}));
		lb.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				saveConfigChanges(pp);
			}
		});
		lb.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				dlg.dispose();
			}
		});

		dlg.pack();
		dlg.setLocationRelativeTo(null);
		// dlg.setModal(true);
		HelpDialog.closeHelpIfOpen();
		dlg.setVisible(true);
		HelpDialog.closeHelpIfOpen();
	}

	protected boolean saveConfigChanges(final PipePart pp) {
		if (!ensureModifyable()) return false;
		for (final ConfigValue v : pp.getGuiConfigs())
			v.setFromGUIComponents();
		updateSaneConfigCache();
		if (!pp.isConfigurationSane()) {
			Log.error("Configuration is invalid.");
			return false;
		}
		try {
			pp.configure();
		} catch (final PipeException e) {
			Log.error(e);
			return false;
		}
		return true;
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		if (underCursor == null) return null;
		final StringBuilder sb = new StringBuilder("<html><b>");
		sb.append(underCursor.getClass().getSimpleName());
		sb.append("</b><table>");
		for (final ConfigValue v : underCursor.getGuiConfigs()) {
			sb.append("<tr><td align=right>");
			sb.append(v.getLabel().replace("<", "&lt;"));
			sb.append("</td><td align=left>");
			sb.append(v.asString().replace("<", "&lt;"));
			sb.append("</td></tr>");
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	public void updateState() {
		final boolean changed = modifyable ^ !MainFrame.the().isPipeRunning();
		modifyable = !MainFrame.the().isPipeRunning();
		if (changed) repaint();
	}

	private boolean ensureModifyable() {
		if (!modifyable)
			Log.error("Configuration board is read-only as "
					+ "the Pipe is currently running.");
		return modifyable;
	}

}
