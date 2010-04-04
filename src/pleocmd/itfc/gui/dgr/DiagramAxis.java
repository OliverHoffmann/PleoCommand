package pleocmd.itfc.gui.dgr;

import java.awt.geom.Point2D;

import pleocmd.itfc.gui.dgr.DiagramDataSet.DiagramType;

public final class DiagramAxis {

	public enum AxisScale {
		LinearScale, LogarithmicScale
	}

	private final Diagram diagram;

	private String axisName;

	private String unitName = "";

	private AxisScale scale = AxisScale.LinearScale;

	private double min = Double.MIN_VALUE;

	private double max = Double.MAX_VALUE;

	private boolean reversed;

	private int subsPerUnit = 5;

	private double offset;

	private double cachedMinUnit;

	private double cachedMaxUnit;

	private double cachedMinVisUnit;

	private double cachedMaxVisUnit;

	private int cachedUnitsPerGrid;

	private double cachedPixelPerUnit;

	private double cachedPixelPerGrid;

	private double cachedPixelPerSubGrid;

	public DiagramAxis(final Diagram diagram, final String axisName) {
		this.diagram = diagram;
		this.axisName = axisName;
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(final String axisName) {
		synchronized (diagram) {
			this.axisName = axisName;
		}
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(final String unitName) {
		synchronized (diagram) {
			this.unitName = unitName;
		}
	}

	public AxisScale getScale() {
		return scale;
	}

	public void setScale(final AxisScale scale) {
		synchronized (diagram) {
			this.scale = scale;
		}
	}

	public double getMin() {
		return min;
	}

	public void setMin(final double min) {
		synchronized (diagram) {
			this.min = min;
		}
	}

	public double getMax() {
		return max;
	}

	public void setMax(final double max) {
		synchronized (diagram) {
			this.max = max;
		}
	}

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(final boolean reversed) {
		synchronized (diagram) {
			this.reversed = reversed;
		}
	}

	public int getSubsPerUnit() {
		return subsPerUnit;
	}

	public void setSubsPerUnit(final int subsPerUnit) {
		synchronized (diagram) {
			this.subsPerUnit = subsPerUnit;
		}
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(final double offset) {
		synchronized (diagram) {
			this.offset = offset;
		}
	}

	void updateCache(final int availPixels) {
		cachedMinUnit = min;
		cachedMaxUnit = max;
		final DiagramAxis xAxis = diagram.getXAxis();
		final boolean isXAxis = xAxis == this;
		if (max >= Double.MAX_VALUE || min <= Double.MIN_VALUE) {
			double low = Double.MAX_VALUE, high = Double.MIN_VALUE;
			for (final DiagramDataSet ds : diagram.getDataSets()) {
				if (!isXAxis && ds.getType() == DiagramType.IntersectionDiagram)
					continue;
				if (!ds.isValid()) continue;
				for (final Point2D.Double pt : ds.getPoints()) {
					final double unitX = pt.x / ds.getValuePerUnitX();
					if (isXAxis) {
						// all values count
						low = Math.min(low, unitX);
						high = Math.max(high, unitX);
					} else {
						// only visible values count
						final double unitY = pt.y / ds.getValuePerUnitY();
						if (unitX >= xAxis.getCachedMinVisUnit()
								&& unitX <= xAxis.getCachedMaxVisUnit()) {
							low = Math.min(low, unitY);
							high = Math.max(high, unitY);
						}
					}
				}
			}
			if (low == Double.MAX_VALUE) low = 0;
			if (high == Double.MIN_VALUE) high = 1;
			if (min <= Double.MIN_VALUE) cachedMinUnit = low;
			if (max >= Double.MAX_VALUE) cachedMaxUnit = high;
		}
		cachedPixelPerUnit = availPixels / (cachedMaxUnit - cachedMinUnit)
				* diagram.getZoom();
		final double visibleUnits = availPixels / cachedPixelPerUnit;
		cachedMinVisUnit = cachedMinUnit + offset;
		cachedMaxVisUnit = cachedMinVisUnit + visibleUnits;
		cachedUnitsPerGrid = Math.max(1, (int) (Diagram.MIN_GRID_DELTA
				* Math.max(subsPerUnit, 1) / cachedPixelPerUnit));
		cachedPixelPerGrid = cachedPixelPerUnit * cachedUnitsPerGrid;
		cachedPixelPerSubGrid = subsPerUnit > 1 ? cachedPixelPerGrid
				/ subsPerUnit : 0;
		// System.out.println(cachedUnitsPerGrid + " " + cachedPixelPerGrid +
		// " "
		// + cachedPixelPerSubGrid + " " + cachedMinVisUnit + " "
		// + cachedMaxVisUnit);
	}

	double unitToPixel(final double unit) {
		return (unit - cachedMinVisUnit) * cachedPixelPerUnit;
	}

	double getCachedMinUnit() {
		return cachedMinUnit;
	}

	double getCachedMaxUnit() {
		return cachedMaxUnit;
	}

	double getCachedMinVisUnit() {
		return cachedMinVisUnit;
	}

	double getCachedMaxVisUnit() {
		return cachedMaxVisUnit;
	}

	int getCachedUnitsPerGrid() {
		return cachedUnitsPerGrid;
	}

	double getCachedPixelPerUnit() {
		return cachedPixelPerUnit;
	}

	double getCachedPixelPerGrid() {
		return cachedPixelPerGrid;
	}

	double getCachedPixelPerSubGrid() {
		return cachedPixelPerSubGrid;
	}

}
