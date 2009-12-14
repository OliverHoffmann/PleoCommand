package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.CommandSequenceMap;
import pleocmd.pipe.cmd.Command;
import pleocmd.pipe.cmd.PleoMonitorCommand;
import pleocmd.pipe.out.ConsoleOutput;
import pleocmd.pipe.out.Output;
import pleocmd.pipe.out.PleoRXTXOutput;

public final class CommandSequenceEditorFrame extends JDialog {

	private static final long serialVersionUID = -5729115559356740425L;

	private final File file;

	private final CommandSequenceMap map = new CommandSequenceMap();

	private final JComboBox cbTrigger;

	private final JTextPane tpCommands;

	private final DefaultComboBoxModel cbModel;

	private List<Output> playOutputList;

	public CommandSequenceEditorFrame(final File file) {
		this.file = file;

		Log.detail("Creating CmdSeqEditor-Frame");
		setTitle("Edit Command Sequence");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.weighty = 0.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		// Add components
		cbModel = new DefaultComboBoxModel(new Vector<String>());
		cbTrigger = new JComboBox(cbModel);
		cbTrigger.setEditable(true);
		cbTrigger.setMaximumRowCount(2);
		cbTrigger.addActionListener(new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				if ("comboBoxEdited".equals(e.getActionCommand())) {
					map.addTrigger(cbTrigger.getSelectedItem().toString());
					updateComboBoxModel();
				}
			}

		});
		cbTrigger.addItemListener(new ItemListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					updateTextPaneFromMap(e.getItem());
				else
					writeTextPaneToMap(e.getItem());
			}
		});
		add(cbTrigger, gbc);

		++gbc.gridy;

		tpCommands = new JTextPane();
		gbc.weighty = 1.0;
		add(new JScrollPane(tpCommands), gbc);
		gbc.weighty = 0.0;

		++gbc.gridy;

		gbc.gridx = 0;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 1.0; // TODO
		gbc.gridwidth = 2;

		++gbc.gridx;
		final JButton btnFromInputHist = new JButton("Copy From Input History",
				IconLoader.getIcon("bookmark-new.png"));
		btnFromInputHist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				addFromInputHistory();
			}
		});
		add(btnFromInputHist, gbc);

		gbc.gridx += 2;
		final JButton btnAddFromFile = new JButton("Add From File ...",
				IconLoader.getIcon("edit-text-frame-update.png"));
		btnAddFromFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				addFromFile();
			}
		});
		add(btnAddFromFile, gbc);

		gbc.gridx += 3;
		gbc.gridwidth = 3;
		final JButton btnUnknown = new JButton("???", IconLoader
				.getIcon("xxx.png"));
		btnUnknown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO ...
			}
		});
		add(btnUnknown, gbc);

		++gbc.gridy;

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 1.0; // TODO

		++gbc.gridx;
		final JButton btnPlayOne = new JButton("Play Selected", IconLoader
				.getIcon("xxx.png"));
		btnPlayOne.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				playSelected();
			}
		});
		add(btnPlayOne, gbc);

		++gbc.gridx;
		final JButton btnPlayAll = new JButton("Play All", IconLoader
				.getIcon("xxx.png"));
		btnPlayAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				playAll();
			}
		});
		add(btnPlayAll, gbc);

		++gbc.gridx;
		final JButton btnUndo = new JButton("Undo", IconLoader
				.getIcon("edit-undo.png"));
		btnUndo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				undo();
			}
		});
		add(btnUndo, gbc);

		++gbc.gridx;
		final JButton btnRedo = new JButton("Redo", IconLoader
				.getIcon("edit-redo.png"));
		btnRedo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				redo();
			}
		});
		add(btnRedo, gbc);

		++gbc.gridx;
		final JButton btnOK = new JButton("OK", IconLoader
				.getIcon("dialog-ok.png"));
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				saveChanges();
				dispose();
			}
		});
		add(btnOK, gbc);

		++gbc.gridx;
		final JButton btnApply = new JButton("Apply", IconLoader
				.getIcon("dialog-ok-apply.png"));
		btnApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				saveChanges();
			}
		});
		add(btnApply, gbc);

		++gbc.gridx;
		final JButton btnCancel = new JButton("Cancel", IconLoader
				.getIcon("dialog-cancel.png"));
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		add(btnCancel, gbc);

		// Center window on screen
		setSize(700, 400);
		setLocationRelativeTo(null);

		map.reset();
		addSequenceListFromFile(file);

		Log.detail("CmdSeqEditor-Frame created");
		setModal(true);
		setVisible(true);
	}

	public void addFromInputHistory() {
		try {
			final StyledDocument doc = tpCommands.getStyledDocument();
			final int offset = doc.getParagraphElement(
					tpCommands.getCaretPosition()).getEndOffset();
			for (final String command : MainFrame.the().getHistory())
				doc.insertString(offset, command + "\n", null);
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void addFromFile() {
		final JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			if (fc.getSelectedFile().getName().endsWith(".csl"))
				addSequenceListFromFile(fc.getSelectedFile());
			else
				addSequenceFromFile(fc.getSelectedFile());
	}

	public void addSequenceListFromFile(final File fileToAdd) {
		try {
			map.addFromFile(fileToAdd);
		} catch (final IOException e) {
			Log.error(e);
		}

		// pass changes to JComboBox and JTextPane
		updateComboBoxModel();
	}

	public void addSequenceFromFile(final File fileToAdd) {
		try {
			final StyledDocument doc = tpCommands.getStyledDocument();
			final int offset = doc.getParagraphElement(
					tpCommands.getCaretPosition()).getEndOffset();
			final BufferedReader in = new BufferedReader(new FileReader(
					fileToAdd));
			String line;
			while ((line = in.readLine()) != null)
				doc.insertString(offset, line.trim() + "\n", null);
			in.close();
		} catch (final IOException e) {
			Log.error(e);
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void playSelected() {
		/*
		 * final Object triggerName = cbTrigger.getSelectedItem();
		 * writeTextPaneToMap(triggerName); if (triggerName != null) { final
		 * List<String> trigger = map.get(triggerName); // TODO only play
		 * selected if (trigger != null) for (final String command : trigger)
		 * play(command); }
		 */
	}

	public void playAll() {
		/*
		 * final Object triggerName = cbTrigger.getSelectedItem();
		 * writeTextPaneToMap(triggerName); if (triggerName != null) { final
		 * List<String> trigger = map.get(triggerName); if (trigger != null) for
		 * (final String command : trigger) play(command); }
		 */
	}

	public void play(final String command) {
		if (playOutputList == null) {
			playOutputList = new ArrayList<Output>(2);
			playOutputList.add(new ConsoleOutput());
			playOutputList.add(new PleoRXTXOutput());
		}
		final Command cmd = new PleoMonitorCommand(null, command);
		try {
			for (final Output out : playOutputList)
				out.writeCommand(cmd);
		} catch (final OutputException e) {
			Log.error(e);
		}
	}

	public void undo() {
		// TODO Auto-generated method stub

	}

	public void redo() {
		// TODO Auto-generated method stub

	}

	public void saveChanges() {
		writeTextPaneToMap(cbTrigger.getSelectedItem());
		try {
			map.writeToFile(file);
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void updateComboBoxModel() {
		final Object lastSelected = cbTrigger.getSelectedItem();
		cbModel.removeAllElements();
		for (final String trigger : map.getAllTriggers())
			cbModel.addElement(trigger);
		if (lastSelected == null || cbModel.getIndexOf(lastSelected) >= 0)
			cbTrigger.setSelectedItem(lastSelected);
		updateTextPaneFromMap(cbTrigger.getSelectedItem());
	}

	private void writeTextPaneToMap(final Object triggerName) {
		try {
			if (triggerName == null) {
				if (tpCommands.getDocument().getLength() == 0) return;
				throw new IOException("No name selected in ComboBox");
			}
			map.clearCommands(triggerName.toString());
			final BufferedReader in = new BufferedReader(new StringReader(
					tpCommands.getText()));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && line.charAt(0) != '#')
					map.addCommand(triggerName.toString(), line);
			}
			in.close();
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void updateTextPaneFromMap(final Object triggerName) {
		try {
			final StyledDocument doc = tpCommands.getStyledDocument();
			doc.remove(0, doc.getLength());
			if (triggerName != null) {
				final List<Command> commands = map.getCommands(triggerName
						.toString());
				if (commands != null)
					for (final Command command : commands)
						doc.insertString(doc.getLength(), command
								.asPleoMonitorCommand()
								+ "\n", null);
			}
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

}
