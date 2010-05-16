package pleocmd.itfc.gui.dse;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class HexTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -7878467781606848084L;

	public static class Cell {
		private final String string;

		private final Color color;

		public Cell(final String string, final Color color) {
			this.string = string;
			this.color = color;
		}

		public String getString() {
			return string;
		}

		public Color getColor() {
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
