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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

public abstract class ConfigCollection<E> extends ConfigValue {

	public enum Type {
		Set, List
	}

	private final Type type;

	private final Collection<E> content;

	private JTextArea ta;

	private boolean internalMod;

	public ConfigCollection(final String label, final Type type) {
		super(label);
		this.type = type;
		switch (type) {
		case List:
			content = new ArrayList<E>();
			break;
		case Set:
			content = new HashSet<E>();
			break;
		default:
			throw new InternalException(type);
		}
	}

	public ConfigCollection(final String label, final Type type,
			final Collection<E> content) {
		this(label, type);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public final Type getType() {
		return type;
	}

	@Override
	public final Collection<E> getContent() {
		return Collections.unmodifiableCollection(content);
	}

	public final void setContent(final Collection<? extends E> content)
			throws ConfigurationException {
		clearContent();
		addContent(content);
	}

	public final <F extends E> void addContent(final F item)
			throws ConfigurationException {
		checkValidString(item.toString(), false);
		content.add(item);
		if (ta != null) ta.setText(asString());
	}

	public final void addContent(final Collection<? extends E> contentToAdd)
			throws ConfigurationException {
		for (final Object o : contentToAdd)
			checkValidString(o.toString(), false);
		content.addAll(contentToAdd);
		if (ta != null) ta.setText(asString());
	}

	public final <F extends E> boolean removeContent(final F item) {
		if (item == null) throw new NullPointerException();
		final boolean res = content.remove(item);
		if (ta != null) ta.setText(asString());
		return res;
	}

	public final <F extends E> boolean contains(final F item) {
		if (item == null) throw new NullPointerException();
		return content.contains(item);
	}

	public final void clearContent() {
		content.clear();
		if (ta != null) ta.setText(asString());
	}

	public final Collection<E> getContentGUI() {
		if (ta == null) return getContent();
		final List<E> list = new ArrayList<E>();
		final StringTokenizer st = new StringTokenizer(ta.getText(), "\n");
		try {
			while (st.hasMoreTokens())
				list.add(createItem(st.nextToken()));
		} catch (final ConfigurationException e) {
			return null;
		}
		return list;
	}

	public final void setContentGUI(final Collection<? extends E> content) {
		internalMod = true;
		try {
			if (ta != null) ta.setText(content.toString());
			// FIXME same as below
		} finally {
			internalMod = false;
		}
	}

	public final void clearContentGUI() {
		internalMod = true;
		try {
			if (ta != null) ta.setText("");
		} finally {
			internalMod = false;
		}
	}

	@Override
	public final String asString() {
		return content.toString();
	}

	@Override
	public final void setFromString(final String string)
			throws ConfigurationException {
		final List<E> list = new ArrayList<E>();
		final StringTokenizer st = new StringTokenizer(string, "\n");
		while (st.hasMoreTokens())
			list.add(createItem(st.nextToken()));
		setContent(list);
	}

	protected abstract E createItem(String itemAsString)
			throws ConfigurationException;

	@Override
	final List<String> asStrings() {
		final List<String> list = new ArrayList<String>(content.size());
		for (final E item : content)
			list.add(item.toString());
		return list;
	}

	@Override
	final void setFromStrings(final List<String> strings)
			throws ConfigurationException {
		final List<E> list = new ArrayList<E>(strings.size());
		for (final String str : strings)
			list.add(createItem(str));
		setContent(list);
	}

	@Override
	public final String getIdentifier() {
		switch (type) {
		case List:
			return "list";
		case Set:
			return "set";
		default:
			throw new InternalException(type);
		}
	}

	@Override
	final boolean isSingleLined() {
		return false;
	}

	@Override
	public final boolean insertGUIComponents(final Layouter lay) {
		ta = new JTextArea(asString(), 5, 20); // FIXME should be asStrings()
		ta.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(final UndoableEditEvent e) {
				if (!isInternalMod()) invokeChangingContent(getTa().getText());
			}
		});
		lay.addWholeLine(new JScrollPane(ta), true);
		invokeChangingContent(ta.getText());
		return true;
	}

	@Override
	public final void setFromGUIComponents() {
		try {
			setFromString(ta.getText());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public final void setGUIEnabled(final boolean enabled) {
		if (ta != null) ta.setEnabled(enabled);
	}

	protected final JTextArea getTa() {
		return ta;
	}

	protected final boolean isInternalMod() {
		return internalMod;
	}

}
