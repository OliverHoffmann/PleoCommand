package pleocmd.itfc.gui;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;

public final class DataFileEditFrame extends JDialog implements
		ConfigurationInterface {

	private static final long serialVersionUID = 7860184232803195768L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final DataSequenceEditorPanel dsePanel;

	private final File file;

	public DataFileEditFrame(final File file) {
		this.file = file;

		dsePanel = new DataSequenceEditorPanel();
		add(dsePanel, BorderLayout.CENTER);

		updateTextPaneFromFile();

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("DataFileEditFrame created");
		// setModal(true);
		HelpDialog.closeHelpIfOpen();
		setVisible(true);
	}

	protected void close() {
		try {
			Configuration.getMain().unregisterConfigurableObject(this);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
		dispose();
		HelpDialog.closeHelpIfOpen();
	}

	private void writeTextPaneToFile() {
		Log.detail("Writing TextPane to file '%s'", file);
		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(file));
			try {
				dsePanel.writeTextPaneToWriter(out);
			} finally {
				out.close();
			}
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void updateTextPaneFromFile() {
		Log.detail("Updating TextPane from file '%s'", file);
		try {
			if (file.exists()) {
				final BufferedReader in = new BufferedReader(new FileReader(
						file));
				try {
					dsePanel.updateTextPaneFromReader(in);
				} finally {
					in.close();
				}
			} else
				dsePanel.updateTextPaneFromReader(null);
		} catch (final IOException e) {
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
	public void configurationRead() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		cfgBounds.assignContent(this);
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		cfgBounds.setContent(getBounds());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

	public void freeResources() {
		dsePanel.freeResources();
	}

}
