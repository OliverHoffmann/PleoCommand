package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class ConfigInt extends ConfigValue {

	private long content;

	private final long min, max;

	private JSpinner sp;

	public ConfigInt(final String label, final long min, final long max) {
		super(label);
		this.min = min;
		this.max = max;
	}

	public long getContent() {
		return content;
	}

	public void setContent(final long content) {
		this.content = content;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	public String toString() {
		return String.valueOf(content);
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		sp = new JSpinner(new SpinnerNumberModel(content, min, max, 1));
		cntr.add(sp, gbc);
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
		setContent((Long) sp.getValue());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		try {
			setContent(Long.valueOf(content));
		} catch (final NumberFormatException e) {
			throw new IOException("Invalid number for  " + getLabel() + ": "
					+ content);
		}
	}

}
