package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;

import javax.swing.JTextField;

public final class ConfigString extends ConfigValue {

	private String content;

	private JTextField tf;

	public ConfigString(final String label) {
		super(label);
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	@Override
	public String getContentAsString() {
		return content;
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		tf = new JTextField(content, 50);
		cntr.add(tf, gbc);
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
		setContent(tf.getText());
	}

	@Override
	protected void setFromString(final String content) {
		setContent(content);
	}

}
