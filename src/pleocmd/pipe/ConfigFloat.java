package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class ConfigFloat extends ConfigValue {

	private double content;
	private final double min, max;

	private JSpinner sp;

	public ConfigFloat(final String label, final double min, final double max) {
		super(label);
		this.min = min;
		this.max = max;
	}

	public double getContent() {
		return content;
	}

	public void setContent(final double content) {
		this.content = content;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public String toString() {
		return String.valueOf(content);
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, .00001));
		cntr.add(sp, gbc);
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
		setContent((Double) sp.getValue());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		try {
			setContent(Double.valueOf(content));
		} catch (final NumberFormatException e) {
			throw new IOException("Invalid number for  " + getLabel() + ": "
					+ content);
		}
	}

}
