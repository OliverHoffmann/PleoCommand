package pleocmd.itfc.gui.log;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

public final class LogTableCellRenderer implements TableCellRenderer {

	private final DefaultTableCellRenderer def = new DefaultTableCellRenderer();

	private final JTextField textField = new JTextField();

	private final JTextArea textArea = new JTextArea();

	public LogTableCellRenderer() {
		textField.setEditable(false);

		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean sel, final boolean foc,
			final int row, final int column) {
		def.getTableCellRendererComponent(table, null, sel, foc, 0, 0);
		if (!(value instanceof LogTableStyledCell)) return def;
		// returning "def" is the same as returning result of above method

		final LogTableStyledCell sc = (LogTableStyledCell) value;

		final JTextComponent comp = sc.isMultiLine() ? textArea : textField;
		comp.setBorder(def.getBorder());
		comp.setText(sc.getText());
		comp.setForeground(sc.getForeground() == null ? def.getForeground()
				: sc.getForeground());
		comp.setBackground(sc.getBackground() == null ? def.getBackground()
				: sc.getBackground());
		Font font = def.getFont();
		if (sc.isBold()) font = font.deriveFont(Font.BOLD);
		if (sc.isItalic()) font = font.deriveFont(Font.ITALIC);
		comp.setFont(font);

		// force the component to use all of the available space in its cell
		comp.setSize(table.getColumnModel().getColumn(column).getWidth(),
				Integer.MAX_VALUE);

		return comp;
	}

}
