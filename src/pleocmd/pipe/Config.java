package pleocmd.pipe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.ConfigFrame;

public final class Config extends AbstractList<ConfigValue> {

	private final List<ConfigValue> list = new ArrayList<ConfigValue>();

	private PipePart owner;

	private boolean okPressed;

	/**
	 * Should only be called in a constructor of a subclass of {@link PipePart}.
	 */
	public Config() {
		assert new Throwable().getStackTrace()[1].getClassName().startsWith(
				getClass().getPackage().getName() + ".");
	}

	@Override
	public ConfigValue get(final int index) {
		return list.get(index);
	}

	public ConfigValue getSafe(final int index) {
		if (index < 0 || index >= list.size()) return new ConfigDummy();
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	protected void setOwner(final PipePart owner) {
		if (this.owner != null)
			throw new IllegalStateException(
					"Config's owner has already been assigned");
		if (owner.getState() != PipePart.State.Constructing)
			throw new IllegalStateException(
					"New Config's owner has already finished constructing");
		this.owner = owner;
	}

	public Config addV(final ConfigValue value) {
		if (owner != null && owner.getState() != PipePart.State.Constructing)
			throw new IllegalStateException(
					"Config's owner has already finished constructing");
		list.add(value);
		return this;
	}

	public boolean readFromGUI(final String prefix) throws PipeException {
		// no need to configure if no values assigned
		if (isEmpty()) return true;

		final JDialog dlg = new JDialog();
		dlg.setLayout(new BorderLayout());
		dlg.setTitle(prefix + " " + owner);
		final JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		dlg.add(main, BorderLayout.CENTER);
		int y = 0;
		for (final ConfigValue v : list) {
			final GridBagConstraints gbc = ConfigFrame.initGBC();
			gbc.weighty = 0.0;
			gbc.gridy = y;
			main.add(new JLabel(v.getLabel() + ":", SwingConstants.RIGHT), gbc);
			gbc.weightx = 0.0;
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			v.insertGUIComponents(main, gbc);
			++y;
		}
		{
			final GridBagConstraints gbc = ConfigFrame.initGBC();
			gbc.gridy = y;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			main.add(new JLabel(), gbc);
		}

		final JPanel bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		dlg.add(bottom, BorderLayout.SOUTH);
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		bottom.add(new JLabel(), gbc);
		gbc.weightx = 0.0;

		final JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				okPressed = true;
				dlg.dispose();
			}
		});
		gbc.gridx = 1;
		bottom.add(btnOK, gbc);
		dlg.getRootPane().setDefaultButton(btnOK);

		final JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dlg.dispose();
			}
		});
		gbc.gridx = 2;
		bottom.add(btnCancel, gbc);
		dlg.getRootPane().setDefaultButton(btnOK);

		dlg.setMinimumSize(new Dimension(200, 100));
		dlg.pack();
		dlg.setLocationRelativeTo(null);
		dlg.setModal(true);
		okPressed = false;
		dlg.setVisible(true);

		if (okPressed) {
			for (final ConfigValue v : list)
				v.setFromGUIComponents(dlg);
			owner.configured();
		}
		return okPressed;
	}

	public void readFromFile(final BufferedReader in) throws IOException,
			PipeException {
		for (final ConfigValue v : list) {
			in.mark(10240);
			final String line = in.readLine().trim();
			final int idx = line.indexOf(':');
			if (idx == -1)
				throw new IOException("Missing ':' delimiter in " + line);
			final String label = line.substring(0, idx).trim();
			if (!label.equals(v.getLabel()))
				throw new IOException("Wrong configuration value " + label
						+ " - excepted " + v.getLabel());
			v.setFromString(line.substring(idx + 1).trim());
		}
		owner.configured();
	}

	public void writeToFile(final Writer out) throws IOException {
		for (final ConfigValue v : list) {
			out.write('\t');
			out.write(v.getLabel());
			out.write(':');
			out.write(' ');
			out.write(v.getContentAsString());
			out.write('\n');
		}
	}

}
