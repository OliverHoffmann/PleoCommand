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

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import pleocmd.itfc.gui.dse.HexTableCellRenderer.Cell;

public final class HexTableCellEditor extends DefaultCellEditor {

	private static final long serialVersionUID = -7011040614747253433L;

	final HexTableModel model; // CS_IGNORE protected access, but final

	public HexTableCellEditor(final HexTableModel model) {
		super(new JTextField());
		this.model = model;
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table,
			final Object value, final boolean isSelected, final int row,
			final int column) {
		final Component cmp = super.getTableCellEditorComponent(table, value,
				isSelected, row, column);
		if (cmp instanceof JTextField && value instanceof Cell) {
			final JTextField tf = (JTextField) cmp;
			tf.setDocument(new PlainDocument() {

				private static final long serialVersionUID = 7127019078029616451L;

				public boolean isHexString(final String s) {
					try {
						Integer.parseInt(s, 16);
						return true;
					} catch (final NumberFormatException e) {
						return false;
					}
				}

				@Override
				public void insertString(final int offs, final String str,
						final AttributeSet a) throws BadLocationException {
					if (tf.getCaretPosition() == 2) tf.setCaretPosition(1);
					if (str == null || !isHexString(str)) return;
					final int newOffs = offs == 2 ? 0 : offs;
					final String newStr = newOffs + str.length() > 2 ? str
							.substring(0, 2 - newOffs) : str;

					if (newOffs < getLength())
						remove(newOffs, newStr.length());
					super.insertString(newOffs, newStr.toUpperCase(), a);
					model.editing(tf.getText(), row, column);
				}
			});
			tf.setText(((Cell) value).getString());
			tf.setForeground(((Cell) value).getColor());
			tf.selectAll();
		}
		return cmp;
	}

	@Override
	public Object getCellEditorValue() {
		final Object value = super.getCellEditorValue();
		if (value instanceof String)
			return new Cell((String) value, Color.BLACK);
		return new Cell("", Color.BLACK);
	}

}
