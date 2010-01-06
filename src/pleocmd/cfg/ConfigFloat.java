package pleocmd.cfg;

import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter;

public final class ConfigFloat extends ConfigValue {

	private double content;
	private final double min, max;

	private JSpinner sp;

	public ConfigFloat(final String label, final double content,
			final double min, final double max) {
		super(label);
		this.content = content; // TODO may be out of bounds
		this.min = min;
		this.max = max;
	}

	public double getContent() {
		return content;
	}

	public void setContent(final double content) throws ConfigurationException {
		if (content < min || content > max)
			throw new ConfigurationException("%d not between %d and %d",
					content, min, max);
		this.content = content;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	String asString() {
		return String.valueOf(content);
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		try {
			setContent(Double.valueOf(string));
		} catch (final NumberFormatException e) {
			throw new ConfigurationException("Invalid number: '%s'", string);
		}
	}

	@Override
	List<String> asStrings() {
		throw new UnsupportedOperationException();
	}

	@Override
	void setFromStrings(final List<String> strings) {
		throw new UnsupportedOperationException();
	}

	@Override
	String getIdentifier() {
		return "float";
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, .00001));
		lay.add(sp, true);
	}

	@Override
	public void setFromGUIComponents() {
		try {
			setContent((Double) sp.getValue());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

}
