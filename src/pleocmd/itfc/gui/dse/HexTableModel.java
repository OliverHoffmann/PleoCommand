package pleocmd.itfc.gui.dse;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import pleocmd.Log;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.dse.HexTableCellRenderer.Cell;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Syntax;

public abstract class HexTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3288312058227312945L;

	private int columnCount;

	private int rowCount;

	private RandomAccess stream;

	private final NavigableMap<Long, Color[]> map;

	private boolean modified;

	public HexTableModel() {
		map = new TreeMap<Long, Color[]>();
	}

	public final void updateColumnCount(final int newColumnCount) {
		columnCount = newColumnCount;
		updateRowCount();
	}

	public final void updateRowCount() {
		long size;
		try {
			size = stream == null ? 0 : stream.length();
			rowCount = columnCount == 0 ? 0 : (int) (size / columnCount + (size
					% columnCount > 0 ? 1 : 0));
			fireTableStructureChanged();
		} catch (final IOException e) {
			columnCount = 0;
			rowCount = 0;
		}
	}

	public final void setStream(final RandomAccess stream) {
		map.clear();
		if (this.stream != null) try {
			this.stream.close();
		} catch (final IOException e) {
			Log.error(e, "Cannot free resources");
		}
		this.stream = stream;
		updateRowCount();
		resetModification();
	}

	public final RandomAccess getStream() {
		return stream;
	}

	@Override
	public final int getColumnCount() {
		return columnCount;
	}

	@Override
	public final int getRowCount() {
		return rowCount;
	}

	@Override
	public final Cell getValueAt(final int rowIndex, final int columnIndex) {
		if (stream == null) return new Cell("", Color.BLACK);
		final long pos = (long) rowIndex * columnCount + columnIndex;
		Long startPos = map.floorKey(pos);
		if (startPos == null) {
			parse(pos);
			startPos = map.floorKey(pos);
		}
		Color[] ca;
		int relPos;
		if (startPos == null) {
			ca = new Color[] { Color.RED };
			relPos = 0;
		} else {
			ca = map.get(startPos);
			relPos = (int) (pos - startPos);
		}
		if (relPos >= ca.length) {
			parse(pos);
			startPos = map.floorKey(pos);
			ca = map.get(startPos);
			relPos = (int) (pos - startPos);
		}
		try {
			stream.seek(pos);
			return new Cell(String.format("%02X", stream.read()),
					relPos < ca.length ? ca[relPos] : Color.RED);
		} catch (final IOException e) {
			return new Cell("", Color.BLACK);
		}
	}

	@Override
	public final boolean isCellEditable(final int rowIndex,
			final int columnIndex) {
		return true;
	}

	@Override
	public final void setValueAt(final Object aValue, final int rowIndex,
			final int columnIndex) {
		if (aValue instanceof Cell)
			editing(((Cell) aValue).getString(), rowIndex, columnIndex);
	}

	private void parse(final long endPos) {
		final long pos = map.isEmpty() ? 0 : map.lastKey()
				+ map.lastEntry().getValue().length;
		try {
			stream.seek(pos);
			long startPos;
			while ((startPos = stream.getFilePointer()) <= endPos
					&& startPos < stream.length()) {
				final List<Syntax> syntaxList = new ArrayList<Syntax>();
				try {
					Data.createFromBinary(stream.getDataInput(), syntaxList);
				} catch (final FormatException e) {
					// already handled in the resulting syntax-list
				}
				final Color[] ca = new Color[(int) (stream.getFilePointer() - startPos)];
				int i = 0;
				Color c = Color.RED;
				for (final Syntax stx : syntaxList) {
					for (; i < stx.getPosition(); ++i)
						ca[i] = c;
					c = stx.getType().getColor();
				}
				for (; i < ca.length; ++i)
					ca[i] = c;
				map.put(startPos, ca);
			}
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	private void update(final long pos) {
		final Long startPos = map.floorKey(pos);
		if (startPos != null) {
			final Iterator<Color[]> it = map.tailMap(startPos).values()
					.iterator();
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
			fireTableDataChanged();
		}
	}

	public final boolean isModified() {
		return modified;
	}

	protected abstract void stateChanged();

	final void editing(final String value, final int rowIndex,
			final int columnIndex) {
		final long pos = (long) rowIndex * columnCount + columnIndex;
		try {
			stream.seek(pos);
			stream.write(Integer.valueOf(value, 16));
		} catch (final IOException e) {
			Log.error(e);
		}
		modified = true;
		stateChanged();
		update(pos);
	}

	public final void resetModification() {
		modified = false;
		stateChanged();
	}

}
