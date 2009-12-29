package pleocmd.pipe.cfg;

import java.awt.Container;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.itfc.gui.DataSequenceEditorFrame;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.Layouter.Button;

public final class ConfigDataSeq extends ConfigValue {

	private String content;

	private JTextField tf;

	public ConfigDataSeq(final String label) {
		super(label);
		content = "";
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		if (content == null) throw new NullPointerException("content");
		this.content = content;
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
		final JButton btnEdit = lay.addButton(Button.Modify,
				"Modifies the contents of the selected file", new Runnable() {
					@Override
					public void run() {
						if (!getTf().getText().isEmpty())
							new DataSequenceEditorFrame(new File(getTf()
									.getText()));
					}
				});
		btnEdit.setEnabled(!tf.getText().isEmpty());
		tf.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(final DocumentEvent e) {
				// just ignore this
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				btnEdit.setEnabled(!getTf().getText().isEmpty());
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				btnEdit.setEnabled(!getTf().getText().isEmpty());
			}
		});
	}

	protected void selectPath(final Container parent) {
		final JFileChooser fc = new JFileChooser(getContent());
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"CommandSequenceList", "csl"));
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
			tf.setText(fc.getSelectedFile().getPath());
	}

	protected JTextField getTf() {
		return tf;
	}

	@Override
	public void setFromGUIComponents() {
		setContent(tf.getText());
	}

	@Override
	public void setFromString(final String content) throws IOException {
		setContent(content);
	}

}
