package pleocmd.itfc.gui.dgr;

import java.awt.Color;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DiagramDataSet {

	public enum DiagramType {
		LineDiagram, BarDiagram, ScatterPlotDiagram, IntersectionDiagram
	}

	private static final double MIN_VAL_PER_UNIT = 0.0000001;

	private final Diagram diagram;

	private final List<Double> points = new ArrayList<Double>();

	private boolean prepared;

	private String label;

	private Pen pen = new Pen(Color.BLACK, 2.0f);

	private boolean penAutomatic = true;

	private DiagramType type = DiagramType.LineDiagram;

	private double valuePerUnitX = 1.0;

	private double valuePerUnitY = 1.0;

	private double cachedValueToPixXIncr;

	private double cachedValueToPixYIncr;

	private double cachedValueToPixXFactor;

	private double cachedValueToPixYFactor;

	public DiagramDataSet(final Diagram diagram, final String label) {
		this.diagram = diagram;
		this.label = label;
		diagram.addDataSet(this);
	}

	public Diagram getDiagram() {
		return diagram;
	}

	public List<Double> getPoints() {
		return Collections.unmodifiableList(points);
	}

	public void setPoints(final List<Double> points) {
		synchronized (diagram) {
			this.points.clear();
			this.points.addAll(points);
			prepared = false;
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		synchronized (diagram) {
			this.label = label;
		}
	}

	public Pen getPen() {
		return pen;
	}

	public void setPen(final Pen pen) {
		synchronized (diagram) {
			this.pen = pen;
			penAutomatic = false;
		}
	}

	public boolean isPenAutomatic() {
		return penAutomatic;
	}

	public DiagramType getType() {
		return type;
	}

	public void setType(final DiagramType type) {
		synchronized (diagram) {
			this.type = type;
		}
	}

	public double getValuePerUnitX() {
		return valuePerUnitX;
	}

	public void setValuePerUnitX(final double valuePerUnitX) {
		synchronized (diagram) {
			this.valuePerUnitX = valuePerUnitX;
		}
	}

	public double getValuePerUnitY() {
		return valuePerUnitY;
	}

	public void setValuePerUnitY(final double valuePerUnitY) {
		synchronized (diagram) {
			this.valuePerUnitY = valuePerUnitY;
		}
	}

	public void addPoint(final Double point) {
		synchronized (diagram) {
			points.add(point);
			prepared = false;
		}
	}

	public void addSequence(final int[] values) {
		addSequence(values, 0);
	}

	public void addSequence(final int[] values, final int xStart) {
		addSequence(values, xStart, 1);
	}

	public void addSequence(final int[] values, final int xStart,
			final int xIncr) {
		synchronized (diagram) {
			int x = xStart;
			for (final int value : values) {
				points.add(new Double(x, value));
				x += xIncr;
			}
			prepared = false;
		}
	}

	public void addSequence(final double[] values) {
		addSequence(values, 0);
	}

	public void addSequence(final double[] values, final double xStart) {
		addSequence(values, xStart, 1);
	}

	public void addSequence(final double[] values, final double xStart,
			final int xIncr) {
		double x = xStart;
		for (final double value : values) {
			points.add(new Double(x, value));
			x += xIncr;
		}
		prepared = false;
	}

	void prepare() {
		if (prepared) return;
		prepared = true;
		if (type == DiagramType.LineDiagram)
			Collections.sort(points, new Comparator<Double>() {

				@Override
				public int compare(final Double p1, final Double p2) {
					return p1.x < p2.x ? -1 : p1.x > p2.x ? 1 : 0;
				}

			});
	}

	public boolean isValid() {
		return !points.isEmpty() && valuePerUnitX >= MIN_VAL_PER_UNIT
				&& valuePerUnitY >= MIN_VAL_PER_UNIT;
	}

	void updateCache() {
		cachedValueToPixXIncr = -diagram.getXAxis().getCachedMinVisUnit();
		cachedValueToPixXFactor = diagram.getXAxis().getCachedPixelPerUnit();
		cachedValueToPixYIncr = -diagram.getYAxis().getCachedMinVisUnit();
		cachedValueToPixYFactor = diagram.getYAxis().getCachedPixelPerUnit();
	}

	double valueToPixelX(final double value) {
		return (value / valuePerUnitX + cachedValueToPixXIncr)
				* cachedValueToPixXFactor;
	}

	double valueToPixelY(final double value) {
		return (value / valuePerUnitY + cachedValueToPixYIncr)
				* cachedValueToPixYFactor;
	}

	double getCachedValueToPixXIncr() {
		return cachedValueToPixXIncr;
	}

	double getCachedValueToPixYIncr() {
		return cachedValueToPixYIncr;
	}

	double getCachedValueToPixXFactor() {
		return cachedValueToPixXFactor;
	}

	double getCachedValueToPixYFactor() {
		return cachedValueToPixYFactor;
	}

}
