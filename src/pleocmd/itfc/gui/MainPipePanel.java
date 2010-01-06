package pleocmd.itfc.gui;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;

public final class MainPipePanel extends JPanel {

	private static final long serialVersionUID = 5361715509143723415L;

	private final JLabel pipeLabel;

	private final JButton btnModify;

	private final JButton btnSave;

	private final JButton btnLoad;

	public MainPipePanel() {
		final Layouter lay = new Layouter(this);
		pipeLabel = new JLabel();
		updatePipeLabel();
		lay.addWholeLine(pipeLabel, false);

		btnModify = lay.addButton(Button.Modify,
				"Modify the currently active pipe", new Runnable() {
					@Override
					public void run() {
						changeConfig();
					}
				});
		lay.addSpacer();
		btnSave = lay.addButton(Button.SaveTo,
				"Save the current pipe to a file", new Runnable() {
					@Override
					public void run() {
						writeConfigToFile();
					}
				});
		btnLoad = lay.addButton(Button.LoadFrom,
				"Load a previously saved pipe from a file", new Runnable() {
					@Override
					public void run() {
						readConfigFromFile();
					}
				});
	}

	public void updatePipeLabel() {
		pipeLabel.setText(String.format(
				"Pipe has %d input%s, %d converter and %d output%s", Pipe.the()
						.getInputList().size(), Pipe.the().getInputList()
						.size() == 1 ? "" : "s", Pipe.the().getConverterList()
						.size(), Pipe.the().getOutputList().size(), Pipe.the()
						.getOutputList().size() == 1 ? "" : "s"));
	}

	public void changeConfig() {
		Log.detail("GUI-Frame starts configuration");
		new PipePartConfigFrame();
		Log.detail("GUI-Frame is done with configuration");
	}

	public void writeConfigToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pca");
			writeConfigToFile(file);
		}
	}

	public void writeConfigToFile(final File file) {
		try {
			// TODO write only pipe specific groups
			Configuration.the().writeToFile(file);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	public void readConfigFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			readConfigFromFile(fc.getSelectedFile());
	}

	public void readConfigFromFile(final File file) {
		try {
			// TODO read only pipe-specific groups
			Configuration.the().readFromFile(file);
			updatePipeLabel();
			Configuration.the().writeToDefaultFile();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	public void updateState() {
		btnModify.setEnabled(!MainFrame.the().isPipeRunning());
		btnSave.setEnabled(Pipe.the().getInputList().isEmpty()
				|| !Pipe.the().getConverterList().isEmpty()
				|| !Pipe.the().getOutputList().isEmpty());
		btnLoad.setEnabled(!MainFrame.the().isPipeRunning());
	}

}
