// This file is part of PleoCommand:
// Interactively control Pleo with psychobiological parameters
//
// Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Boston, USA.

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

	@Override
	public Boolean getContent() {
		return content;
	}

	public void setContent(final boolean content) {
		this.content = content;
		if (cb != null) cb.setSelected(content);
	}

	public Boolean getContentGUI() {
		return cb == null ? content : cb.isSelected();
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
		invokeChangingContent(cb.isSelected());
		return false;
	}

	@Override
	public void setFromGUIComponents() {
		setContent(cb.isSelected());
	}

	@Override
	public void setGUIEnabled(final boolean enabled) {
		if (cb != null) cb.setEnabled(enabled);
	}

	protected JCheckBox getCb() {
		return cb;
	}

	protected boolean isInternalMod() {
		return internalMod;
	}

}
