package pleocmd.itfc.gui;

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import pleocmd.Log;
import pleocmd.exc.OutputException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Data;
import pleocmd.pipe.DataSequenceMap;
import pleocmd.pipe.out.ConsoleOutput;
import pleocmd.pipe.out.Output;
import pleocmd.pipe.out.PleoRXTXOutput;

// CS_IGNORE_NEXT The classes this one relies on are mainly GUI components
public final class DataSequenceEditorFrame extends JDialog {

	private static final long serialVersionUID = -5729115559356740425L;

	private final File file;

	private final DataSequenceMap map = new DataSequenceMap();

	private final JComboBox cbTrigger;

	private final JTextPane tpDataSequence;

	private final DefaultComboBoxModel cbModel;

	private List<Output> playOutputList;

	// CS_IGNORE_NEXT Contains only GUI component creation
	public DataSequenceEditorFrame(final File file) {
		this.file = file;

		Log.detail("Creating DataSequenceEditorFrame");
		setTitle("Edit Data Sequence");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Add components
		final Layouter lay = new Layouter(this);
		cbModel = new DefaultComboBoxModel(new Vector<String>());
		cbTrigger = new JComboBox(cbModel);
		cbTrigger.setEditable(true);
		cbTrigger.setMaximumRowCount(2);
		cbTrigger.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if ("comboBoxEdited".equals(e.getActionCommand())) {
					getMap().addTrigger(
							getCBTrigger().getSelectedItem().toString());
					updateComboBoxModel();
				}
			}

		});
		cbTrigger.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					updateTextPaneFromMap(e.getItem());
				else
					writeTextPaneToMap(e.getItem());
			}
		});
		lay.addWholeLine(cbTrigger, false);

		tpDataSequence = new JTextPane();
		lay.addWholeLine(new JScrollPane(tpDataSequence), true);

		lay.addSpacer();

		lay.setSpan(2);
		lay.addButton("Copy From Input History", "bookmark-new.png", "",
				new Runnable() {
					@Override
					public void run() {
						addFromInputHistory();
					}
				});
		lay.addButton("Add From File ...", "edit-text-frame-update.png", "",
				new Runnable() {
					@Override
					public void run() {
						addFromFile();
					}
				});
		lay.setSpan(3);
		lay.addSpacer();
		lay.setSpan(1);

		lay.newLine();

		lay.addSpacer();

		lay.addButton("Play Selected", "unknownapp",
				"Sends all currently selected data to "
						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
					@Override
					public void run() {
						playSelected();
					}
				});
		lay.addButton("Play All", "unknownapp",
				"Sends all data in the list to "
						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
					@Override
					public void run() {
						playAll();
					}
				});
		lay.addButton(Button.Undo, new Runnable() {
			@Override
			public void run() {
				undo();
			}
		});
		lay.addButton(Button.Redo, new Runnable() {
			@Override
			public void run() {
				redo();
			}
		});
		lay.addButton(Button.Ok, new Runnable() {
			@Override
			public void run() {
				saveChanges();
				dispose();
			}
		});
		lay.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				saveChanges();
			}
		});
		lay.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				dispose();
			}
		});

		// Center window on screen
		setSize(800, 400);
		setLocationRelativeTo(null);

		map.reset();
		addSequenceListFromFile(file);

		Log.detail("DataSequenceEditorFrame created");
		setModal(true);
		setVisible(true);
	}

	protected DataSequenceMap getMap() {
		return map;
	}

	protected JComboBox getCBTrigger() {
		return cbTrigger;
	}

	public void addFromInputHistory() {
		try {
			final StyledDocument doc = tpDataSequence.getStyledDocument();
			final int offset = doc.getParagraphElement(
					tpDataSequence.getCaretPosition()).getEndOffset();
			for (final String data : MainFrame.the().getHistory())
				doc.insertString(offset, data + "\n", null);
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void addFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"CommandSequenceList", "csl"));
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return !f.getName().endsWith(".csl");
			}

			@Override
			public String getDescription() {
				return "Ascii-Textfile containing Data-List";
			}
		});
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
			final StyledDocument doc = tpDataSequence.getStyledDocument();
			final int offset = doc.getParagraphElement(
					tpDataSequence.getCaretPosition()).getEndOffset();
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
		 * TODO implement play final Object triggerName =
		 * cbTrigger.getSelectedItem(); writeTextPaneToMap(triggerName); if
		 * (triggerName != null) { final List<String> trigger =
		 * map.get(triggerName); if (trigger != null) for (final String command
		 * : trigger) play(command); }
		 */
	}

	public void playAll() {
		/*
		 * TODO implement play final Object triggerName =
		 * cbTrigger.getSelectedItem(); writeTextPaneToMap(triggerName); if
		 * (triggerName != null) { final List<String> trigger =
		 * map.get(triggerName); if (trigger != null) for (final String command
		 * : trigger) play(command); }
		 */
	}

	public void play(final Data data) {
		if (playOutputList == null) {
			playOutputList = new ArrayList<Output>(2);
			playOutputList.add(new ConsoleOutput());
			playOutputList.add(new PleoRXTXOutput());
		}
		try {
			for (final Output out : playOutputList)
				out.write(data);
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

	protected void updateComboBoxModel() {
		final Object lastSelected = cbTrigger.getSelectedItem();
		cbModel.removeAllElements();
		for (final String trigger : map.getAllTriggers())
			cbModel.addElement(trigger);
		if (lastSelected == null || cbModel.getIndexOf(lastSelected) >= 0)
			getCBTrigger().setSelectedItem(lastSelected);
		updateTextPaneFromMap(getCBTrigger().getSelectedItem());
	}

	protected void writeTextPaneToMap(final Object triggerName) {
		try {
			if (triggerName == null) {
				if (tpDataSequence.getDocument().getLength() == 0) return;
				throw new IOException("No name selected in ComboBox");
			}
			map.clearDataList(triggerName.toString());
			final BufferedReader in = new BufferedReader(new StringReader(
					tpDataSequence.getText()));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && line.charAt(0) != '#')
					map.addData(triggerName.toString(), Data
							.createFromAscii(line));
			}
			in.close();
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	protected void updateTextPaneFromMap(final Object triggerName) {
		try {
			final StyledDocument doc = tpDataSequence.getStyledDocument();
			doc.remove(0, doc.getLength());
			if (triggerName != null) {
				final List<Data> dataList = map.getDataList(triggerName
						.toString());
				if (dataList != null)
					for (final Data data : dataList)
						doc.insertString(doc.getLength(), data.toString()
								+ "\n", null);
			}
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

}
