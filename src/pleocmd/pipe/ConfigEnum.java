package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

public final class ConfigEnum extends ConfigValue {

	private final List<String> identifiers = new ArrayList<String>();

	private int content;

	private JComboBox cb;

	public ConfigEnum(final String label, final List<String> identifiers) {
		super(label);
		this.identifiers.addAll(identifiers);
	}

	public ConfigEnum(final String label, final Object[] identifiers) {
		super(label);
		for (final Object id : identifiers)
			this.identifiers.add(id.toString());
	}

	public int getContent() {
		return content;
	}

	public void setContent(final int content) {
		this.content = content;
	}

	public List<String> getIdentifiers() {
		return Collections.unmodifiableList(identifiers);
	}

	@Override
	public String toString() {
		return identifiers.get(content);
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		cb = new JComboBox(identifiers.toArray());
		cb.setSelectedIndex(content);
		cntr.add(cb, gbc);
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
		setContent(cb.getSelectedIndex());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		final int idx = identifiers.indexOf(content);
		if (idx == -1)
			throw new IOException("Invalid enum constant for  " + getLabel()
					+ ": " + content + " - must be one of "
					+ Arrays.toString(identifiers.toArray()));
		setContent(idx);
	}

}
