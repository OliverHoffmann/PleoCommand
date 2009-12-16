package pleocmd.itfc.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;

import pleocmd.itfc.gui.icons.IconLoader;

public final class Layouter {

	public enum Button {
		Ok, Apply, Cancel, Add, Remove, Clear, Modify, Up, Down, Undo, Redo, Browse, SaveTo, LoadFrom
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

	private final Container container;

	private final GridBagConstraints gbc;

	private int span = 1;

	static {
		DEFAULT.put(Button.Ok, new ButtonText("Ok", "dialog-ok",
				"Accept the modifications and close this dialog"));
		DEFAULT.put(Button.Apply, new ButtonText("Apply", "dialog-ok-apply",
				"Accept the modifications"));
		DEFAULT.put(Button.Cancel, new ButtonText("Cancel", "dialog-cancel",
				"Close this dialog without accepting any modifications"));

		DEFAULT.put(Button.Add, new ButtonText("Add", "list-add",
				"Adds a new entry to the list"));
		DEFAULT.put(Button.Remove, new ButtonText("Remove", "list-remove",
				"Removes all selected entries from the list"));
		DEFAULT.put(Button.Clear, new ButtonText("Clear", "archive-remove",
				"Removes all entries from the list"));
		DEFAULT.put(Button.Modify, new ButtonText("Modify", "document-edit",
				"Modifies the currently focused entry in the list"));

		DEFAULT.put(Button.Up, new ButtonText("Up", "arrow-up",
				"Moves the currently focused entry one position upwards"));
		DEFAULT.put(Button.Down, new ButtonText("Down", "arrow-down",
				"Moves the currently focused entry one position downwards"));

		DEFAULT.put(Button.Undo, new ButtonText("Undo", "edit-undo",
				"Undoes the last action"));
		DEFAULT.put(Button.Redo, new ButtonText("Redo", "edit-redo",
				"Repeats the last undone action"));

		DEFAULT.put(Button.Browse, new ButtonText("...", "edit-rename",
				"Opens a file selection dialog"));

		DEFAULT.put(Button.SaveTo, new ButtonText("Save To ...",
				"document-save-as", "Saves the document to a file"));
		DEFAULT.put(Button.LoadFrom, new ButtonText("Load From ...",
				"document-open",
				"Loads a previously saved document from a file"));
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

	public Component getContainer() {
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

	public void newLine() {
		gbc.gridx = 0;
		++gbc.gridy;
	}

	public void addSpacer() {
		add(new JLabel(), true);
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

}
