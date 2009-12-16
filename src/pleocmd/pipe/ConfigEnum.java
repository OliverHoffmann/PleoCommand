package pleocmd.pipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import pleocmd.itfc.gui.Layouter;

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
	public String getContentAsString() {
		return identifiers.get(content);
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		cb = new JComboBox(identifiers.toArray());
		cb.setSelectedIndex(content);
		lay.add(cb, true);
	}

	@Override
	public void setFromGUIComponents() {
		setContent(cb.getSelectedIndex());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		final int idx = identifiers.indexOf(content);
		if (idx == -1)
			throw new IOException(String.format(
					"Invalid enumeration constant for '%s': "
							+ "'%s' - must be one of %s", getLabel(), content,
					Arrays.toString(identifiers.toArray())));
		setContent(idx);
	}

}
