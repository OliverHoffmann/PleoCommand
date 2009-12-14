package pleocmd.pipe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import pleocmd.itfc.gui.DataSequenceEditorFrame;

public final class ConfigDataSeq extends ConfigValue {

	private String content;

	private JTextField tf;

	public ConfigDataSeq(final String label) {
		super(label);
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	@Override
	public String getContentAsString() {
		return content;
	}

	@Override
	public void insertGUIComponents(final Container cntr,
			final GridBagConstraints gbc) {
		tf = new JTextField(content, 50);
		gbc.gridwidth = 1;
		cntr.add(tf, gbc);

		++gbc.gridx;
		final JButton btnBrowse = new JButton("...");
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser(getContent());
				if (fc.showSaveDialog(cntr.getParent()) == JFileChooser.APPROVE_OPTION)
					tf.setText(fc.getSelectedFile().getPath());
			}
		});
		cntr.add(btnBrowse, gbc);

		++gbc.gridx;
		final JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				if (!tf.getText().isEmpty())
					new DataSequenceEditorFrame(new File(tf.getText()));
			}
		});
		cntr.add(btnEdit, gbc);

		btnEdit.setEnabled(!tf.getText().isEmpty());
		tf.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(final DocumentEvent e) {
				// just ignore this
			}

			@Override
			@SuppressWarnings("synthetic-access")
			public void insertUpdate(final DocumentEvent e) {
				btnEdit.setEnabled(!tf.getText().isEmpty());
			}

			@Override
			@SuppressWarnings("synthetic-access")
			public void removeUpdate(final DocumentEvent e) {
				btnEdit.setEnabled(!tf.getText().isEmpty());
			}
		});
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
