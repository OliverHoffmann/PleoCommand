package pleocmd.itfc.gui.log;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import pleocmd.Log;
import pleocmd.StringManip;

public final class LogTableModel extends AbstractTableModel {

	private static final String NOMARK = "XXXXXXXXXX";

	private static final long serialVersionUID = 4577491604077043435L;

	private static final Color CLR_MARK = new Color(1.0f, 1.0f, 0.8f, 1.0f);

	private final List<Log> list = new ArrayList<Log>();

	private final LogTable table;

	private String mark = NOMARK;

	public LogTableModel(final LogTable table) {
		this.table = table;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	private String getCell(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
		case 0:
			return list.get(rowIndex).getFormattedTime();
		case 1:
			return list.get(rowIndex).getType().toString();
		case 2:
			return list.get(rowIndex).getFormattedCaller();
		case 3:
			return list.get(rowIndex).getMsg();
		default:
			return "???";
		}
	}

	@Override
	public LogTableStyledCell getValueAt(final int rowIndex,
			final int columnIndex) {
		return new LogTableStyledCell(getCell(rowIndex, columnIndex),
				columnIndex == 3, list.get(rowIndex).getTypeColor(),
				getBackgroundColor(rowIndex, columnIndex), false, false);
	}

	private Color getBackgroundColor(final int rowIndex, final int columnIndex) {
		if (columnIndex != 2) return null;
		if (list.get(rowIndex).getFormattedCaller().startsWith(mark))
			return CLR_MARK;
		return null;
	}

	protected void addLog(final Log log) {
		list.add(log);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}

	public void clear() {
		list.clear();
		fireTableDataChanged();
	}

	public void refresh() {
		// remove all currently not processed logs from our list
		final int minType = Log.getMinLogType().ordinal();
		final List<Log> newList = new ArrayList<Log>();
		for (final Log log : list)
			if (log.getType().ordinal() >= minType) newList.add(log);
		if (list.size() != newList.size()) {
			list.clear();
			list.addAll(newList);
			fireTableDataChanged();
			table.updateRowHeights();
		}
	}

	public void writeToFile(final File file) throws IOException {
		final FileWriter out = new FileWriter(file);
		try {
			if (file.getPath().endsWith(".html"))
				writeToHTMLFile(out);
			else if (file.getPath().endsWith(".tex"))
				writeToTexFile(out);
			else
				writeToAsciiFile(out);
		} finally {
			out.close();
		}
	}

	private void writeToHTMLFile(final FileWriter out) throws IOException {
		out.write("<html>\n");
		out.write("<body>\n");
		out.write("<table border=0>\n");
		for (final Log log : list) {
			out.write(String.format("<tr style=\"color:%s\">", log
					.getTypeHTMLColor()));
			out.write(log.toHTMLString());
			out.write("</tr>\n");
		}
		out.write("</table>\n");
		out.write("</body>\n");
		out.write("</html>\n");
	}

	private void writeToTexFile(final FileWriter out) throws IOException {
		final Set<String> colorNames = new HashSet<String>();
		final String ec = Log.getExportColumns();
		final StringBuilder sb = new StringBuilder("\\begin{longtable}{");
		double sum = 0;
		if (ec.contains("T")) sum += 2;
		if (ec.contains("Y")) sum += 1;
		if (ec.contains("S")) sum += 3;
		if (ec.contains("M")) sum += 4;
		if (ec.contains("T")) {
			if (sb.length() > 18) sb.append(" | ");
			sb.append(String.format("p{%f\\textwidth}", 2 / sum));
		}
		if (ec.contains("Y")) {
			if (sb.length() > 18) sb.append(" | ");
			sb.append(String.format("p{%f\\textwidth}", 1 / sum));
		}
		if (ec.contains("S")) {
			if (sb.length() > 18) sb.append(" | ");
			sb.append(String.format("p{%f\\textwidth}", 3 / sum));
		}
		if (ec.contains("M")) {
			if (sb.length() > 18) sb.append(" | ");
			sb.append(String.format("p{%f\\textwidth}", 4 / sum));
		}
		sb.append("}\n");
		for (final Log log : list) {
			sb.append(log.toTexString(colorNames));
			sb.append("\\\\\n");
		}
		sb.append("\\end{longtable}\n");
		out.write("\\definecolor{orange}{RGB}{160,100,0}\n");
		for (final String cn : colorNames) {
			final Color c = StringManip.hexToColor(cn
					.substring("PleoCommandColor".length()));
			out.write(String.format("\\definecolor{%s}{RGB}{%d,%d,%d}\n", cn, c
					.getRed(), c.getGreen(), c.getBlue()));
		}
		out.write("\n\n");
		out.write(sb.toString());
	}

	private void writeToAsciiFile(final FileWriter out) throws IOException {
		for (final Log log : list) {
			out.write(log.toString());
			out.write("\n");
		}
	}

	protected Log getLogAt(final int index) {
		return list.get(index);
	}

	public void setMark(final int index) {
		mark = index == -1 ? NOMARK : list.get(index).getFormattedCaller();
		table.repaint();
	}

}
