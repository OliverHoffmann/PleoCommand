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
