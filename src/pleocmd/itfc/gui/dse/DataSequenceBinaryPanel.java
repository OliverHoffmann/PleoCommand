package pleocmd.itfc.gui.dse;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pleocmd.itfc.gui.Layouter;

public abstract class DataSequenceBinaryPanel extends JPanel {

	private static final long serialVersionUID = 3987426369125034916L;

	private final HexTable table;

	public DataSequenceBinaryPanel() {
		final Layouter lay = new Layouter(this);
		lay.addWholeLine(new JScrollPane(table = new HexTable() {
			private static final long serialVersionUID = -7249872242413008237L;

			@Override
			protected void stateChanged() {
				DataSequenceBinaryPanel.this.stateChanged();
			}
		}), true);
	}

	protected abstract void stateChanged();

	public void updateState() {
		stateChanged();
	}

	public void freeResources() {
		setTableToStream(null);
	}

	public void setTableToStream(final RandomAccess stream) {
		table.getModel().setStream(stream);
	}

	public RandomAccess getTableStream() {
		return table.getModel().getStream();
	}

	public boolean isModified() {
		return table.getModel().isModified();
	}

	public void resetModification() {
		table.getModel().resetModification();
	}

}
