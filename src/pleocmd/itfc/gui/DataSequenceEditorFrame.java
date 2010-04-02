package pleocmd.itfc.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.ConfigDataMap;
import pleocmd.cfg.ConfigInt;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.pipe.data.Data;

// CS_IGNORE_NEXT The classes this one relies on are mainly GUI components
public final class DataSequenceEditorFrame extends JDialog implements
		ConfigurationInterface {

	private static final long serialVersionUID = -5729115559356740425L;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final ConfigInt cfgSplitterPos = new ConfigInt("Splitter Position",
			-1);

	private final ConfigDataMap map;

	private final ConfigDataMap mapOrg;

	private final JSplitPane splitPane;

	private final JList triggerList;

	private final DataSequenceEditorListModel triggerModel;

	private final JButton btnAddTrigger;

	private final JButton btnRenameTrigger;

	private final JButton btnRemoveTrigger;

	private final DataSequenceEditorPanel dsePanel;

	private String trigger;

	// CS_IGNORE_NEXT Contains only GUI component creation
	public DataSequenceEditorFrame(final ConfigDataMap cfgMap) {
		mapOrg = cfgMap;
		map = new ConfigDataMap(mapOrg.getLabel());
		map.assignFrom(mapOrg);

		Log.detail("Creating DataSequenceEditorFrame");
		setTitle("Edit Data Sequence");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);
		final JPanel panel = new JPanel();
		final Layouter layInner = new Layouter(panel);
		triggerModel = new DataSequenceEditorListModel();
		triggerList = new JList(triggerModel);
		triggerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		layInner.addWholeLine(new JScrollPane(triggerList), true);
		triggerList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				triggerIndexChanged();
			}

		});

		layInner.add(new JLabel("Trigger:"), false);
		btnAddTrigger = layInner.addButton("Add", "list-add",
				"Add a new trigger to the list", new Runnable() {
					@Override
					public void run() {
						addNewTrigger();
					}
				});
		btnRenameTrigger = layInner.addButton("Rename", "edit-rename",
				"Change the name of the select trigger", new Runnable() {
					@Override
					public void run() {
						renameSelectedTrigger();
					}
				});
		layInner.addSpacer();
		btnRemoveTrigger = layInner.addButton("Remove", "list-remove",
				"Remove the selected trigger from the list", new Runnable() {
					@Override
					public void run() {
						removeSelectedTrigger();
					}

				});

		dsePanel = new DataSequenceEditorPanel(this, new Runnable() {
			@Override
			public void run() {
				saveChanges();
			}
		}, new Runnable() {
			@Override
			public void run() {
				close();
			}
		});
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panel,
				dsePanel);
		splitPane.setResizeWeight(0.25);
		lay.addWholeLine(splitPane, true);

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.the().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		updateTriggerModel();
		updateState();

		Log.detail("DataSequenceEditorFrame created");
		// setModal(true);
		HelpDialog.closeHelpIfOpen();
		setVisible(true);
	}

	protected void saveChanges() {
		writeTextPaneToMap();
		mapOrg.assignFrom(map);
	}

	protected void close() {
		try {
			Configuration.the().unregisterConfigurableObject(this);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
		dispose();
		HelpDialog.closeHelpIfOpen();
	}

	protected void triggerIndexChanged() {
		final Object newTrigger = triggerList.getSelectedValue();
		if (trigger == newTrigger) return;
		writeTextPaneToMap();
		trigger = (String) newTrigger;
		updateTextPaneFromMap();
		updateState();
	}

	private void writeTextPaneToMap() {
		Log.detail("Writing TextPane to map with '%s'", trigger);
		try {
			final List<Data> list = dsePanel.writeTextPaneToList();
			if (trigger == null) {
				if (!list.isEmpty())
					throw new IOException("No name selected in JList");
			} else
				map.setContent(trigger, list);
		} catch (final ConfigurationException e) {
			Log.error(e);
		} catch (final IOException e) {
			Log.error(e);
		} catch (final FormatException e) {
			Log.error(e);
		}
	}

	private void updateTextPaneFromMap() {
		Log.detail("Updating TextPane from map with '%s'", trigger);
		dsePanel.updateTextPaneFromList(trigger == null ? null : map
				.getContent(trigger));
	}

	protected void updateTriggerModel() {
		final Object lastSelected = trigger;
		triggerModel.set(map.getAllKeysSorted(new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return o1.compareTo(o2);
			}
		}));
		triggerList.setSelectedValue(lastSelected, true);
		updateState(); // TODO needed?
	}

	protected void addNewTrigger() {
		final String name = JOptionPane.showInputDialog(this,
				"Name of the new trigger", "Add new trigger",
				JOptionPane.PLAIN_MESSAGE);
		if (name != null) {
			try {
				map.createContent(name);
			} catch (final ConfigurationException e) {
				Log.error(e);
			}
			updateTriggerModel();
			triggerList.setSelectedValue(name, true);
		}
	}

	protected void renameSelectedTrigger() {
		if (trigger != null) {
			final String name = (String) JOptionPane.showInputDialog(this,
					"New Name of the trigger", "Rename trigger",
					JOptionPane.PLAIN_MESSAGE, null, null, trigger);
			if (name != null) {
				try {
					map.renameContent(trigger, name);
				} catch (final ConfigurationException e) {
					Log.error(e);
				}
				trigger = name;
				updateTriggerModel();
				triggerList.setSelectedValue(name, true);
			}
		}
	}

	protected void removeSelectedTrigger() {
		if (trigger != null) {
			map.removeContent(trigger);
			dsePanel.clear();
			trigger = null;
			updateTriggerModel();
		}
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(cfgBounds).add(cfgSplitterPos);
	}

	@Override
	public void configurationAboutToBeChanged() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		cfgBounds.assignContent(this);
		splitPane.setDividerLocation(cfgSplitterPos.getContent());
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		cfgBounds.setContent(getBounds());
		cfgSplitterPos.setContent(splitPane.getDividerLocation());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

	public void updateState() {
		btnAddTrigger.setEnabled(true);
		btnRenameTrigger.setEnabled(trigger != null);
		btnRemoveTrigger.setEnabled(trigger != null);
		dsePanel.setEnabled(trigger != null);
		dsePanel.updateState();
	}

	public void freeResources() {
		dsePanel.freeResources();
	}

}
