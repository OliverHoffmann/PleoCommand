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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

public class ConfigItem<E> extends ConfigValue {

	private final List<String> identifiers = new ArrayList<String>();

	private final boolean freeAssign;

	private String content;

	private JComboBox cb;

	private boolean internalMod;

	/**
	 * Creates a new {@link ConfigItem}.
	 * 
	 * @param label
	 *            name of this {@link ConfigItem} - used in GUI mode
	 *            configuration and for configuration files
	 * @param freeAssign
	 *            if true any string may be assigned to this {@link ConfigItem},
	 *            if false only the ones in the list of identifiers may be used
	 * @param identifiers
	 *            list of valid {@link String}s that can be used for this
	 *            {@link ConfigItem} or - if freeAssign is true - a list of
	 *            proposals for GUI mode configuration.
	 */
	public ConfigItem(final String label, final boolean freeAssign,
			final List<E> identifiers) {
		super(label);

		if (identifiers.isEmpty() && !freeAssign)
			throw new IllegalArgumentException("list of identifiers is empty");
		try {
			for (final E id : identifiers) {
				final String idStr = id.toString();
				checkValidString(idStr, false);
				this.identifiers.add(idStr);
			}
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"List of identifiers is invalid", e);
		}

		this.freeAssign = freeAssign;
		if (!identifiers.isEmpty()) setContentIndex(0);
	}

	public ConfigItem(final String label, final String content,
			final List<E> identifiers) {
		this(label, true, identifiers);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public ConfigItem(final String label, final int contentIndex,
			final List<E> identifiers) {
		this(label, true, identifiers);
		setContentIndex(contentIndex);
	}

	@Override
	public final String getContent() {
		return content;
	}

	public final void setContent(final String content)
			throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!freeAssign && !identifiers.contains(content))
			throw new ConfigurationException("Invalid constant "
					+ "for '%s': '%s' - must be one of '%s'", getLabel(),
					content, Arrays.toString(identifiers.toArray()));
		checkValidString(content, false);
		this.content = content;
		if (cb != null) cb.setSelectedItem(content);
	}

	public final void setContentIndex(final int content) {
		if (content < 0 || content >= identifiers.size())
			throw new IndexOutOfBoundsException(String.format(
					"New content %d for '%s' must be between 0 "
							+ "and %d for '%s'", content, getLabel(),
					identifiers.size() - 1,
					Arrays.toString(identifiers.toArray())));
		this.content = identifiers.get(content);
		if (cb != null) cb.setSelectedIndex(content);
	}

	public final String getContentGUI() {
		return cb == null ? content : (String) cb.getSelectedItem();
	}

	public final void setContentGUI(final String content) {
		internalMod = true;
		try {
			if (cb != null) cb.setSelectedItem(content);
		} finally {
			internalMod = false;
		}
	}

	public final void setContentIndexGUI(final int content) {
		internalMod = true;
		try {
			if (cb != null) cb.setSelectedIndex(content);
		} finally {
			internalMod = false;
		}
	}

	public final List<String> getIdentifiers() {
		return Collections.unmodifiableList(identifiers);
	}

	@Override
	public final String asString() {
		return content;
	}

	@Override
	public final void setFromString(final String string)
			throws ConfigurationException {
		setContent(string);
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
		return null;
	}

	@Override
	final boolean isSingleLined() {
		return true;
	}

	@Override
	public final boolean insertGUIComponents(final Layouter lay) {
		cb = new JComboBox(identifiers.toArray());
		cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (!isInternalMod())
					invokeChangingContent(getCb().getSelectedItem());
			}
		});
		cb.setEditable(freeAssign);
		cb.setSelectedItem(content);
		lay.add(cb, true);
		invokeChangingContent(cb.getSelectedItem());
		return false;
	}

	@Override
	public final void setFromGUIComponents() {
		try {
			setContent(cb.getSelectedItem().toString());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public final void setGUIEnabled(final boolean enabled) {
		if (cb != null) cb.setEnabled(enabled);
	}

	protected final JComboBox getCb() {
		return cb;
	}

	protected final boolean isInternalMod() {
		return internalMod;
	}

}
