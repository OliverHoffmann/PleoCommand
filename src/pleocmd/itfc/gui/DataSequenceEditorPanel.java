package pleocmd.itfc.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import pleocmd.Log;
import pleocmd.api.PleoCommunication;
import pleocmd.cfg.ConfigItem;
import pleocmd.exc.FormatException;
import pleocmd.exc.InternalException;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.out.ConsoleOutput;
import pleocmd.pipe.out.Output;
import pleocmd.pipe.out.PleoRXTXOutput;
import pleocmd.pipe.out.PrintType;

public final class DataSequenceEditorPanel extends JPanel {

	private static final long serialVersionUID = -3019900508373635307L;

	private final JTextPane tpDataSequence;

	private final UndoManager tpUndoManager;

	private final JButton btnCopyInput;

	private final JButton btnAddFile;

	private final JButton btnPlaySel;

	private final JButton btnPlayAll;

	private final JButton btnUndo;

	private final JButton btnRedo;

	private List<Output> playOutputList;

	public DataSequenceEditorPanel(final Window owner,
			final Runnable saveChanges, final Runnable close) {
		final Layouter lay = new Layouter(this);

		tpUndoManager = new UndoManager();
		tpDataSequence = new JTextPane();
		tpDataSequence.setPreferredSize(new Dimension(0, 10 * tpDataSequence
				.getFontMetrics(tpDataSequence.getFont()).getHeight()));
		tpDataSequence.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(final CaretEvent e) {
				updateState();
			}
		});
		tpDataSequence.getDocument().addUndoableEditListener(
				new UndoableEditListener() {
					@Override
					public void undoableEditHappened(final UndoableEditEvent e) {
						addUndo(e.getEdit());
					}
				});
		tpDataSequence.setEditorKitForContentType("text/datasequence",
				new DataSequenceEditorKit());
		tpDataSequence.setContentType("text/datasequence");
		tpDataSequence.setFont(tpDataSequence.getFont().deriveFont(Font.BOLD));

		lay.addWholeLine(new JScrollPane(tpDataSequence), true);

		lay.setSpan(2);
		btnCopyInput = lay.addButton("Copy From Input History",
				"bookmark-new.png",
				"Copy all history from console input into this Data list",
				new Runnable() {
					@Override
					public void run() {
						addFromInputHistory();
					}
				});
		btnAddFile = lay.addButton("Add From File ...",
				"edit-text-frame-update.png",
				"Copy the contents of a file into this Data list",
				new Runnable() {
					@Override
					public void run() {
						addFromFile();
					}
				});
		lay.addButton(Button.Help, Layouter.help(owner, getClass()
				.getSimpleName()));
		lay.setSpan(1);
		lay.addSpacer();

		lay.newLine();

		btnPlaySel = lay.addButton("Play Selected", "unknownapp",
				"Sends all currently selected data to "
						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
					@Override
					public void run() {
						playSelected();
					}
				});
		btnPlayAll = lay.addButton("Play All", "unknownapp",
				"Sends all data in the list to "
						+ "ConsoleOutput and PleoRXTXOutput", new Runnable() {
					@Override
					public void run() {
						playAll();
					}
				});
		btnUndo = lay.addButton(Button.Undo, new Runnable() {
			@Override
			public void run() {
				undo();
			}
		});
		btnRedo = lay.addButton(Button.Redo, new Runnable() {
			@Override
			public void run() {
				redo();
			}
		});
		lay.addButton(Button.Ok, new Runnable() {
			@Override
			public void run() {
				saveChanges.run();
				close.run();
			}
		});
		lay.addButton(Button.Apply, saveChanges);
		lay.addSpacer();
		lay.addButton(Button.Cancel, close);
	}

	public void addFromInputHistory() {
		try {
			final StyledDocument doc = tpDataSequence.getStyledDocument();
			final int offset = doc.getParagraphElement(
					tpDataSequence.getCaretPosition()).getEndOffset();
			for (final String data : MainFrame.the().getMainInputPanel()
					.getHistoryListModel().getAll())
				doc.insertString(offset, data + "\n", null);
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void addFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return !f.getName().endsWith(".cfg"); // TODO extension?
			}

			@Override
			public String getDescription() {
				return "Ascii-Textfile containing Data-List";
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			addSequenceFromFile(fc.getSelectedFile());
	}

	public void addSequenceFromFile(final File fileToAdd) {
		try {
			final StyledDocument doc = tpDataSequence.getStyledDocument();
			int offset = doc.getParagraphElement(
					tpDataSequence.getCaretPosition()).getEndOffset();
			final BufferedReader in = new BufferedReader(new FileReader(
					fileToAdd));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim() + "\n";
				doc.insertString(offset, line, null);
				offset += line.length();
			}
			in.close();
		} catch (final IOException e) {
			Log.error(e);
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void clear() {
		final StyledDocument doc = tpDataSequence.getStyledDocument();
		try {
			doc.remove(0, doc.getLength());
		} catch (final BadLocationException e) {
			Log.error(e);
		}
	}

	public void playSelected() {
		final String sel = tpDataSequence.getSelectedText();
		if (sel != null) for (final String line : sel.split("\n"))
			try {
				play(Data.createFromAscii(line));
			} catch (final IOException e) {
				Log.error(e);
			} catch (final FormatException e) {
				Log.error(e);
			}
	}

	public void playAll() {
		final String text = tpDataSequence.getText();
		if (text != null) for (final String line : text.split("\n"))
			try {
				play(Data.createFromAscii(line));
			} catch (final IOException e) {
				Log.error(e);
			} catch (final FormatException e) {
				Log.error(e);
			}
	}

	public void play(final Data data) {
		if (playOutputList == null)
			try {
				String device = null;
				for (final Output out : Pipe.the().getOutputList())
					if (out instanceof PleoRXTXOutput)
						device = ((ConfigItem<?>) out.getGroup().get("Device"))
								.getContent();
				if (device == null)
					device = JOptionPane.showInputDialog(this,
							"Set Pleo device name for playback:",
							PleoCommunication.getHighestPort().getName());
				if (device == null) return;
				playOutputList = new ArrayList<Output>(2);
				final Output outC = new ConsoleOutput(PrintType.DataAscii);
				outC.configure();
				outC.init();
				playOutputList.add(outC);
				final Output outP = new PleoRXTXOutput(device);
				outP.configure();
				outP.init();
				playOutputList.add(outP);

			} catch (final Throwable e) { // CS_IGNORE catch all here
				Log.error(e);
			}

		try {
			for (final Output out : playOutputList)
				out.write(data);
		} catch (final Throwable e) { // CS_IGNORE catch all here
			Log.error(e);
		}
	}

	public void freeResources() {
		try {
			if (playOutputList != null) for (final Output out : playOutputList)
				out.close();
		} catch (final PipeException e) {
			Log.error(e);
		}
	}

	protected void addUndo(final UndoableEdit edit) {
		if (edit.isSignificant()) {
			tpUndoManager.addEdit(edit);
			updateState();
		}
	}

	public void undo() {
		try {
			tpUndoManager.undo();
		} catch (final CannotUndoException e) {
			Log.error(e);
		}
	}

	public void redo() {
		try {
			tpUndoManager.redo();
		} catch (final CannotRedoException e) {
			Log.error(e);
		}
	}

	protected List<Data> writeTextPaneToList() throws IOException,
			FormatException {
		final List<Data> res = new ArrayList<Data>();
		final BufferedReader in = new BufferedReader(new StringReader(
				tpDataSequence.getText()));
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty() && line.charAt(0) != '#')
				res.add(Data.createFromAscii(line));
		}
		in.close();
		return res;
	}

	protected void writeTextPaneToWriter(final Writer out) throws IOException {
		final BufferedReader in = new BufferedReader(new StringReader(
				tpDataSequence.getText()));
		String line;
		while ((line = in.readLine()) != null) {
			out.write(line);
			out.write('\n');
		}
		in.close();
		out.flush();
	}

	protected void updateTextPaneFromList(final List<Data> dataList) {
		clear();
		final StyledDocument doc = tpDataSequence.getStyledDocument();
		if (dataList != null)
			try {
				for (final Data data : dataList)
					doc.insertString(doc.getLength(), data.toString() + "\n",
							null);
			} catch (final BadLocationException e) {
				throw new InternalException(e);
			}
		tpUndoManager.discardAllEdits();
		updateState(); // TODO needed?
	}

	protected void updateTextPaneFromReader(final BufferedReader in)
			throws IOException {
		clear();
		final StyledDocument doc = tpDataSequence.getStyledDocument();
		if (in != null) try {
			String line;
			while ((line = in.readLine()) != null)
				doc.insertString(doc.getLength(), line + "\n", null);
		} catch (final BadLocationException e) {
			throw new InternalException(e);
		}
		tpUndoManager.discardAllEdits();
		updateState(); // TODO needed?
	}

	public void updateState() {
		tpDataSequence.setEnabled(true);
		btnCopyInput.setEnabled(MainFrame.the().getMainInputPanel()
				.getHistoryListModel().getSize() > 0);
		btnAddFile.setEnabled(true);
		btnPlaySel.setEnabled(tpDataSequence.getSelectedText() != null);
		btnPlayAll.setEnabled(tpDataSequence.getDocument().getLength() > 0);
		btnUndo.setEnabled(tpUndoManager.canUndo());
		btnRedo.setEnabled(tpUndoManager.canRedo());
	}

}
