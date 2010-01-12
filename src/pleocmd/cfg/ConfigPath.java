package pleocmd.cfg;

import java.awt.Container;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.Layouter.Button;

public final class ConfigPath extends ConfigValue {

	public enum PathType {
		FileForReading, FileForWriting, Directory
	}

	private File content;

	private final PathType type;

	private JTextField tf;

	public ConfigPath(final String label, final PathType type) {
		super(label);
		this.type = type;
		content = new File("");
	}

	public ConfigPath(final String label, final File content,
			final PathType type) {
		this(label, type);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public File getContent() {
		return content;
	}

	public void setContent(final File content) throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (type != PathType.FileForWriting && !content.exists())
			throw new ConfigurationException("'%s' does not exist", content);
		if (type != PathType.Directory && content.isDirectory())
			throw new ConfigurationException("'%s' is a directory", content);

		switch (type) {
		case FileForReading:
			if (!content.canRead())
				throw new ConfigurationException("Cannot read from file '%s'",
						content);
			break;
		case FileForWriting:
			if (content.exists() ? !content.canWrite() : !content
					.getParentFile().canWrite())
				throw new ConfigurationException("Cannot write to file '%s'",
						content);
			break;
		case Directory:
			if (!content.isDirectory())
				throw new ConfigurationException("'%s' is not a directory",
						content);
			break;
		default:
			throw new InternalError("Invalid PathType");
		}
		checkValidString(content.getPath(), false);
		this.content = content;
	}

	public PathType getType() {
		return type;
	}

	@Override
	String asString() {
		return content.getPath();
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		setContent(new File(string));
	}

	@Override
	List<String> asStrings() {
		throw new UnsupportedOperationException();
	}

	@Override
	void setFromStrings(final List<String> strings) {
		throw new UnsupportedOperationException();
	}

	@Override
	String getIdentifier() {
		switch (type) {
		case FileForReading:
			return "read";
		case FileForWriting:
			return "write";
		case Directory:
			return "dir";
		default:
			throw new InternalError("Invalid PathType");
		}
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		tf = new JTextField(content.getPath(), 50);
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
		switch (type) {
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
		try {
			setContent(new File(tf.getText()));
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

}
