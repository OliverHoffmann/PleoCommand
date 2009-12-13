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

import pleocmd.itfc.gui.CommandSequenceEditorFrame;

public final class ConfigCmdSeq extends ConfigValue {

	private String content;

	private JTextField tf;

	public ConfigCmdSeq(final String label) {
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
				new CommandSequenceEditorFrame(new File(content));
			}
		});
		cntr.add(btnEdit, gbc);
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
