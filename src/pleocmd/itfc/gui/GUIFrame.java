package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.pipe.Pipe;

/**
 * @author oliver
 */
public final class GUIFrame extends JFrame {

	private static final long serialVersionUID = 7174844214646208915L;

	private static GUIFrame guiFrame;

	private final Pipe pipe = new Pipe();

	private final JLabel pipeLabel;

	private final JTable logTable;

	private final LogTableModel logModel;

	private final JTextField consoleInput;

	private GUIFrame() {
		// this two lines should be the first to avoid missing some log entries
		logModel = new LogTableModel();
		logTable = new JTable(logModel);
		guiFrame = this;

		Log.detail("Creating GUI-Frame");
		setTitle("PleoCommand");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		// Add components
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		pipeLabel = new JLabel();
		updatePipeLabel();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(pipeLabel, gbc);
		gbc.gridwidth = 1;

		++gbc.gridy;
		gbc.gridx = 0;
		final JButton btnCfgChange = new JButton("Change");
		btnCfgChange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				config();
			}
		});
		add(btnCfgChange, gbc);
		++gbc.gridx;
		final JButton btnCfgSave = new JButton("Save To ...");
		btnCfgSave.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showSaveDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
					try {
						pipe.writeToFile(fc.getSelectedFile());
					} catch (final IOException exc) {
						Log.error(exc);
					}
			}
		});
		add(btnCfgSave, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnCfgLoad = new JButton("Load From ...");
		btnCfgLoad.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
					try {
						pipe.readFromFile(fc.getSelectedFile());
						updatePipeLabel();
					} catch (final IOException exc) {
						Log.error(exc);
					} catch (final PipeException exc) {
						Log.error(exc);
					}
			}
		});
		add(btnCfgLoad, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weighty = 1.0;
		logTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(50);
		logTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(50);
		logTable.getTableHeader().getColumnModel().getColumn(1)
				.setMinWidth(200);
		logTable.getTableHeader().getColumnModel().getColumn(1)
				.setMaxWidth(200);
		logTable.getTableHeader().setVisible(false);
		logTable.setShowGrid(false);
		add(new JScrollPane(logTable), gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 0.0;

		++gbc.gridy;
		gbc.gridx = 0;
		final JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				// TODO put in extra thread
				try {
					pipe.configuredAll();
					pipe.initializeAll();
				} catch (final PipeException exc) {
					Log.error(exc);
					return;
				}
				pipe.pipeAllData();
				pipe.closeAll();
			}
		});
		add(btnStart, gbc);
		++gbc.gridx;
		final JButton btnLogSave = new JButton("Save To ...");
		btnLogSave.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showSaveDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
					try {
						logModel.writeToFile(fc.getSelectedFile());
					} catch (final IOException exc) {
						Log.error(exc);
					}
			}
		});
		add(btnLogSave, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnLogClear = new JButton("Clear");
		btnLogClear.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				logModel.clear();
			}
		});
		add(btnLogClear, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		consoleInput = new JTextField();
		add(consoleInput, gbc);
		gbc.gridwidth = 1;

		++gbc.gridy;
		gbc.gridx = 0;
		final JButton btnSendEOS = new JButton("Send EOS");
		btnSendEOS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO
			}
		});
		add(btnSendEOS, gbc);
		++gbc.gridx;
		final JButton btnConsoleRead = new JButton("Read From ...");
		btnConsoleRead.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO
			}
		});
		add(btnConsoleRead, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				close();
			}
		});
		add(btnExit, gbc);

		// Center window on screen
		setSize(800, 500);
		setLocationRelativeTo(null);

		// Load default configuration
		try {
			pipe.readFromFile(new File(System.getProperty("user.home")
					+ "/.pleocommand.pipe"));
		} catch (final IOException e) {
			Log.error(e);
		} catch (final PipeException e) {
			Log.error(e);
		}

		Log.detail("GUI-Frame created");
		setVisible(true);
	}

	public static GUIFrame the() {
		if (guiFrame == null) new GUIFrame();
		return guiFrame;
	}

	public static boolean hasGUI() {
		return guiFrame != null;
	}

	public boolean config() {
		Log.detail("GUI-Frame starts configuration");
		final boolean ok = new ConfigFrame(pipe).isOkPressed();
		Log.detail("GUI-Frame is done with configuration: " + ok);
		updatePipeLabel();
		return ok;
	}

	private void updatePipeLabel() {
		pipeLabel.setText(String.format(
				"Pipe has %d input%s, %d converter and %d output%s", pipe
						.getInputList().size(),
				pipe.getInputList().size() == 1 ? "" : "s", pipe
						.getConverterList().size(),
				pipe.getOutputList().size(),
				pipe.getOutputList().size() == 1 ? "" : "s"));
	}

	public void close() {
		Log.detail("GUI-Frame has been closed");
		try {
			pipe.writeToFile(new File(System.getProperty("user.home")
					+ "/.pleocommand.pipe"));
		} catch (final IOException e) {
			e.printStackTrace(); // nothing better to do here
		}
		guiFrame = null;
		dispose();
	}

	public LogTableModel getLogTableModel() {
		return logModel;
	}

}
