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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public final class HexTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -7878467781606848084L;

	public static class Cell {

		private final String string;

		private final Color color;

		public Cell(final String string, final Color color) {
			this.string = string;
			this.color = color;
		}

		public final String getString() {
			return string;
		}

		public final Color getColor() {
			return color;
		}

	}

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		final Component cmp = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		if (value instanceof Cell) {
			final Cell cell = (Cell) value;
			setText(cell.getString());
			setForeground(cell.getColor());
		}
		return cmp;
	}

}
