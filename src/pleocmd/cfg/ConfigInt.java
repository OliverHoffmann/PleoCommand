package pleocmd.cfg;

import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter;

public final class ConfigInt extends ConfigValue {

	private long content;

	private final long min, max;

	private JSpinner sp;

	public ConfigInt(final String label, final long content, final long min,
			final long max) {
		super(label);
		this.content = content; // TODO may be out of bounds
		this.min = min;
		this.max = max;
	}

	public long getContent() {
		return content;
	}

	public void setContent(final long content) throws ConfigurationException {
		if (content < min || content > max)
			throw new ConfigurationException("%d not between %d and %d",
					content, min, max);
		this.content = content;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	String asString() {
		return String.valueOf(content);
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		try {
			setContent(Long.valueOf(string));
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
		return "int";
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, 1));
		lay.add(sp, true);
	}

	@Override
	public void setFromGUIComponents() {
		try {
			setContent(((Double) sp.getValue()).longValue());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

}