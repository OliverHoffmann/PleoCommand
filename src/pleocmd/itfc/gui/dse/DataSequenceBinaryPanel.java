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

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pleocmd.itfc.gui.Layouter;

public abstract class DataSequenceBinaryPanel extends JPanel {

	private static final long serialVersionUID = 3987426369125034916L;

	private final HexTable table;

	public DataSequenceBinaryPanel() {
		final Layouter lay = new Layouter(this);
		lay.addWholeLine(new JScrollPane(table = new HexTable() {
			private static final long serialVersionUID = -7249872242413008237L;

			@Override
			protected void stateChanged() {
				DataSequenceBinaryPanel.this.stateChanged();
			}
		}), true);
	}

	protected abstract void stateChanged();

	public final void updateState() {
		stateChanged();
	}

	public final void freeResources() {
		setTableToStream(null);
	}

	public final void setTableToStream(final RandomAccess stream) {
		table.getModel().setStream(stream);
	}

	public final RandomAccess getTableStream() {
		return table.getModel().getStream();
	}

	public final boolean isModified() {
		return table.getModel().isModified();
	}

	public final void resetModification() {
		table.getModel().resetModification();
	}

}
