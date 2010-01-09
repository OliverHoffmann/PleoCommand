package pleocmd.itfc.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;

public final class DataSequenceEditorListModel extends AbstractListModel {

	private static final long serialVersionUID = 5162824240598187154L;

	private final List<String> triggers = new ArrayList<String>();

	@Override
	public int getSize() {
		return triggers.size();
	}

	@Override
	public Object getElementAt(final int index) {
		return index == -1 ? null : triggers.get(index);
	}

	public void set(final Collection<String> newTriggers) {
		final int size = triggers.size();
		triggers.clear();
		if (size > 0) fireIntervalRemoved(this, 0, size - 1);
		triggers.addAll(newTriggers);
		if (newTriggers.size() > 0)
			fireIntervalAdded(this, 0, triggers.size() - 1);
	}

}
