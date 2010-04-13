package pleocmd.itfc.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

public class PipePreviewAccessory extends PipePreviewLabel {

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
