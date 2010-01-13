package pleocmd.itfc.gui;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.PipeException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

public final class PipePartConfigFrame extends JDialog implements
		ConfigurationInterface {

	private static final long serialVersionUID = -1967218353263515865L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final PipePartPanel<Input> pppInput;

	private final PipePartPanel<Converter> pppConverter;

	private final PipePartPanel<Output> pppOutput;

	public PipePartConfigFrame() {
		pppInput = new PipePartPanel<Input>(PipePartDetection.ALL_INPUT);
		pppConverter = new PipePartPanel<Converter>(
				PipePartDetection.ALL_CONVERTER);
		pppOutput = new PipePartPanel<Output>(PipePartDetection.ALL_OUTPUT);

		Log.detail("Creating Config-Frame");
		setTitle("Configure Pipe");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		pppInput.assignPipeParts(Pipe.the().getInputList());
		pppConverter.assignPipeParts(Pipe.the().getConverterList());
		pppOutput.assignPipeParts(Pipe.the().getOutputList());

		// Add components
		final Layouter lay = new Layouter(this);
		final JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Input", pppInput);
		tabs.addTab("Converter", pppConverter);
		tabs.addTab("Output", pppOutput);
		lay.addWholeLine(tabs, true);

		lay.addSpacer();
		getRootPane().setDefaultButton(lay.addButton(Button.Ok, new Runnable() {
			@Override
			public void run() {
				applyChanges();
				dispose();
			}
		}));
		lay.addButton(Button.Apply, new Runnable() {
			@Override
			public void run() {
				applyChanges();
			}
		});
		lay.addButton(Button.Cancel, new Runnable() {
			@Override
			public void run() {
				Log.detail("Canceled Config-Frame");
				dispose();
			}
		});

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.the().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("Config-Frame created");
		setModal(true);
		setVisible(true);
	}

	public void applyChanges() {
		try {
			Pipe.the().reset();
			for (int i = 0; i < pppInput.getTableModel().getRowCount(); ++i)
				Pipe.the().addInput(pppInput.getTableModel().getPipePart(i));
			for (int i = 0; i < pppConverter.getTableModel().getRowCount(); ++i)
				Pipe.the().addConverter(
						pppConverter.getTableModel().getPipePart(i));
			for (int i = 0; i < pppOutput.getTableModel().getRowCount(); ++i)
				Pipe.the().addOutput(pppOutput.getTableModel().getPipePart(i));
			Configuration.the().writeToDefaultFile();
			MainFrame.the().getMainPipePanel().updateState();
			MainFrame.the().getMainPipePanel().updatePipeLabel();
			Log.detail("Applied Config-Frame");
		} catch (final PipeException e) {
			Log.error(e);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(cfgBounds);
	}

	@Override
	public void configurationAboutToBeChanged() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		setBounds(cfgBounds.getContent());
	}

	@Override
	public List<Group> configurationWriteback() {
		cfgBounds.setContent(getBounds());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

}
