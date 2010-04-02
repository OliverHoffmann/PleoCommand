package pleocmd.itfc.gui.dgr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Pen {

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
