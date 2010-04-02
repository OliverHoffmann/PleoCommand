package pleocmd.itfc.gui.dgr;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter;
import pleocmd.pipe.PipePart;

public class PipeVisualizationDialog extends JDialog {

	private static final long serialVersionUID = 2818789810493796194L;

	private final PipePart part;

	private final Diagram diagram;

	private final List<DiagramDataSet> dataSets;

	public PipeVisualizationDialog(final PipePart part, final int dataSetCount) {
		this.part = part;

		Log.detail("Creating Pipe-Visualization-Dialog");
		setTitle("Visualization for " + part.getClass().getSimpleName());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);
		diagram = new Diagram();
		lay.addWholeLine(diagram, true);

		// Add DataSets
		dataSets = new ArrayList<DiagramDataSet>(dataSetCount);
		for (int i = 0; i < dataSetCount; ++i)
			dataSets.add(new DiagramDataSet(diagram, String.format("Data #%d",
					i + 1)));

		setSize(400, 300);
		setLocationRelativeTo(null);
		Log.detail("Pipe-Visualization-Dialog created");
	}

	protected void close() {
		dispose();
		part.setVisualize(false);
	}

	public DiagramDataSet getDataSet(final int index) {
		return index < 0 || index >= dataSets.size() ? null : dataSets
				.get(index);
	}

	public void plot(final int index, final double x, final double y) {
		if (index < 0 || index >= dataSets.size()) {
			Log.detail("Ignoring plotting of invalid DataSet %d", index);
			return;
		}
		dataSets.get(index).addPoint(new Point2D.Double(x, y));
		repaint();
	}
}
