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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

final class PipePreviewAccessory extends PipePreviewLabel {

	private static final long serialVersionUID = -7636683927913445711L;

	public PipePreviewAccessory(final JFileChooser fc) {
		fc.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent e) {
						update((File) null);
					}
				});
		fc.addPropertyChangeListener(
				JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent e) {
						final File file = (File) e.getNewValue();
						update(file != null && file.getName().endsWith(".pca") ? file
								: null);
					}
				});
		final File file = fc.getSelectedFile();
		if (file != null && file.getName().endsWith(".pca")) update(file);
		fc.setAccessory(this);
	}

}
