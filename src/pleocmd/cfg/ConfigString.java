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
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

public class ConfigString extends ConfigValue {

	private final boolean multiLine;

	private String content;

	private JTextComponent tc;

	private boolean internalMod;

	public ConfigString(final String label, final boolean multiLine) {
		super(label);
		this.multiLine = multiLine;
		clearContent();
	}

	public ConfigString(final String label, final String content) {
		super(label);
		multiLine = false;
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public ConfigString(final String label, final List<String> content) {
		super(label);
		multiLine = true;
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	@Override
	public final String getContent() {
		return content;
	}

	public final List<String> getContentList() {
		final List<String> res = new ArrayList<String>();
		final StringTokenizer st = new StringTokenizer(content, "\n");
		while (st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	public final void setContent(final String content)
			throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine && content.contains("\n"))
			throw new ConfigurationException("content contains line-feeds");
		final String s = content.endsWith("\n") ? content.substring(0, content
				.length() - 1) : content;
		checkValidString(s, multiLine);
		this.content = s;
		if (tc != null) tc.setText(s);
	}

	public final void setContent(final List<String> content)
			throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine)
			throw new ConfigurationException("content must be single lined");
		final StringBuilder sb = new StringBuilder();
		for (final String str : content) {
			checkValidString(str, false);
			sb.append(str);
			sb.append('\n');
		}
		try {
			setContent(sb.toString());
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public final void clearContent() {
		content = "";
		if (tc != null) tc.setText("");
	}

	public final String getContentGUI() {
		return tc == null ? content : tc.getText();
	}

	public final List<String> getContentListGUI() {
		if (tc == null) return null;
		final List<String> res = new ArrayList<String>();
		final StringTokenizer st = new StringTokenizer(tc.getText(), "\n");
		while (st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	public final void setContentGUI(final String content) {
		internalMod = true;
		try {
			if (tc != null) tc.setText(content);
		} finally {
			internalMod = false;
		}
	}

	public final void clearContentGUI() {
		internalMod = true;
		try {
			if (tc != null) tc.setText("");
		} finally {
			internalMod = false;
		}
	}

	@Override
	public final String asString() {
		return content;
	}

	@Override
	final void setFromString(final String string) throws ConfigurationException {
		setContent(string);
	}

	@Override
	final List<String> asStrings() {
		return getContentList();
	}

	@Override
	final void setFromStrings(final List<String> strings)
			throws ConfigurationException {
		setContent(strings);
	}

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	final boolean isSingleLined() {
		return !multiLine;
	}

	@Override
	// CS_IGNORE_PREV need to be overridable
	public boolean insertGUIComponents(final Layouter lay) {
		tc = multiLine ? new JTextArea(content, 5, 20) : new JTextField(
				content, 20);
		if (multiLine) {
			final JScrollPane sp = new JScrollPane(tc);
			lay.addWholeLine(sp, true);
		} else
			lay.add(tc, true);
		invokeChangingContent(tc.getText());
		return multiLine;
	}

	@Override
	// CS_IGNORE_PREV need to be overridable
	public void setFromGUIComponents() {
		try {
			setContent(tc.getText());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public final void setGUIEnabled(final boolean enabled) {
		if (tc != null) tc.setEnabled(enabled);
	}

	protected final JTextComponent getTc() {
		return tc;
	}

	protected final void setTc(final JTextComponent tc) {
		this.tc = tc;
		tc.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(final UndoableEditEvent e) {
				if (!isInternalMod()) invokeChangingContent(getTc().getText());
			}
		});
	}

	protected final boolean isInternalMod() {
		return internalMod;
	}

}
