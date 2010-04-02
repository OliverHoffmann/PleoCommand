package pleocmd.itfc.gui.dgr;

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

}
