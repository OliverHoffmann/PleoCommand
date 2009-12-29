package pleocmd.pipe.cfg;

import java.io.IOException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pleocmd.itfc.gui.Layouter;

public final class ConfigInt extends ConfigValue {

	private long content;

	private final long min, max;

	private JSpinner sp;

	public ConfigInt(final String label, final long min, final long max) {
		super(label);
		this.min = min;
		this.max = max;
		content = min;
	}

	public long getContent() {
		return content;
	}

	public void setContent(final long content) {
		if (content < min || content > max)
			throw new IndexOutOfBoundsException(String.format(
					"New content %d must be between %d and %d", content, min,
					max));
		this.content = content;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	public String getContentAsString() {
		return String.valueOf(content);
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, 1));
		lay.add(sp, true);
	}

	@Override
	public void setFromGUIComponents() {
		setContent((Long) sp.getValue());
	}

	@Override
	public void setFromString(final String content) throws IOException {
		try {
			setContent(Long.valueOf(content));
		} catch (final NumberFormatException e) {
			throw new IOException(String.format(
					"Invalid number for '%s': '%s'", getLabel(), content));
		}
	}

}
