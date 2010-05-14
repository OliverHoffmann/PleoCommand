package pleocmd.itfc.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import pleocmd.Log;
import pleocmd.cfg.ConfigString;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;

final class HistoryListModel extends AbstractListModel implements
		ConfigurationInterface {

	private static final long serialVersionUID = 4510015901086617192L;

	private final List<String> history = new ArrayList<String>();

	private final ConfigString cfgHistory = new ConfigString("history", true);

	public HistoryListModel() {
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	@Override
	public int getSize() {
		return history.size();
	}

	@Override
	public String getElementAt(final int index) {
		return history.get(index);
	}

	public void add(final String line) {
		history.add(line);
		fireIntervalAdded(this, history.size() - 1, history.size() - 1);
	}

	public void clear() {
		final int size = history.size();
		history.clear();
		fireIntervalRemoved(this, 0, size - 1);
	}

	public List<String> getAll() {
		return Collections.unmodifiableList(history);
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(cfgHistory);
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
		clear();
		history.addAll(cfgHistory.getContentList());
		fireIntervalAdded(this, 0, history.size() - 1);
		cfgHistory.clearContent(); // let GC free memory
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		cfgHistory.setContent(history);
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}
}
