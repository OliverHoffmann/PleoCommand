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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.Log.Type;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.itfc.gui.log.LogTable;
import pleocmd.itfc.gui.log.LogTableModel;

final class MainLogPanel extends JPanel {

	private static final long serialVersionUID = -6921879308383765734L;

	private final LogTableModel logModel;

	private final LogTable logTable;

	private final JButton btnStart;

	private final JButton btnAbort;

	private final JButton btnSave;

	private final JCheckBox cbShowDetail;

	private final JButton btnClear;

	public MainLogPanel() {
		// avoid to access Log class here !!!
		// Log will be written to stderr otherwise and be lost for the GUI

		final Layouter lay = new Layouter(this);

		logTable = new LogTable();
		logModel = new LogTableModel(logTable);
		logTable.setLogModel(logModel);

		lay.addWholeLine(new JScrollPane(logTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), true);

		btnStart = lay.addButton("Start", "arrow-right",
				"Start the currently configured pipe", new Runnable() {
					@Override
					public void run() {
						MainFrame.the().startPipeThread();
					}
				});
		btnAbort = lay.addButton("Abort", "dialog-close",
				"Forcefully abort the currently running pipe", new Runnable() {
					@Override
					public void run() {
						MainFrame.the().abortPipeThread();
					}
				});
		lay.addSpacer();
		// checkbox state will later be set to the correct value
		cbShowDetail = new JCheckBox("Show detailed log", false);
		cbShowDetail.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				refreshLogDetailState();
			}
		});
		lay.add(cbShowDetail, false);
		btnSave = lay.addButton(Button.SaveTo,
				"Save the whole log to a text file", new Runnable() {
					@Override
					public void run() {
						writeLogToFile();
					}
				});
		btnClear = lay.addButton(Button.Clear, "Empty the whole log list",
				new Runnable() {
					@Override
					public void run() {
						clearLog();
					}
				});
	}

	public LogTable getLogTable() {
		return logTable;
	}

	public void writeLogToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("ASCII Logfile",
				"log"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("HTML Logfile",
				"html"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Latex Logfile",
				"tex"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			try {
				File file = fc.getSelectedFile();
				if (!file.getName().contains(".")
						&& fc.getFileFilter() instanceof FileNameExtensionFilter)
					file = new File(
							file.getPath()
									+ "."
									+ ((FileNameExtensionFilter) fc
											.getFileFilter()).getExtensions()[0]);
				logModel.writeToFile(file);
			} catch (final IOException exc) {
				Log.error(exc);
			}
	}

	public void clearLog() {
		logModel.clear();
		updateState();
	}

	public void refreshLogDetailState() {
		Log.setMinLogType(cbShowDetail.isSelected() ? Type.Detail : Type.Info);
		logModel.refresh();
		updateState();
	}

	public void addLog(final Log log) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				getLogTable().addLog(log);
				if (getLogTable().getRowCount() == 1) updateState();
			}
		});
	}

	public void updateState() {
		if (cbShowDetail.isSelected() ^ Log.canLogDetail()) {
			cbShowDetail.setSelected(Log.canLogDetail());
			logModel.refresh();
		}
		btnStart.setEnabled(!MainFrame.the().isPipeRunning());
		btnAbort.setEnabled(MainFrame.the().isPipeRunning());
		btnSave.setEnabled(logModel.getRowCount() > 0);
		btnClear.setEnabled(logModel.getRowCount() > 0);
	}

}
