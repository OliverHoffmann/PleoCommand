package pleocmd.pipe.cfg;

import java.awt.Container;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.Layouter.Button;

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
	public String getContentAsString() {
		return content;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		tf = new JTextField(content, 50);
		lay.add(tf, true);
		lay.addButton(Button.Browse, new Runnable() {
			@Override
			public void run() {
				selectPath(lay.getContainer().getParent());
			}
		});
	}

	protected void selectPath(final Container parent) {
		final JFileChooser fc = new JFileChooser(getContent());
		// TODO file extensioon?
		switch (getType()) {
		case FileForReading:
			if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
				tf.setText(fc.getSelectedFile().getPath());
			break;
		case FileForWriting:
			if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
				tf.setText(fc.getSelectedFile().getPath());
			break;
		case Directory:
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
				tf.setText(fc.getSelectedFile().getPath());
			break;
		default:
			throw new InternalError("Invalid PathType");
		}
	}

	@Override
	public void setFromGUIComponents() {
		setContent(tf.getText());
	}

	@Override
	protected void setFromString(final String content) throws IOException {
		setContent(content);
	}

}
