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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;

final class DataSequenceEditorListModel extends AbstractListModel {

	private static final long serialVersionUID = 5162824240598187154L;

	private final List<String> triggers = new ArrayList<String>();

	@Override
	public int getSize() {
		return triggers.size();
	}

	@Override
	public String getElementAt(final int index) {
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
