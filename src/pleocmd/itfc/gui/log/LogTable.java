package pleocmd.itfc.gui.log;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import pleocmd.Log;

public final class LogTable extends JTable {

	private static final long serialVersionUID = -8728774076766042538L;

	private LogTableModel logModel;

	private int minRowHeight;

	public LogTable() {
		setDefaultRenderer(Object.class, new LogTableCellRenderer());

		getTableHeader().setEnabled(false);
		setShowGrid(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 1)
					getLogModel().setMark(getSelectedRow());
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				updateRowHeights();
			}
		});

	}

	public void setLogModel(final LogTableModel logModel) {
		this.logModel = logModel;
		setModel(logModel);

		final TableColumnModel tcm = getTableHeader().getColumnModel();
		tcm.getColumn(0).setHeaderValue("Time");
		tcm.getColumn(0).setMinWidth(100);
		tcm.getColumn(0).setMaxWidth(100);

		tcm.getColumn(1).setHeaderValue("Type");
		tcm.getColumn(1).setMinWidth(50);
		tcm.getColumn(1).setMaxWidth(50);

		tcm.getColumn(2).setHeaderValue("Source");
		tcm.getColumn(2).setMinWidth(200);
		tcm.getColumn(2).setMaxWidth(200);

		tcm.getColumn(3).setHeaderValue("Message");
	}

	public LogTableModel getLogModel() {
		return logModel;
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		final Throwable bt = logModel.getLogAt(rowAtPoint(event.getPoint()))
				.getBacktrace();
		if (bt == null) return null;
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		bt.printStackTrace(pw); // CS_IGNORE
		pw.flush();
		return String.format("<html>%s</html>", sw.toString().replace("<",
				"&lt;").replace("\n", "<br>"));
	}

	public void updateRowHeights() {
		if (getRowCount() == 0) return;
		final TableCellRenderer tcr = getCellRenderer(0, 3);
		minRowHeight = prepareRenderer(getCellRenderer(0, 0), 0, 0)
				.getPreferredSize().height;
		for (int i = getRowCount() - 1; i >= 0; --i)
			setRowHeight(i, 2 + Math.max(minRowHeight, prepareRenderer(tcr, i,
					3).getPreferredSize().height));
	}

	public void addLog(final Log log) {
		logModel.addLog(log);
		final int row = logModel.getRowCount() - 1;
		if (minRowHeight == 0)
			minRowHeight = prepareRenderer(getCellRenderer(0, 0), 0, 0)
					.getPreferredSize().height;
		setRowHeight(row, 2 + Math.max(minRowHeight, prepareRenderer(
				getCellRenderer(row, 3), row, 3).getPreferredSize().height));
		scrollRectToVisible(getCellRect(row, 0, true));
	}

}
