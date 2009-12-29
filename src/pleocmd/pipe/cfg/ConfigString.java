package pleocmd.pipe.cfg;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import pleocmd.itfc.gui.Layouter;

public final class ConfigString extends ConfigValue {

	private final boolean multiLine;

	private String content;

	private JTextComponent tc;

	public ConfigString(final String label, final boolean multiLine) {
		super(label);
		this.multiLine = multiLine;
		content = "";
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine && content.contains("\n"))
			throw new IllegalArgumentException("content contains line-feeds");
		this.content = content;
	}

	@Override
	public String getContentAsString() {
		return content;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		tc = multiLine ? new JTextArea(content, 50, 5) : new JTextField(
				content, 50);
		lay.add(tc, true);
	}

	@Override
	public void setFromGUIComponents() {
		setContent(tc.getText());
	}

	@Override
	public void setFromString(final String content) {
		setContent(content);
	}

}
