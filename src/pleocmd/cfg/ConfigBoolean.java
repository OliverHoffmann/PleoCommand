package pleocmd.cfg;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter;

public final class ConfigBoolean extends ConfigValue {

	private boolean content;

	private JCheckBox cb;

	private boolean internalMod;

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
		if (cb != null) cb.setSelected(content);
	}

	public Boolean getContentGUI() {
		return cb == null ? null : cb.isSelected();
	}

	public void setContentGUI(final boolean content) {
		internalMod = true;
		try {
			if (cb != null) cb.setSelected(content);
		} finally {
			internalMod = false;
		}
	}

	@Override
	public String asString() {
		return String.valueOf(content);
	}

	@Override
	public void setFromString(final String string)
			throws ConfigurationException {
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
	public String getIdentifier() {
		return "bool";
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public boolean insertGUIComponents(final Layouter lay) {
		cb = new JCheckBox("", content);
		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!isInternalMod())
					invokeChangingContent(getCb().isSelected());
			}
		});
		lay.add(cb, false);
		return false;
	}

	@Override
	public void setFromGUIComponents() {
		setContent(cb.isSelected());
	}

	protected JCheckBox getCb() {
		return cb;
	}

	protected boolean isInternalMod() {
		return internalMod;
	}

}
