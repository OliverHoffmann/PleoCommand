package pleocmd.pipe.cfg;

import javax.swing.JTextField;

import pleocmd.itfc.gui.Layouter;

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
	public void insertGUIComponents(final Layouter lay) {
		tf = new JTextField(content, 50);
		lay.add(tf, true);
	}

	@Override
	public void setFromGUIComponents() {
		setContent(tf.getText());
	}

	@Override
	protected void setFromString(final String content) {
		setContent(content);
	}

}
