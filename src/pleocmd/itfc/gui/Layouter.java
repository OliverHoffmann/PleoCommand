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

package pleocmd.itfc.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;

import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.icons.IconLoader;

public final class Layouter {

	public enum Button {
		Ok, Apply, Cancel, Close, Revert, Help, Add, Remove, Clear, Modify, Up, Down, Undo, Redo, Browse, SaveTo, LoadFrom
	}

	private static final class ButtonText {
		final String title; // CS_IGNORE
		final String icon;// CS_IGNORE
		final String tooltip;// CS_IGNORE

		protected ButtonText(final String title, final String icon,
				final String tooltip) {
			this.title = title;
			this.icon = icon;
			this.tooltip = tooltip;
		}
	}

	private static final Map<Button, ButtonText> DEFAULT = new HashMap<Button, ButtonText>();

	private static final int MAX_ROWS = 10 * 1024;

	private static final int LAST_ROW = MAX_ROWS - 1;

	private final Container container;

	private final GridBagConstraints gbc;

	private int span = 1;

	private int gridYStored;

	static {
		DEFAULT.put(Button.Ok, new ButtonText("Ok", "dialog-ok",
				"Accept the modifications and close this dialog"));
		DEFAULT.put(Button.Apply, new ButtonText("Apply", "dialog-ok-apply",
				"Accept the modifications"));
		DEFAULT.put(Button.Cancel, new ButtonText("Cancel", "dialog-cancel",
				"Close this dialog without accepting any modifications"));
		DEFAULT.put(Button.Close, new ButtonText("Close", "dialog-ok",
				"Close this dialog while keeping any modifications"));
		DEFAULT.put(Button.Revert, new ButtonText("Revert", "dialog-cancel",
				"Close this dialog and revert any modifications"));
		DEFAULT.put(Button.Help, new ButtonText("Help", "help-contents",
				"Display context sensitive help"));

		DEFAULT.put(Button.Add, new ButtonText("Add", "list-add",
				"Add a new entry to the list"));
		DEFAULT.put(Button.Remove, new ButtonText("Remove", "list-remove",
				"Remove all selected entries from the list"));
		DEFAULT.put(Button.Clear, new ButtonText("Clear", "archive-remove",
				"Remove all entries from the list"));
		DEFAULT.put(Button.Modify, new ButtonText("Modify", "document-edit",
				"Modify the currently focused entry in the list"));

		DEFAULT.put(Button.Up, new ButtonText("Up", "arrow-up",
				"Move the currently focused entry one position upwards"));
		DEFAULT.put(Button.Down, new ButtonText("Down", "arrow-down",
				"Move the currently focused entry one position downwards"));

		DEFAULT.put(Button.Undo, new ButtonText("Undo", "edit-undo",
				"Undo the last action"));
		DEFAULT.put(Button.Redo, new ButtonText("Redo", "edit-redo",
				"Repeat the last undone action"));

		DEFAULT.put(Button.Browse, new ButtonText("...", "edit-rename",
				"Open a file selection dialog"));

		DEFAULT.put(Button.SaveTo, new ButtonText("Save To ...",
				"document-save-as", "Save the document to a file"));
		DEFAULT.put(Button.LoadFrom,
				new ButtonText("Load From ...", "document-open",
						"Load a previously saved document from a file"));
	}

	public Layouter(final Container container) {
		this.container = container;
		container.setLayout(new GridBagLayout());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.ipadx = 0;
		gbc.ipady = 0;
	}

	public Container getContainer() {
		return container;
	}

	public void add(final Component comp, final boolean greedyX) {
		gbc.gridwidth = span;
		gbc.weightx = greedyX ? 1.0 : 0.0;
		gbc.weighty = 0.0;
		container.add(comp, gbc);
		gbc.gridx += span;
	}

	public void addWholeLine(final Component comp, final boolean greedyY) {
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = greedyY ? 1.0 : 0.0;
		container.add(comp, gbc);
		newLine();
	}

	/**
	 * The following components will always appear in the last row no matter how
	 * many components will be inserted afterwards.<br>
	 * This mode stops (and Components will be inserted on the current position
	 * again) after {@link #newLine()} or
	 * {@link #addWholeLine(Component, boolean)} or {@link #addVerticalSpacer()}
	 * is invoked.
	 */
	public void nextComponentsAlwaysOnLastLine() {
		gridYStored = gbc.gridy;
		gbc.gridy = LAST_ROW;
	}

	public void newLine() {
		gbc.gridx = 0;
		if (gbc.gridy == LAST_ROW)
			gbc.gridy = gridYStored;
		else if (++gbc.gridy == LAST_ROW)
			throw new InternalException("Too many rows in the Layouter");
	}

	public void addSpacer() {
		add(new JLabel(), true);
	}

	public void addVerticalSpacer() {
		addWholeLine(new JLabel(), true);
	}

	public JButton addButton(final Button button, final Runnable run) {
		final ButtonText bt = DEFAULT.get(button);
		if (bt == null) throw new RuntimeException("Missing button in map");
		return addButton(bt.title, bt.icon, bt.tooltip, run);
	}

	public JButton addButton(final Button button, final String tooltip,
			final Runnable run) {
		final ButtonText bt = DEFAULT.get(button);
		if (bt == null) throw new RuntimeException("Missing button in map");
		return addButton(bt.title, bt.icon, tooltip, run);
	}

	public JButton addButton(final String title, final String icon,
			final String tooltip, final Runnable run) {
		final JButton btn = new JButton(title, IconLoader.getIcon(icon));
		btn.setToolTipText(tooltip);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				run.run();
			}
		});
		add(btn, false);
		return btn;
	}

	public void setSpan(final int span) {
		this.span = span;
	}

	public int getSpan() {
		return span;
	}

	public void clear() {
		container.removeAll();
	}

	public static Runnable help(final Window owner, final String category) {
		return new Runnable() {
			@Override
			public void run() {
				HelpDialog.the(owner).display(category);
			}
		};
	}

}
