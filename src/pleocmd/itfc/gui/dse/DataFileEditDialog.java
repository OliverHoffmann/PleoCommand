package pleocmd.itfc.gui.dse;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.AutoDisposableWindow;
import pleocmd.itfc.gui.HelpDialog;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.MainFrame;
import pleocmd.itfc.gui.Layouter.Button;

public final class DataFileEditDialog extends JDialog implements
		ConfigurationInterface, AutoDisposableWindow {

	private static final long serialVersionUID = 7860184232803195768L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final DataSequenceEditorPanel dsePanel;

	private final JButton btnHelp;

	private final JButton btnSave;

	private final JButton btnLoad;

	private final JButton btnOk;

	private final JButton btnApply;

	private final JButton btnCancel;

	public DataFileEditDialog(final File file) {
		setTitle("TextEditor - " + file.getPath());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);
		dsePanel = new DataSequenceEditorPanel() {

			private static final long serialVersionUID = 4031934645004418741L;

			@Override
			protected void stateChanged() {
				updateStatus();
			}

		};
		lay.addWholeLine(dsePanel, true);

		btnHelp = lay.addButton(Button.Help, Layouter.help(this, getClass()
				.getSimpleName()));
		btnSave = lay.addButton(Button.SaveTo, "Save the content to a file",
				new Runnable() {
					@Override
					public void run() {
						saveToFile();
					}
				});
		btnLoad = lay.addButton(Button.LoadFrom, "Load content from a file",
				new Runnable() {
					@Override
					public void run() {
						loadFromFile();
					}
				});
		lay.addSpacer();
		btnOk = lay.addButton(Button.Ok, new Runnable() {
			@Override
			public void run() {
				writeTextPaneToFile(file);
				close();
			}
		});
		getRootPane().setDefaultButton(btnOk);
		btnApply = lay.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				writeTextPaneToFile(file);
				getDsePanel().getTpUndoManager().discardAllEdits();
				getDsePanel().updateState();
			}
		});
		btnCancel = lay.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				close();
			}
		});

		updateTextPaneFromFile(file);

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("DataFileEditDialog created");
		updateStatus();
		MainFrame.the().addKnownWindow(this);
		setModal(true);
		HelpDialog.closeHelpIfOpen();
		setVisible(true);
		dsePanel.freeResources();
	}

	protected void loadFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				final String name = f.getName();
				return !name.endsWith(".pbd") && !name.endsWith(".pca");
			}

			@Override
			public String getDescription() {
				return "Ascii-Textfile containing Data-List";
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			updateTextPaneFromFile(fc.getSelectedFile());
	}

	protected void saveToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(true);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Ascii-Textfile containing Data-List", "pad"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pad");
			writeTextPaneToFile(file);
		}
	}

	protected void close() {
		try {
			Configuration.getMain().unregisterConfigurableObject(this);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
		MainFrame.the().removeKnownWindow(this);
		dispose();
		HelpDialog.closeHelpIfOpen();
	}

	@Override
	public void autoDispose() {
		close();
	}

	protected void writeTextPaneToFile(final File file) {
		Log.detail("Writing TextPane to file '%s'", file);
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(file));
			try {
				dsePanel.writeTextPaneToWriter(out);
			} finally {
				out.close();
			}
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void updateTextPaneFromFile(final File file) {
		Log.detail("Updating TextPane from file '%s'", file);
		try {
			if (file.exists()) {
				final BufferedReader in = new BufferedReader(new FileReader(
						file));
				try {
					dsePanel.updateTextPaneFromReader(in);
				} finally {
					in.close();
				}
			} else
				dsePanel.updateTextPaneFromReader(null);
		} catch (final IOException e) {
			Log.error(e);
		}
		dsePanel.updateState();
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(cfgBounds);
	}

	@Override
	public void configurationAboutToBeChanged() {
		// nothing to do
	}

	@Override
	public void configurationRead() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		cfgBounds.assignContent(this);
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		cfgBounds.setContent(getBounds());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

	public void updateStatus() {
		btnHelp.setEnabled(true);
		btnSave.setEnabled(dsePanel.getTpDataSequence().getDocument()
				.getLength() > 0);
		btnLoad.setEnabled(true);
		btnOk.setEnabled(true);
		btnApply.setEnabled(dsePanel.getTpUndoManager().canUndo());
		btnCancel.setEnabled(true);

	}

	protected DataSequenceEditorPanel getDsePanel() {
		return dsePanel;
	}

}
