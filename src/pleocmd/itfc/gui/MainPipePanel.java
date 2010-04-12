package pleocmd.itfc.gui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.cfg.Configuration;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;

public final class MainPipePanel extends JPanel {

	private static final long serialVersionUID = 5361715509143723415L;

	private static final int THUMBNAIL_HEIGHT = 100;

	private final JLabel pipeLabel;

	private final JLabel thumbnailLabel;

	private final JButton btnModify;

	private final JButton btnSave;

	private final JButton btnLoad;

	private final Timer updateTimer;

	private TimerTask updateTimerTask;

	private PipeConfigDialog cfgDialog;

	public MainPipePanel() {
		final Layouter lay = new Layouter(this);
		updateTimer = new Timer();

		pipeLabel = new JLabel();
		lay.addWholeLine(pipeLabel, false);

		thumbnailLabel = new JLabel();
		thumbnailLabel.setBorder(BorderFactory.createBevelBorder(1));
		lay.addWholeLine(thumbnailLabel, false);

		updatePipeLabel();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				updatePipeLabel();
			}
		});

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
						writePipeConfigToFile();
					}
				});
		btnLoad = lay.addButton(Button.LoadFrom,
				"Load a previously saved pipe from a file", new Runnable() {
					@Override
					public void run() {
						readPipeConfigFromFile();
					}
				});
	}

	public void updatePipeLabel() {
		String fn = Pipe.the().getLastSaveFile().getName();
		if (fn.contains("."))
			fn = ": \"" + fn.substring(0, fn.lastIndexOf('.')) + "\"";
		pipeLabel.setText(String.format(
				"Pipe has %d input%s, %d converter and %d output%s%s", Pipe
						.the().getInputList().size(), Pipe.the().getInputList()
						.size() == 1 ? "" : "s", Pipe.the().getConverterList()
						.size(), Pipe.the().getOutputList().size(), Pipe.the()
						.getOutputList().size() == 1 ? "" : "s", fn));

		final int width = getWidth();
		if (width == 0)
			thumbnailLabel.setIcon(null);
		else {
			final BoardPainter painter = new BoardPainter();
			final BufferedImage img = new BufferedImage(width,
					THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
			painter.assignFromPipe(img.getGraphics(), false);
			final Dimension pref = painter.getPreferredSize();
			painter.setScale(Math.min((double) width / pref.width,
					(double) THUMBNAIL_HEIGHT / pref.height));
			painter.setBounds(pref.width, pref.height, false);
			painter.paint(img.getGraphics(), null, null, null, null, false,
					null, true);
			thumbnailLabel.setIcon(new ImageIcon(img));
		}

		if (cfgDialog != null) cfgDialog.updatePipeLabel();
	}

	public void changeConfig() {
		Log.detail("GUI-Frame starts configuration");
		if (cfgDialog == null)
			cfgDialog = new PipeConfigDialog();
		else
			cfgDialog.toFront();
	}

	public void writePipeConfigToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		fc.setSelectedFile(Pipe.the().getLastSaveFile());
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pca");
			writePipeConfigToFile(file);
		}
	}

	public void writePipeConfigToFile(final File file) {
		try {
			Configuration.the().writeToFile(file, Pipe.the());
			Pipe.the().setLastSaveFile(file);
			updatePipeLabel();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	public void readPipeConfigFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		fc.setSelectedFile(Pipe.the().getLastSaveFile());
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			readPipeConfigFromFile(fc.getSelectedFile());
	}

	public void readPipeConfigFromFile(final File file) {
		try {
			Configuration.the().readFromFile(file, Pipe.the());
			Configuration.the().writeToDefaultFile();
			Pipe.the().setLastSaveFile(file);
			updateState();
			updatePipeLabel();
			if (cfgDialog != null) cfgDialog.getBoard().assignFromPipe();
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
		if (cfgDialog != null) cfgDialog.updateState();
	}

	void configDialogDisposed() {
		cfgDialog = null;
	}

	public void timeUpdatePipeLabel() {
		if (updateTimerTask != null) return;
		updateTimerTask = new TimerTask() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				updateTimerTask = null;
				updatePipeLabel();
			}
		};
		updateTimer.schedule(updateTimerTask, 1000);
	}

}
