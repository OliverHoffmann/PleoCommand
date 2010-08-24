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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;

final class PipeConfigDialog extends JDialog implements ConfigurationInterface,
		AutoDisposableWindow {

	private static final long serialVersionUID = 145574241927303337L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final JSlider sldZoom;

	private final JButton btnSave;

	private final JButton btnLoad;

	private final JButton btnClose;

	private final JButton btnRevert;

	private final PipeConfigBoard board;

	private char[] originalPipe;

	public PipeConfigDialog(final Pipe pipe) {
		Log.detail("Creating Config-Frame");
		setTitle("Pending ....");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close(false);
			}
		});
		board = new PipeConfigBoard(pipe);
		updatePipeLabel();

		// Save current pipe's configuration
		saveCurrentPipe();

		sldZoom = new JSlider(-100, 100, 0);
		sldZoom.setMinimumSize(new Dimension(150, sldZoom.getHeight()));
		sldZoom.setToolTipText("Zoom the Pipe Configuration Board");
		sldZoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				setCurrentZoom();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);
		lay.addWholeLine(new JScrollPane(board,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS), true);

		lay.addButton(Button.Help,
				Layouter.help(this, getClass().getSimpleName()));
		lay.add(sldZoom, false);
		lay.addSpacer();
		btnSave = lay.addButton(Button.SaveTo,
				"Save the current pipe to a file", new Runnable() {
					@Override
					public void run() {
						MainFrame.the().getMainPipePanel()
								.writePipeConfigToFile();
					}
				});
		btnLoad = lay.addButton(Button.LoadFrom,
				"Load a previously saved pipe from a file", new Runnable() {
					@Override
					public void run() {
						MainFrame.the().getMainPipePanel()
								.readPipeConfigFromFile();
					}
				});
		btnClose = lay.addButton(Button.Close, new Runnable() {
			@Override
			public void run() {
				applyChanges();
				close(false);
			}
		});
		getRootPane().setDefaultButton(btnClose);
		btnRevert = lay.addButton(Button.Revert, new Runnable() {
			@Override
			public void run() {
				Log.detail("Reverted Config-Frame");
				close(true);
			}
		});

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("Config-Frame created");
		MainFrame.the().addKnownWindow(this);
		// setModal(true);
		HelpDialog.closeHelpIfOpen();
		updateState();
		setVisible(true);
	}

	public PipeConfigBoard getBoard() {
		return board;
	}

	protected void setCurrentZoom() {
		board.setZoom(sldZoom.getValue() / 100.0);
	}

	private void saveCurrentPipe() {
		final CharArrayWriter out = new CharArrayWriter();
		try {
			Configuration.getMain().writeToWriter(out, board.getPipe());
		} catch (final IOException e) {
			throw new InternalException(e);
		}
		originalPipe = out.toCharArray();
	}

	protected void close(final boolean resetChanges) {
		board.closed();
		if (resetChanges) {
			final CharArrayReader in = new CharArrayReader(originalPipe);
			try {
				Configuration.getMain().readFromReader(new BufferedReader(in),
						board.getPipe());
			} catch (final ConfigurationException e) {
				Log.error(e, "Cannot restore previous pipe");
			} catch (final IOException e) {
				Log.error(e, "Cannot restore previous pipe");
			}
			in.close();
		}
		try {
			Configuration.getMain().unregisterConfigurableObject(this);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
		MainFrame.the().removeKnownWindow(this);
		dispose();
		MainFrame.the().getMainPipePanel().configDialogDisposed();
		HelpDialog.closeHelpIfOpen();
	}

	@Override
	public void autoDispose() {
		close(true);
	}

	public void applyChanges() {
		try {
			saveCurrentPipe();
			Configuration.getMain().writeToDefaultFile();
			MainFrame.the().getMainPipePanel().updateState();
			MainFrame.the().getMainPipePanel().updatePipeLabel();
			Log.detail("Applied Config-Frame");
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
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
	public List<Group> configurationWriteback() {
		cfgBounds.setContent(getBounds());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

	public void updateState() {
		sldZoom.setEnabled(true);
		btnSave.setEnabled(board.getPipe().getInputList().isEmpty()
				|| !board.getPipe().getConverterList().isEmpty()
				|| !board.getPipe().getOutputList().isEmpty());
		btnLoad.setEnabled(!MainFrame.the().isPipeRunning());
		btnClose.setEnabled(true);
		btnRevert.setEnabled(!MainFrame.the().isPipeRunning());
		board.updateState();
	}

	public void updatePipeLabel() {
		String fn = board.getPipe().getLastSaveFile().getName();
		if (fn.contains("."))
			fn = " \"" + fn.substring(0, fn.lastIndexOf('.')) + "\"";
		setTitle("Configure Pipe" + fn);
	}
}
