package pleocmd.itfc.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pleocmd.pipe.PipePart;
import pleocmd.pipe.cfg.Config;

public final class PipePartTableModel<E extends PipePart> extends
		AbstractTableModel {

	private static final long serialVersionUID = -815026047488409255L;

	private final List<E> list = new ArrayList<E>();

	private int maxConfigs;

	@Override
	public int getColumnCount() {
		return 1 + maxConfigs;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (columnIndex == 0)
			return list.get(rowIndex).getClass().getSimpleName();
		final Config cfg = list.get(rowIndex).getConfig();
		return columnIndex > cfg.size() ? "" : cfg.get(columnIndex - 1);
	}

	private void detectMaxConfigs() {
		int mc = 0;
		for (final E pp : list)
			mc = Math.max(mc, pp.getConfig().size());
		if (maxConfigs != mc) {
			maxConfigs = mc;
			fireTableStructureChanged();
		}
	}

	public void addPipePart(final E pp) {
		list.add(pp);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
		detectMaxConfigs();
	}

	public void addPipeParts(final List<E> ppList) {
		for (final E pp : ppList)
			list.add(pp);
		fireTableDataChanged();
		detectMaxConfigs();
	}

	public void insertPipePart(final E pp, final int idx) {
		list.add(idx, pp);
		fireTableRowsInserted(idx, idx);
		detectMaxConfigs();
	}

	public void clear() {
		list.clear();
		fireTableDataChanged();
		detectMaxConfigs();
	}

	public E removePipePart(final int idx) {
		final E res = list.remove(idx);
		fireTableRowsDeleted(idx, idx);
		detectMaxConfigs();
		return res;
	}

	public E getPipePart(final int idx) {
		return list.get(idx);
	}

}
