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

import java.awt.Font;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public abstract class HexTable extends JTable {

	private static final long serialVersionUID = 6105084549053202222L;

	public HexTable() {
		// work-around because HexTable.this is accessibly only after super ctor
		super(new HexTableModel() {
			private static final long serialVersionUID = 2216669809679475141L;

			@Override
			protected void stateChanged() {
				// nothing
			}
		});
		setModel(new HexTableModel() {
			private static final long serialVersionUID = -4243689487667229928L;

			@Override
			protected void stateChanged() {
				HexTable.this.stateChanged();
			}
		});

		setDefaultRenderer(Object.class, new HexTableCellRenderer());
		setDefaultEditor(Object.class, new HexTableCellEditor(getModel()));

		setShowGrid(false);
		setRowSelectionAllowed(false);
		setCellSelectionEnabled(true);
		setFont(new Font("monospaced", Font.PLAIN, getFont().getSize()));

		// getColumnModel().getSelectionModel().addListSelectionListener(
		// new ListSelectionListener() {
		//
		// public void valueChanged(final ListSelectionEvent e) {
		// repaint();
		// }
		// });

		addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(final HierarchyEvent e) {
				updateColCount();
			}
		});
		updateColCount();
	}

	@Override
	public final HexTableModel getModel() {
		return (HexTableModel) super.getModel();
	}

	public final void updateColCount() {
		final int textWidth = (int) getFontMetrics(getFont()).getStringBounds(
				"CC", getGraphics()).getWidth();
		final int colWidth = textWidth + 5;
		getModel().updateColumnCount(getWidth() / colWidth);
		for (int i = 0; i < getColumnModel().getColumnCount(); ++i) {
			final TableColumn tc = getColumnModel().getColumn(i);
			tc.setResizable(false);
			tc.setMinWidth(colWidth);
			tc.setMaxWidth(colWidth);
			tc.setWidth(colWidth);
			tc.setHeaderValue(String.format("%02d", i));
		}
	}

	protected abstract void stateChanged();

}
