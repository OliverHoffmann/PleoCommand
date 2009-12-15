package pleocmd.itfc.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pleocmd.Log;

public final class LogTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 4577491604077043435L;

	private final List<Log> list = new ArrayList<Log>();

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
		case 0:
			return list.get(rowIndex).getType();
		case 1:
			return list.get(rowIndex).getCaller();
		case 2:
			return String.format("<html><span color=%s>%s</span></html>", list
					.get(rowIndex).getTypeColor(), list.get(rowIndex).getMsg());
		default:
			return "???";
		}
	}

	public void addLog(final Log log) {
		list.add(log);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}

	public void clear() {
		list.clear();
		fireTableDataChanged();
	}

	public void writeToFile(final File file) throws IOException {
		final FileWriter out = new FileWriter(file);
		for (final Log log : list) {
			out.write(log.toString());
			out.write("\n");
		}
		out.close();
	}

}
