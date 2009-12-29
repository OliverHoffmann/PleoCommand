package pleocmd.pipe.cfg;

import java.io.IOException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pleocmd.itfc.gui.Layouter;

public final class ConfigFloat extends ConfigValue {

	private double content;
	private final double min, max;

	private JSpinner sp;

	public ConfigFloat(final String label, final double min, final double max) {
		super(label);
		this.min = min;
		this.max = max;
		content = min;
	}

	public double getContent() {
		return content;
	}

	public void setContent(final double content) {
		if (content < min || content > max)
			throw new IndexOutOfBoundsException(String.format(
					"New content %f must be between %f and %f", content, min,
					max));
		this.content = content;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public String getContentAsString() {
		return String.valueOf(content);
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, .00001));
		lay.add(sp, true);
	}

	@Override
	public void setFromGUIComponents() {
		setContent((Double) sp.getValue());
	}

	@Override
	public void setFromString(final String content) throws IOException {
		try {
			setContent(Double.valueOf(content));
		} catch (final NumberFormatException e) {
			throw new IOException(String.format(
					"Invalid number for '%s': '%s'", getLabel(), content));
		}
	}

}
