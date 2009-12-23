package pleocmd.itfc.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pleocmd.Log;
import pleocmd.Log.Type;

public final class LogTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 4577491604077043435L;

	private final List<Log> list = new ArrayList<Log>();

	private String mark = "XXXXXXXXXX";

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
			return list.get(rowIndex).getCaller();
		case 3:
			return list.get(rowIndex).getMsg();
		default:
			return "???";
		}
	}

	private String getAttributes(final int rowIndex, final int columnIndex) {
		String res = String.format(" color=%s", list.get(rowIndex)
				.getTypeColor());
		if (columnIndex == 2 && list.get(rowIndex).getCaller().startsWith(mark))
			res += String.format(" bgcolor=#FFFF80");
		return res;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		return String.format("<html><span%s><nobr>%s</nobr></span></html>",
				getAttributes(rowIndex, columnIndex), getCell(rowIndex,
						columnIndex));
	}

	public void addLog(final Log log) {
		list.add(log);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}

	public void clear() {
		list.clear();
		fireTableDataChanged();
	}

	public void refresh() {
		if (!Log.isLogDetailed()) {
			// remove all detailed logs from our list
			final List<Log> newList = new ArrayList<Log>();
			for (final Log log : list)
				if (log.getType() != Type.Detail) newList.add(log);
			if (list.size() != newList.size()) {
				list.clear();
				list.addAll(newList);
				fireTableDataChanged();
			}
		}
	}

	public void writeToFile(final File file) throws IOException {
		final FileWriter out = new FileWriter(file);
		for (final Log log : list) {
			out.write(log.toString());
			out.write("\n");
		}
		out.close();
	}

	public Log getLogAt(final int index) {
		return list.get(index);
	}

	public void setMark(final int index) {
		mark = list.get(index).getCaller();
		fireTableDataChanged();
	}

}
