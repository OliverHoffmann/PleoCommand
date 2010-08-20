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

package pleocmd.cfg;

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.RunnableWithArgument;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.Layouter.Button;

public final class ConfigPath extends ConfigValue {

	public enum PathType {
		FileForReading, FileForWriting, Directory
	}

	private File content;

	private final PathType type;

	private RunnableWithArgument modifyFile;

	private JTextField tf;

	private boolean acceptAllFileFilter = true;

	private final List<FileFilter> filters = new ArrayList<FileFilter>();

	private boolean internalMod;

	public ConfigPath(final String label, final PathType type) {
		super(label);
		this.type = type;
		clearContent();
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

	@Override
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
			if (content.exists() ? !content.canWrite() : content
					.getParentFile() != null
					&& !content.getParentFile().canWrite())
				throw new ConfigurationException("Cannot write to file '%s'",
						content);
			break;
		case Directory:
			if (!content.isDirectory())
				throw new ConfigurationException("'%s' is not a directory",
						content);
			break;
		default:
			throw new InternalException(type);
		}
		checkValidString(content.getPath(), false);
		this.content = content;
		if (tf != null) tf.setText(content.getPath());
	}

	public void clearContent() {
		content = new File("");
		if (tf != null) tf.setText("");
	}

	public File getContentGUI() {
		return tf == null ? content : new File(tf.getText());
	}

	public void setContentGUI(final File file) {
		internalMod = true;
		try {
			if (tf != null) tf.setText(file.getPath());
		} finally {
			internalMod = false;
		}
	}

	public void clearContentGUI() {
		internalMod = true;
		try {
			if (tf != null) tf.setText("");
		} finally {
			internalMod = false;
		}
	}

	public PathType getType() {
		return type;
	}

	@Override
	public String asString() {
		return content.getPath();
	}

	@Override
	public void setFromString(final String string)
			throws ConfigurationException {
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
	public String getIdentifier() {
		switch (type) {
		case FileForReading:
			return "read";
		case FileForWriting:
			return "write";
		case Directory:
			return "dir";
		default:
			throw new InternalException(type);
		}
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public boolean insertGUIComponents(final Layouter lay) {
		tf = new JTextField(content.getPath(), 50);
		tf.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(final UndoableEditEvent e) {
				if (!isInternalMod()) invokeChangingContent(getTf().getText());
			}
		});
		lay.add(tf, true);
		lay.addButton(Button.Browse, new Runnable() {
			@Override
			public void run() {
				selectPath(lay.getContainer().getParent());
			}
		});
		if (modifyFile != null) {
			final JButton btnModify = lay.addButton(Button.Modify,
					"Edit the selected file", new Runnable() {
						@Override
						@SuppressWarnings("synthetic-access")
						public void run() {
							if (getModifyFile() != null)
								getModifyFile().run(tf.getText());
						}
					});
			btnModify.setEnabled(!tf.getText().isEmpty());
			tf.addCaretListener(new CaretListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void caretUpdate(final CaretEvent e) {
					btnModify.setEnabled(!tf.getText().isEmpty());
				}
			});
		}
		invokeChangingContent(tf.getText());
		return false;
	}

	protected void selectPath(final Container parent) {
		final JFileChooser fc = new JFileChooser(getContent());
		fc.setSelectedFile(new File(tf.getText()));
		fc.setAcceptAllFileFilterUsed(acceptAllFileFilter);
		for (final FileFilter filter : filters)
			fc.addChoosableFileFilter(filter);

		switch (type) {
		case FileForReading:
			if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
				tf.setText(fc.getSelectedFile().getPath());
			break;
		case FileForWriting:
			if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				final FileFilter ff = fc.getFileFilter();
				String path = fc.getSelectedFile().getPath();
				if (ff instanceof FileNameExtensionFilter
						&& !fc.getSelectedFile().getName().contains("."))
					path = path + "."
							+ ((FileNameExtensionFilter) ff).getExtensions()[0];
				tf.setText(path);
			}
			break;
		case Directory:
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
				tf.setText(fc.getSelectedFile().getPath());
			break;
		default:
			throw new InternalException(type);
		}
	}

	@Override
	public void setFromGUIComponents() {
		try {
			setContent(new File(tf.getText()));
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public void setGUIEnabled(final boolean enabled) {
		if (tf != null) tf.setEnabled(enabled);
	}

	/**
	 * Sets the method which will be invoked if the user clicks on "Modify".<br>
	 * If <b>null</b> the "Modify" button will not be available.
	 * 
	 * @param modifyFile
	 *            {@link Runnable} or <b>null</b>
	 */
	public void setModifyFile(final RunnableWithArgument modifyFile) {
		this.modifyFile = modifyFile;
	}

	/**
	 * @return {@link Runnable} invoked after the user clicked on "Modify" or
	 *         <b>null</b>
	 */
	public RunnableWithArgument getModifyFile() {
		return modifyFile;
	}

	/**
	 * Sets the list of {@link FileFilter}s available in the file selection
	 * dialog via {@link JFileChooser}.
	 * 
	 * @param filters
	 *            {@link List} of {@link FileFilter}
	 */
	public void setFileFilter(final List<FileFilter> filters) {
		this.filters.clear();
		this.filters.addAll(filters);
	}

	/**
	 * Determines whether the "Accept All" {@link FileFilter} is used as an
	 * available choice in the file selection dialog via {@link JFileChooser}.
	 * 
	 * @param acceptAllFileFilter
	 *            whether to accept all files
	 */
	public void setAcceptAllFileFilter(final boolean acceptAllFileFilter) {
		this.acceptAllFileFilter = acceptAllFileFilter;
	}

	/**
	 * @return true if "Accept All" {@link FileFilter} will be available
	 */
	public boolean isAcceptAllFileFilter() {
		return acceptAllFileFilter;
	}

	protected JTextField getTf() {
		return tf;
	}

	protected boolean isInternalMod() {
		return internalMod;
	}

}
