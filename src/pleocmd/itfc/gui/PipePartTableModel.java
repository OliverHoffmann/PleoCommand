package pleocmd.itfc.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import pleocmd.cfg.ConfigValue;
import pleocmd.pipe.PipePart;

public final class PipePartTableModel<E extends PipePart> extends
		AbstractTableModel {

	private static final long serialVersionUID = -815026047488409255L;

	private final List<E> list = new ArrayList<E>();

	private final Map<E, List<String>> valueMap = new HashMap<E, List<String>>();

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
		final PipePart pp = list.get(rowIndex);
		return columnIndex > pp.getGroup().getSize() ? "" : pp.getGroup().get(
				valueMap.get(pp).get(columnIndex - 1));
	}

	private void detectMaxConfigs() {
		int mc = 0;
		for (final E pp : list)
			mc = Math.max(mc, pp.getGroup().getSize());
		if (maxConfigs != mc) {
			maxConfigs = mc;
			fireTableStructureChanged();
		}
	}

	private void addPipePart0(final E pp) {
		final List<String> labels = new ArrayList<String>(pp.getGroup()
				.getSize());
		for (final ConfigValue value : pp.getGroup().getValueMap().values())
			labels.add(value.getLabel());
		valueMap.put(pp, labels);
		list.add(pp);
	}

	public void addPipePart(final E pp) {
		addPipePart0(pp);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
		detectMaxConfigs();
	}

	public void addPipeParts(final List<E> ppList) {
		for (final E pp : ppList)
			addPipePart0(pp);
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
