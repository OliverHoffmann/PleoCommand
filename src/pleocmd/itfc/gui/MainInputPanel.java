package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.itfc.gui.icons.IconLoader;

public class MainInputPanel extends JPanel {

	private static final long serialVersionUID = 8130292678723649962L;

	private final JTextField consoleInput;

	public MainInputPanel() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.weightx = 0.0;
		gbc.gridy = 0;
		gbc.gridx = 0;
		consoleInput = new JTextField();
		consoleInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) putConsoleInput();
			}
		});
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weighty = 1.0;
		add(consoleInput, gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 0.0;

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

}
