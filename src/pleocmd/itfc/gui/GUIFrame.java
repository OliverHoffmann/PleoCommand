package pleocmd.itfc.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.icons.IconLoader;
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

	private final JButton btnStart;

	private Thread pipeThread;

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
				exit();
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
		final JButton btnCfgChange = new JButton("Change", IconLoader
				.getIcon("document-edit.png"));
		btnCfgChange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				changeConfig();
			}
		});
		add(btnCfgChange, gbc);
		++gbc.gridx;
		final JButton btnCfgSave = new JButton("Save To ...", IconLoader
				.getIcon("document-save-as.png"));
		btnCfgSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				writeConfigToFile();
			}
		});
		add(btnCfgSave, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnCfgLoad = new JButton("Load From ...", IconLoader
				.getIcon("document-open.png"));
		btnCfgLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				readConfigFromFile();
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
		logTable.setEnabled(false);
		add(new JScrollPane(logTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 0.0;

		++gbc.gridy;
		gbc.gridx = 0;
		btnStart = new JButton("Start", IconLoader.getIcon("arrow-right.png"));
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startPipeThread();
			}
		});
		add(btnStart, gbc);
		++gbc.gridx;
		final JButton btnLogSave = new JButton("Save To ...", IconLoader
				.getIcon("document-save-as.png"));
		btnLogSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				writeLogToFile();
			}
		});
		add(btnLogSave, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnLogClear = new JButton("Clear", IconLoader
				.getIcon("archive-remove.png"));
		btnLogClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				clearLog();
			}
		});
		add(btnLogClear, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		consoleInput = new JTextField();
		consoleInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) putConsoleInput();
			}
		});
		add(consoleInput, gbc);
		gbc.gridwidth = 1;

		++gbc.gridy;
		gbc.gridx = 0;
		final JButton btnSendEOS = new JButton("Send EOS", IconLoader
				.getIcon("media-playback-stop.png"));
		btnSendEOS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				closeConsoleInput();
			}
		});
		add(btnSendEOS, gbc);
		++gbc.gridx;
		final JButton btnConsoleRead = new JButton("Read From ...", IconLoader
				.getIcon("document-import.png"));
		btnConsoleRead.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				readConsoleInputFromFile();
			}
		});
		add(btnConsoleRead, gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;
		++gbc.gridx;
		final JButton btnExit = new JButton("Exit", IconLoader
				.getIcon("application-exit.png"));
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				exit();
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
		updatePipeLabel();

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

	private void updatePipeLabel() {
		pipeLabel.setText(String.format(
				"Pipe has %d input%s, %d converter and %d output%s", pipe
						.getInputList().size(),
				pipe.getInputList().size() == 1 ? "" : "s", pipe
						.getConverterList().size(),
				pipe.getOutputList().size(),
				pipe.getOutputList().size() == 1 ? "" : "s"));
	}

	public boolean changeConfig() {
		Log.detail("GUI-Frame starts configuration");
		final boolean ok = new ConfigFrame(pipe).isOkPressed();
		Log.detail("GUI-Frame is done with configuration: " + ok);
		updatePipeLabel();
		return ok;
	}

	public void writeConfigToFile() {
		final JFileChooser fc = new JFileChooser();
		if (fc.showSaveDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
			try {
				pipe.writeToFile(fc.getSelectedFile());
			} catch (final IOException exc) {
				Log.error(exc);
			}
	}

	public void readConfigFromFile() {
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

	public synchronized void startPipeThread() {
		if (pipeThread != null)
			throw new IllegalStateException("Pipe-Thread already running");
		btnStart.setEnabled(false);
		pipeThread = new Thread("Pipe-Thread") {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				try {
					StandardInput.the().close();
					StandardInput.the().resetCache();
					pipe.configuredAll();
					pipe.initializeAll();
					pipe.pipeAllData();
					pipe.closeAll();
				} catch (final Throwable t) {
					Log.error(t);
				}
				synchronized (GUIFrame.this) {
					pipeThread = null;
				}
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						btnStart.setEnabled(true);
					}
				});
			}
		};
		pipeThread.start();
	}

	public void writeLogToFile() {
		final JFileChooser fc = new JFileChooser();
		if (fc.showSaveDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
			try {
				logModel.writeToFile(fc.getSelectedFile());
			} catch (final IOException exc) {
				Log.error(exc);
			}
	}

	public void clearLog() {
		logModel.clear();
	}

	public void addLog(final Log log) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				logModel.addLog(log);
				logTable.scrollRectToVisible(logTable.getCellRect(logModel
						.getRowCount() - 1, 0, true));
			}
		});
	}

	public void putConsoleInput() {
		try {
			StandardInput.the().put(
					(consoleInput.getText() + "\n").getBytes("ISO-8859-1"));
		} catch (final IOException exc) {
			Log.error(exc);
		}
	}

	public void closeConsoleInput() {
		try {
			StandardInput.the().close();
		} catch (final IOException exc) {
			Log.error(exc);
		}
	}

	public void readConsoleInputFromFile() {
		final JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
			readConsoleInputFromFile(fc.getSelectedFile());
	}

	public void readConsoleInputFromFile(final File file) {
		try {
			final BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null)
				StandardInput.the().put((line + '\n').getBytes("ISO-8859-1"));
			in.close();
		} catch (final IOException exc) {
			Log.error(exc);
		}
	}

	public void exit() {
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

}
