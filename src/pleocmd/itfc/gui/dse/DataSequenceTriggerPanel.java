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

package pleocmd.itfc.gui.dse;

import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
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
import pleocmd.cfg.ConfigDataMap;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.pipe.data.Data;

public final class DataSequenceTriggerPanel extends JPanel {

	private static final long serialVersionUID = -8090213458513567801L;

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

	public DataSequenceTriggerPanel(final ConfigDataMap cfgMap) {
		mapOrg = cfgMap;
		map = new ConfigDataMap(mapOrg.getLabel());
		map.assignFrom(mapOrg);

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

		dsePanel = new DataSequenceEditorPanel() {

			private static final long serialVersionUID = -4279793671317418153L;

			@Override
			protected void stateChanged() {
				// nothing to do here
			}

		};
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panel,
				dsePanel);
		splitPane.setResizeWeight(0.25);
		lay.addWholeLine(splitPane, true);

		updateTriggerModel();
		updateState();
	}

	public void saveChanges() {
		writeTextPaneToMap();
		mapOrg.setInternalMod(true);
		try {
			mapOrg.assignFrom(map);
		} finally {
			mapOrg.setInternalMod(false);
		}
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
			} else {
				map.setContent(trigger, list);
				modified();
			}
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
		triggerModel.set(map.getAllKeysSorted());
		Log.detail("Updating trigger model for map '%s' - last selected '%s'",
				map, lastSelected);
		triggerList.setSelectedValue(lastSelected, true);
	}

	protected void addNewTrigger() {
		final String name = JOptionPane.showInputDialog(this,
				"Name of the new trigger", "Add new trigger",
				JOptionPane.PLAIN_MESSAGE);
		if (name != null) {
			try {
				map.createContent(name);
				Log.detail("Added new trigger '%s' to '%s'", name, map);
				modified();
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
					Log.detail("Renamed trigger '%s' to '%s' in '%s'", trigger,
							name, map);
					modified();
				} catch (final ConfigurationException e) {
					Log.error(e);
				}
				trigger = name;
				updateTriggerModel();
				triggerList.setSelectedValue(name, true);
				updateState();
			}
		}
	}

	protected void removeSelectedTrigger() {
		if (trigger != null) {
			map.removeContent(trigger);
			Log.detail("Removed trigger '%s' from  '%s'", trigger, map);
			dsePanel.clear();
			trigger = null;
			updateTriggerModel();
			updateState();
			modified();
		}
	}

	public void modified() {
		mapOrg.dstpModified(map);
	}

	protected void updateState() {
		btnAddTrigger.setEnabled(true);
		btnRenameTrigger.setEnabled(trigger != null);
		btnRemoveTrigger.setEnabled(trigger != null);
		dsePanel.setEnabled(trigger != null);
		dsePanel.updateState();
	}

	public void externalChanged(final ConfigDataMap other) {
		Log.detail("Replacing map '%s' with '%s' due to external change", map,
				other);
		map.assignFrom(other);
		updateTriggerModel();
		updateState();
		modified();
	}

	public ConfigDataMap getMap() {
		return map;
	}

}
