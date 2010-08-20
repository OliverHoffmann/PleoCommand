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

import java.awt.Color;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter;

public class ConfigColor extends ConfigValue {

	private Color content;

	private JColorChooser cc;

	private boolean internalMod;

	public ConfigColor(final String label) {
		super(label);
		clearContent();
	}

	public ConfigColor(final String label, final Color content) {
		super(label);
		setContent(content);
	}

	@Override
	public final Color getContent() {
		return content;
	}

	public final void setContent(final Color content) {
		if (content == null) throw new NullPointerException("content");
		this.content = content;
		if (cc != null) cc.setColor(content);
	}

	public final void clearContent() {
		content = Color.BLACK;
		if (cc != null) cc.setColor(content);
	}

	public final Color getContentGUI() {
		return cc == null ? content : cc.getColor();
	}

	public final void setContentGUI(final Color content) {
		internalMod = true;
		try {
			if (cc != null) cc.setColor(content);
		} finally {
			internalMod = false;
		}
	}

	public final void clearContentGUI() {
		internalMod = true;
		try {
			if (cc != null) cc.setColor(Color.BLACK);
		} finally {
			internalMod = false;
		}
	}

	@Override
	public final String asString() {
		return String.format("%d,%d,%d", content.getRed(), content.getGreen(),
				content.getBlue());
	}

	@Override
	public final void setFromString(final String string)
			throws ConfigurationException {
		final int idx1 = string.indexOf(',');
		final int idx2 = string.indexOf(',', idx1 + 1);
		final int idx3 = string.indexOf(',', idx2 + 1);
		if (idx1 == -1 || idx2 == -1 || idx3 != -1)
			throw new ConfigurationException("Invalid color string", string);
		final int r = Integer.valueOf(string.substring(0, idx1));
		final int g = Integer.valueOf(string.substring(idx1 + 1, idx2));
		final int b = Integer.valueOf(string.substring(idx2 + 1));
		setContent(new Color(r, g, b));
	}

	@Override
	final List<String> asStrings() {
		throw new UnsupportedOperationException();
	}

	@Override
	final void setFromStrings(final List<String> strings) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final String getIdentifier() {
		return "color";
	}

	@Override
	final boolean isSingleLined() {
		return true;
	}

	@Override
	// CS_IGNORE_PREV need to be overridable
	public boolean insertGUIComponents(final Layouter lay) {
		cc = new JColorChooser();
		cc.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!isInternalMod())
					invokeChangingContent(getCc().getColor());
			}
		});
		lay.add(cc, true);
		invokeChangingContent(cc.getColor());
		return false;
	}

	@Override
	// CS_IGNORE_PREV need to be overridable
	public void setFromGUIComponents() {
		setContent(cc.getColor());
	}

	@Override
	public final void setGUIEnabled(final boolean enabled) {
		if (cc != null) cc.setEnabled(enabled);
	}

	protected final JColorChooser getCc() {
		return cc;
	}

	protected final boolean isInternalMod() {
		return internalMod;
	}
}
