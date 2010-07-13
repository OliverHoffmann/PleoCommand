package pleocmd.itfc.gui.log;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import pleocmd.Log;

final class LogTableCellRenderer implements TableCellRenderer {

	private final DefaultTableCellRenderer def = new DefaultTableCellRenderer();

	private final JTextField textField = new JTextField();

	private final JTextPane textArea = new JTextPane();

	public LogTableCellRenderer() {
		textField.setEditable(false);

		textArea.setEditable(false);
		// textArea.setLineWrap(true);
		// textArea.setWrapStyleWord(true);
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
		// comp.setText(sc.getText());
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

		if (comp instanceof JTextPane)
			try {
				deHTML((JTextPane) comp, sc.getText());
			} catch (final BadLocationException e) {
				// just ignore
			}
		else
			comp.setText(sc.getText());
		return comp;
	}

	private void deHTML(final JTextPane tp, final String text)
			throws BadLocationException {
		tp.setText("");
		final StyledDocument sd = tp.getStyledDocument();
		Style style = sd.getStyle(StyleContext.DEFAULT_STYLE);
		if (!text.startsWith("<html>")) {
			sd.insertString(0, text, style);
			return;
		}
		StringBuilder sb = new StringBuilder();
		int offset = 0;
		for (int i = 0; i < text.length(); ++i) {
			final char c = text.charAt(i);
			switch (c) {
			case '<': {
				sd.insertString(offset, sb.toString(), style);
				offset += sb.length();
				sb = new StringBuilder();
				final StringBuilder sb2 = new StringBuilder();
				char c2;
				while ((c2 = text.charAt(++i)) != '>')
					sb2.append(c2);
				final String tag = sb2.toString();
				if ("/font".equals(tag) || "/b".equals(tag) || "/i".equals(tag))
					// we don't support recursive attributes, so just ...
					style = sd.getStyle(StyleContext.DEFAULT_STYLE);
				else if (tag.startsWith("b")) {
					style = sd.addStyle(null, style);
					StyleConstants.setBold(style, true);
				} else if (tag.startsWith("i")) {
					style = sd.addStyle(null, style);
					StyleConstants.setItalic(style, true);
				} else if (tag.startsWith("font color=#")) {
					style = sd.addStyle(null, style);
					try {
						StyleConstants.setForeground(style, new Color(Integer
								.valueOf(tag.substring(12, 14), 16), Integer
								.valueOf(tag.substring(14, 16), 16), Integer
								.valueOf(tag.substring(16, 18), 16)));
					} catch (final NumberFormatException e) {
						StyleConstants.setForeground(style, Color.RED);
					}
				} else if ("html".equals(tag) || "/html".equals(tag)) {
					// just ignore them silently
				} else
					Log.detail("Ignoring unknown tag '%s'", tag);
				break;
			}
			case '&': {
				final StringBuilder sb2 = new StringBuilder();
				char c2;
				while ((c2 = text.charAt(++i)) != ';')
					sb2.append(c2);
				final String id = sb2.toString();
				if ("lt".equals(id))
					sb.append('<');
				else if ("gt".equals(id))
					sb.append('>');
				else if ("amp".equals(id))
					sb.append('&');
				else if ("quot".equals(id))
					sb.append('"');
				else
					Log.detail("Ignoring unknown &%s;", id); // CS_IGNORE
				break;
			}
			default:
				sb.append(c);
			}
		}
		sd.insertString(offset, sb.toString(), style);
	}
}
