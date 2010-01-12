package pleocmd.cfg;

import java.util.List;

import javax.swing.JCheckBox;

import pleocmd.itfc.gui.Layouter;

public final class ConfigBoolean extends ConfigValue {

	private boolean content;

	private JCheckBox checkBox;

	public ConfigBoolean(final String label) {
		super(label);
		setContent(false);
	}

	public ConfigBoolean(final String label, final boolean content) {
		super(label);
		setContent(content);
	}

	public boolean getContent() {
		return content;
	}

	public void setContent(final boolean content) {
		this.content = content;
	}

	@Override
	String asString() {
		return String.valueOf(content);
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		if ("true".equals(string))
			setContent(true);
		else if ("false".equals(string))
			setContent(false);
		else
			throw new ConfigurationException("Invalid boolean string in '%s'",
					string);
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
		return "bool";
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		checkBox = new JCheckBox("", content);
		lay.add(checkBox, false);
	}

	@Override
	public void setFromGUIComponents() {
		setContent(checkBox.isSelected());
	}

}
