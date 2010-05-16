package pleocmd.itfc.gui.dse;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

public final class DataFileBinaryDialog extends JDialog implements
		ConfigurationInterface, AutoDisposableWindow {

	private static final long serialVersionUID = 7106692123051074764L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final DataSequenceBinaryPanel dsbPanel;

	private final JButton btnHelp;

	private final JButton btnSave;

	private final JButton btnLoad;

	private final JButton btnOk;

	private final JButton btnApply;

	private final JButton btnCancel;

	public DataFileBinaryDialog(final File file) {
		setTitle("Binary Editor - " + file.getPath());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);
		dsbPanel = new DataSequenceBinaryPanel() {

			private static final long serialVersionUID = -1868980685873882750L;

			@Override
			protected void stateChanged() {
				updateStatus();
			}

		};
		lay.addWholeLine(dsbPanel, true);

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
				writeHexTableToFile(file);
				close();
			}
		});
		getRootPane().setDefaultButton(btnOk);
		btnApply = lay.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				writeHexTableToFile(file);
				getDsbPanel().updateState();
			}
		});
		btnCancel = lay.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				close();
			}
		});

		updateHexTableFromFile(file);

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("DataFileBinaryDialog created");
		updateStatus();
		MainFrame.the().addKnownWindow(this);
		setModal(true);
		HelpDialog.closeHelpIfOpen();
		setVisible(true);
		dsbPanel.freeResources();
	}

	protected void loadFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Binary file containing Data-List", "pbd"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			updateHexTableFromFile(fc.getSelectedFile());
	}

	protected void saveToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(true);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Binary file containing Data-List", "pbd"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pbd");
			writeHexTableToFile(file);
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

	protected void writeHexTableToFile(final File file) {
		Log.detail("Writing HexTable to file '%s'", file);
		try {
			final RandomAccess stream = dsbPanel.getTableStream();
			final byte[] buf = new byte[(int) stream.length()];
			stream.seek(0);
			stream.getDataInput().readFully(buf);
			final FileOutputStream out = new FileOutputStream(file);
			try {
				out.write(buf);
			} finally {
				out.close();
			}
			dsbPanel.resetModification();
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void updateHexTableFromFile(final File file) {
		Log.detail("Updating HexTable from file '%s'", file);
		try {
			if (file.exists()) {
				final FileInputStream in = new FileInputStream(file);
				dsbPanel.setTableToStream(new RandomAccessArray(in, file
						.length()));
				in.close();
			} else
				dsbPanel.setTableToStream(new RandomAccessArray(null, 0));
		} catch (final IOException e) {
			Log.error(e);
		}
		dsbPanel.updateState();
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
		try {
			btnSave.setEnabled(dsbPanel.getTableStream() != null
					&& dsbPanel.getTableStream().length() > 0);
		} catch (final IOException e) {
			btnSave.setEnabled(false);
			Log.error(e);
		}
		btnLoad.setEnabled(true);
		btnOk.setEnabled(true);
		btnApply.setEnabled(dsbPanel.isModified());
		btnCancel.setEnabled(true);

	}

	protected DataSequenceBinaryPanel getDsbPanel() {
		return dsbPanel;
	}

}
