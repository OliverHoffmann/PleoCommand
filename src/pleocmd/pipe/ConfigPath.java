package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public final class ConfigPath extends ConfigValue {

	public enum PathType {
		FileForReading, FileForWriting, Directory
	}

	private String content;

	private final PathType type;

	private JTextField tf;

	public ConfigPath(final String label, final PathType type) {
		super(label);
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public PathType getType() {
		return type;
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		tf = new JTextField(content, 50);
		gbc.gridwidth = 1;
		cntr.add(tf, gbc);
		++gbc.gridx;
		final JButton btn = new JButton("...");
		btn.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser(getContent());
				switch (getType()) {
				case FileForReading:
					if (fc.showOpenDialog(cntr.getParent()) == JFileChooser.APPROVE_OPTION)
						tf.setText(fc.getSelectedFile().getPath());
					break;
				case FileForWriting:
					if (fc.showSaveDialog(cntr.getParent()) == JFileChooser.APPROVE_OPTION)
						tf.setText(fc.getSelectedFile().getPath());
					break;
				case Directory:
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fc.showOpenDialog(cntr.getParent()) == JFileChooser.APPROVE_OPTION)
						tf.setText(fc.getSelectedFile().getPath());
					break;
				default:
					throw new RuntimeException(
							"Internal error: Invalid PathType");
				}
			}
		});
		cntr.add(btn, gbc);
	}

	@Override
	public void setFromGUIComponents(final Container cntr) {
		setContent(tf.getText());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		setContent(content);
	}

}
