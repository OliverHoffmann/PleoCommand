package pleocmd.itfc.gui;

import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter.Button;

public final class MainLogPanel extends JPanel {

	private static final long serialVersionUID = -6921879308383765734L;

	private final JTable logTable;

	private final LogTableModel logModel;

	private final JButton btnStart;

	public MainLogPanel() {
		final Layouter lay = new Layouter(this);

		logModel = new LogTableModel();
		logTable = new JTable(logModel) {

			private static final long serialVersionUID = 1128237812769648620L;

			@Override
			public String getToolTipText(final MouseEvent event) {
				return getBacktrace(rowAtPoint(event.getPoint()));
			}

		};
		logTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(50);
		logTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(50);
		logTable.getTableHeader().getColumnModel().getColumn(1)
				.setMinWidth(200);
		logTable.getTableHeader().getColumnModel().getColumn(1)
				.setMaxWidth(200);
		logTable.getTableHeader().setVisible(false);
		logTable.setShowGrid(false);
		logTable.setEnabled(false);
		lay.addWholeLine(new JScrollPane(logTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), true);

		btnStart = lay.addButton("Start", "arrow-right",
				"Starts the currently configured pipe", new Runnable() {
					@Override
					public void run() {
						MainFrame.the().startPipeThread();
					}
				});
		lay.addButton(Button.SaveTo, "Saves the whole log to a text file",
				new Runnable() {
					@Override
					public void run() {
						writeLogToFile();
					}
				});
		lay.addSpacer();
		lay.addButton(Button.Clear, "Empties the whole log list",
				new Runnable() {
					@Override
					public void run() {
						clearLog();
					}
				});
	}

	public JButton getBtnStart() {
		return btnStart;
	}

	protected String getBacktrace(final int index) {
		final Throwable bt = logModel.getLogAt(index).getBacktrace();
		if (bt == null) return null;
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		bt.printStackTrace(pw);
		pw.flush();
		return String.format("<html>%s</html>", sw.toString().replace("<",
				"&lt;").replace("\n", "<br>"));
	}

	public void writeLogToFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Ascii-Logfile",
				"log"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) try {
			logModel.writeToFile(fc.getSelectedFile());
		} catch (final IOException exc) {
			Log.error(exc);
		}
	}

	public void clearLog() {
		logModel.clear();
	}

	public void addLog(final Log log) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				addLog0(log);
			}
		});
	}

	protected void addLog0(final Log log) {
		logModel.addLog(log);
		logTable.scrollRectToVisible(logTable.getCellRect(logModel
				.getRowCount() - 1, 0, true));
	}

}
