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

import java.awt.Dimension;
import java.text.ParseException;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

abstract class ConfigNumber<E extends Number> extends ConfigValue {

	private E content;

	private final E min;

	private final E max;

	private final E step;

	private JSpinner sp;

	private boolean internalMod;

	public ConfigNumber(final String label, final E min, final E max,
			final E step) {
		super(label);
		if (lessThan(max, min))
			throw new IllegalArgumentException(String.format(
					"min (%s) must not be larger than max (%s)", min, max));
		this.min = min;
		this.max = max;
		this.step = step;
		try {
			setContent(min);
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	protected abstract boolean lessThan(E nr1, E nr2);

	protected abstract E valueOf(String str) throws ConfigurationException;

	@Override
	public final E getContent() {
		return content;
	}

	public final void setContent(final E content) throws ConfigurationException {
		if (lessThan(content, min) || lessThan(max, content))
			throw new ConfigurationException("%s not between %s and %s",
					content, min, max);
		this.content = content;
		if (sp != null) sp.setValue(content);
	}

	public final E getContentGUI() {
		try {
			return sp == null ? content : valueOf(sp.getValue().toString());
		} catch (final ConfigurationException e) {
			return null;
		}
	}

	public final void setContentGUI(final E content) {
		internalMod = true;
		try {
			if (sp != null) sp.setValue(content);
		} finally {
			internalMod = false;
		}
	}

	public final E getMin() {
		return min;
	}

	public final E getMax() {
		return max;
	}

	@Override
	public final String asString() {
		return String.valueOf(content);
	}

	@Override
	final void setFromString(final String string) throws ConfigurationException {
		try {
			setContent(valueOf(string));
		} catch (final NumberFormatException e) {
			throw new ConfigurationException("Invalid number: '%s'", string);
		}
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
	final boolean isSingleLined() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	// all Number implementations are Comparable to themselves
	// but we can't express this in Java genericals :(
	public final boolean insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, (Comparable<E>) min,
				(Comparable<E>) max, step));
		sp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!isInternalMod())
					invokeChangingContent(getSp().getValue());
			}
		});
		((DefaultEditor) sp.getEditor()).getTextField().getDocument()
				.addUndoableEditListener(new UndoableEditListener() {
					@Override
					public void undoableEditHappened(final UndoableEditEvent e) {
						if (!isInternalMod()) try {
							getSp().commitEdit();
							invokeChangingContent(getSp().getValue());
						} catch (final ParseException exc) {
							// silently ignore invalid content here
						}
					}
				});
		sp.setPreferredSize(new Dimension(150, sp.getMinimumSize().height));
		lay.add(sp, true);
		invokeChangingContent(sp.getValue());
		return false;
	}

	@Override
	public final void setFromGUIComponents() {
		try {
			sp.commitEdit();
			setContent(valueOf(sp.getValue().toString()));
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		} catch (final ParseException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public void setGUIEnabled(final boolean enabled) {
		if (sp != null) sp.setEnabled(enabled);
	}

	protected JSpinner getSp() {
		return sp;
	}

	protected boolean isInternalMod() {
		return internalMod;
	}

}
