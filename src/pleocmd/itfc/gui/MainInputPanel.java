package pleocmd.itfc.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.itfc.gui.icons.IconLoader;

public final class MainInputPanel extends JPanel {

	private static final long serialVersionUID = 8130292678723649962L;

	private final JList historyList;

	private final JTextField consoleInput;

	private final HistoryListModel historyListModel;

	public MainInputPanel() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.weightx = 0.0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		historyListModel = new HistoryListModel();
		historyList = new JList(historyListModel);
		gbc.weighty = 1.0;
		add(new JScrollPane(historyList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), gbc);
		gbc.weighty = 0.0;
		historyList.addMouseListener(new MouseAdapter() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int idx = historyList.getSelectedIndex();
					if (idx != -1)
						putInput(historyListModel.getElementAt(idx).toString());
				}
			}
		});

		++gbc.gridy;

		consoleInput = new JTextField();
		consoleInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) putConsoleInput();
			}
		});
		add(consoleInput, gbc);

		++gbc.gridy;
		gbc.gridwidth = 1;

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
		final JButton btnConsoleClear = new JButton("Clear History", IconLoader
				.getIcon("archive-remove.png"));
		btnConsoleClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				clearHistory();
			}
		});
		add(btnConsoleClear, gbc);

	}

	public void putConsoleInput() {
		putInput(consoleInput.getText());
		consoleInput.setText("");
	}

	public void putInput(final String input) {
		try {
			StandardInput.the().put((input + "\n").getBytes("ISO-8859-1"));
			historyListModel.add(input);
			EventQueue.invokeLater(new Runnable() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void run() {
					final int size = historyListModel.getSize() - 1;
					historyList.scrollRectToVisible(historyList.getCellBounds(
							size, size));
				}
			});
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
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
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

	public void clearHistory() {
		historyListModel.clear();
	}

	public List<String> getHistory() {
		return historyListModel.getAll();
	}

	class HistoryListModel extends AbstractListModel {

		private static final long serialVersionUID = 4510015901086617192L;

		private final List<String> history = new ArrayList<String>();

		@Override
		public int getSize() {
			return history.size();
		}

		@Override
		public Object getElementAt(final int index) {
			return history.get(index);
		}

		public void add(final String line) {
			history.add(line);
			fireIntervalAdded(this, history.size() - 1, history.size() - 1);
		}

		public void clear() {
			final int size = history.size();
			history.clear();
			fireIntervalRemoved(this, 0, size - 1);
		}

		public List<String> getAll() {
			return Collections.unmodifiableList(history);
		}

	}

}
