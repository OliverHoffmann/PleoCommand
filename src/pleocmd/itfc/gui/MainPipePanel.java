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

import java.awt.Dimension;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.cfg.Configuration;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter.Button;

public final class MainPipePanel extends JPanel {

	private static final long serialVersionUID = 5361715509143723415L;

	private final JLabel pipeLabel;

	private final PipePreviewLabel thumbnailLabel;

	private final JButton btnModify;

	private final JButton btnSave;

	private final JButton btnLoad;

	private static final Timer UPDATE_TIMER = new Timer(
			"Pipe Thumbnail Update Timer", true);

	private TimerTask updateTimerTask;

	private PipeConfigDialog cfgDialog;

	private PipeFlowVisualization pipeFlowVisualization;

	public MainPipePanel() {
		final Layouter lay = new Layouter(this);

		pipeLabel = new JLabel();
		lay.addWholeLine(pipeLabel, false);

		thumbnailLabel = new PipePreviewLabel();
		thumbnailLabel.setMinimumSize(new Dimension(0, 100));
		lay.addWholeLine(thumbnailLabel, false);

		updatePipeLabel();

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

	protected void updatePipeLabel() {
		String title = MainFrame.the().getPipe().getTitle();
		if (!title.isEmpty()) title = ": \"" + title + "\"";
		pipeLabel.setText(String.format(
				"Pipe has %d input%s, %d converter and %d output%s%s",
				MainFrame.the().getPipe().getInputList().size(), MainFrame
						.the().getPipe().getInputList().size() == 1 ? "" : "s",
				MainFrame.the().getPipe().getConverterList().size(), MainFrame
						.the().getPipe().getOutputList().size(),
				MainFrame.the().getPipe().getOutputList().size() == 1 ? ""
						: "s", title));

		thumbnailLabel.update(MainFrame.the().getPipe());
		if (cfgDialog != null) cfgDialog.updatePipeLabel();
	}

	protected void changeConfig() {
		Log.detail("GUI-Frame starts configuration");
		if (cfgDialog == null) {
			cfgDialog = new PipeConfigDialog(MainFrame.the().getPipe());
			pipeFlowVisualization = new PipeFlowVisualization(
					cfgDialog.getBoard());
		} else
			cfgDialog.toFront();
	}

	protected void writePipeConfigToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		fc.setSelectedFile(MainFrame.the().getPipe().getLastSaveFile());
		new PipePreviewAccessory(fc);
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().contains("."))
				file = new File(file.getPath() + ".pca");
			writePipeConfigToFile(file);
		}
	}

	protected void writePipeConfigToFile(final File file) {
		try {
			Configuration.getMain()
					.writeToFile(file, MainFrame.the().getPipe());
			MainFrame.the().getPipe().setLastSaveFile(file);
			updatePipeLabel();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	protected void readPipeConfigFromFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Pipe-Configuration", "pca"));
		fc.setSelectedFile(MainFrame.the().getPipe().getLastSaveFile());
		new PipePreviewAccessory(fc);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			readPipeConfigFromFile(fc.getSelectedFile());
	}

	protected void readPipeConfigFromFile(final File file) {
		try {
			Configuration.getMain().readFromFile(file,
					MainFrame.the().getPipe());
			Configuration.getMain().writeToDefaultFile();
			MainFrame.the().getPipe().setLastSaveFile(file);
			updateState();
			updatePipeLabel();
			if (cfgDialog != null) cfgDialog.getBoard().assignFromPipe();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	protected void updateState() {
		btnModify.setEnabled(true);
		btnSave.setEnabled(MainFrame.the().getPipe().getInputList().isEmpty()
				|| !MainFrame.the().getPipe().getConverterList().isEmpty()
				|| !MainFrame.the().getPipe().getOutputList().isEmpty());
		btnLoad.setEnabled(!MainFrame.the().isPipeRunning());
		if (cfgDialog != null) cfgDialog.updateState();
	}

	void configDialogDisposed() {
		Log.detail("Config Dialog disposed, cleaning up resources");
		cfgDialog = null;
		pipeFlowVisualization.cancel();
		pipeFlowVisualization = null;
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
		Log.detail("Scheduling a thumbnail update in 1000 ms");
		UPDATE_TIMER.schedule(updateTimerTask, 1000);
	}

	public PipeFlowVisualization getPipeFlowVisualization() {
		return pipeFlowVisualization;
	}

}
