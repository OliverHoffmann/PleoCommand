package pleocmd.itfc.gui.dgr;

public class DiagramAxis {

	public enum AxisScale {
		LinearScale, LogarithmicScale
	}

	private String axisName;

	private String unitName = "";

	private AxisScale scale = AxisScale.LinearScale;

	private double min = Double.MIN_VALUE;

	private double max = Double.MAX_VALUE;

	private boolean reversed;

	private int subsPerUnit = 5;

	private double offset;

	public DiagramAxis(final String axisName) {
		this.axisName = axisName;
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(final String axisName) {
		this.axisName = axisName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(final String unitName) {
		this.unitName = unitName;
	}

	public AxisScale getScale() {
		return scale;
	}

	public void setScale(final AxisScale scale) {
		this.scale = scale;
	}

	public double getMin() {
		return min;
	}

	public void setMin(final double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(final double max) {
		this.max = max;
	}

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(final boolean reversed) {
		this.reversed = reversed;
	}

	public int getSubsPerUnit() {
		return subsPerUnit;
	}

	public void setSubsPerUnit(final int subsPerUnit) {
		this.subsPerUnit = subsPerUnit;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(final double offset) {
		this.offset = offset;
	}

}
