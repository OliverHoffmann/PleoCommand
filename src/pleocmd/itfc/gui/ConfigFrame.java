package pleocmd.itfc.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import pleocmd.Log;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.cvt.EmotionConverter;
import pleocmd.pipe.cvt.PleoMonitorCMDConverter;
import pleocmd.pipe.cvt.SimpleConverter;
import pleocmd.pipe.in.ConsoleInput;
import pleocmd.pipe.in.FileInput;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.in.TcpIpInput;
import pleocmd.pipe.out.ConsoleOutput;
import pleocmd.pipe.out.FileOutput;
import pleocmd.pipe.out.Output;
import pleocmd.pipe.out.PleoRXTXOutput;

public final class ConfigFrame extends JDialog {

	private static final long serialVersionUID = -1967218353263515865L;

	private boolean okPressed;

	@SuppressWarnings("unchecked")
	public ConfigFrame(final Pipe pipe) {
		Log.detail("Creating Config-Frame");
		setTitle("Configure Pipe");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final PipePartPanel<Input> pppInput = new PipePartPanel<Input>(
				(Class<Input>[]) new Class<?>[] { FileInput.class,
						ConsoleInput.class, TcpIpInput.class });
		final PipePartPanel<Converter> pppConverter = new PipePartPanel<Converter>(
				(Class<Converter>[]) new Class<?>[] { SimpleConverter.class,
						EmotionConverter.class, PleoMonitorCMDConverter.class });
		final PipePartPanel<Output> pppOutput = new PipePartPanel<Output>(
				(Class<Output>[]) new Class<?>[] { FileOutput.class,
						ConsoleOutput.class, PleoRXTXOutput.class });

		pppInput.addPipeParts(pipe.getInputList());
		pppConverter.addPipeParts(pipe.getConverterList());
		pppOutput.addPipeParts(pipe.getOutputList());

		// Add components
		final JTabbedPane tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		tabs.addTab("Input", pppInput);
		tabs.addTab("Converter", pppConverter);
		tabs.addTab("Output", pppOutput);

		final JPanel bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		add(bottom, BorderLayout.SOUTH);
		final GridBagConstraints gbc = initGBC();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		bottom.add(new JLabel(), gbc);
		gbc.weightx = 0.0;

		final JButton btnOK = new JButton("OK", IconLoader
				.getIcon("dialog-ok.png"));
		btnOK.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				pipe.reset();
				for (int i = 0; i < pppInput.getPipePartCount(); ++i)
					pipe.addInput(pppInput.getPipePart(i));
				for (int i = 0; i < pppConverter.getPipePartCount(); ++i)
					pipe.addConverter(pppConverter.getPipePart(i));
				for (int i = 0; i < pppOutput.getPipePartCount(); ++i)
					pipe.addOutput(pppOutput.getPipePart(i));
				okPressed = true;
				dispose();
			}
		});
		gbc.gridx = 1;
		bottom.add(btnOK, gbc);
		getRootPane().setDefaultButton(btnOK);

		final JButton btnCancel = new JButton("Cancel", IconLoader
				.getIcon("dialog-cancel.png"));
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		gbc.gridx = 2;
		bottom.add(btnCancel, gbc);

		// Center window on screen
		setSize(700, 400);
		setLocationRelativeTo(null);

		Log.detail("Config-Frame created");
		okPressed = false;
		setModal(true);
		setVisible(true);
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	public static GridBagConstraints initGBC() {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		return gbc;
	}

}
