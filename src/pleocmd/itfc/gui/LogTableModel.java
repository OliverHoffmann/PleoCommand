package pleocmd.itfc.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pleocmd.Log.MsgType;

public final class LogTableModel extends AbstractTableModel {

	private class Log {
		private final MsgType type;
		private final String caller;
		private final String msg;

		public Log(final MsgType type, final String caller, final String msg) {
			this.type = type;
			this.caller = caller;
			this.msg = msg;
		}

		public MsgType getType() {
			return type;
		}

		public String getCaller() {
			return caller;
		}

		public String getMsg() {
			return msg;
		}

		public String getColor() {
			switch (type) {
			case Detail:
				return "#A0A0A0"; // gray
			case Info:
				return "#000000"; // black
			case Warn:
				return "#FFA020"; // orange
			case Error:
				return "#FF0000"; // red
			default:
				return "#000000";
			}
		}

	}

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
			return "<html><span color=" + list.get(rowIndex).getColor() + ">"
					+ list.get(rowIndex).getMsg() + "</span></html>";
		default:
			return "???";
		}
	}

	public void addLog(final MsgType type, final String caller, final String msg) {
		list.add(new Log(type, caller, msg));
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}
}
