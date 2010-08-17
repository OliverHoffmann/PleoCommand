// This file is part of PleoCommand:
// Interactively control Pleo with psychobiological parameters
//
// Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Boston, USA.

package pleocmd.itfc.gui.dgr;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import pleocmd.Log;
import pleocmd.itfc.gui.AutoDisposableWindow;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.MainFrame;
import pleocmd.pipe.PipePart;

public final class PipeVisualizationDialog extends JDialog implements
		AutoDisposableWindow {

	private static final long serialVersionUID = 2818789810493796194L;

	private static final long TIME_SPAN = 3 * 1000;

	private final PipePart part;

	private final Diagram diagram;

	private final List<DiagramDataSet> dataSets;

	public PipeVisualizationDialog(final PipePart part, final int dataSetCount) {
		this.part = part;

		Log.detail("Creating Pipe-Visualization-Dialog");
		setTitle("Visualization for " + part.getName());
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
		diagram.getXAxis().setMin(0);
		diagram.getXAxis().setMax(TIME_SPAN);
		diagram.getXAxis().setUnitName("s");
		diagram.getXAxis().setSubsPerUnit(10);

		// Add DataSets
		dataSets = new ArrayList<DiagramDataSet>(dataSetCount);
		for (int i = 0; i < dataSetCount; ++i) {
			final DiagramDataSet ds = new DiagramDataSet(diagram, String
					.format("#%d", i + 1));
			ds.setValuePerUnitX(1000);
			dataSets.add(ds);
		}

		setSize(400, 300);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		Log.detail("Pipe-Visualization-Dialog created");
		MainFrame.the().addKnownWindow(this);

		diagram.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					getDiagram().getMenu().show(PipeVisualizationDialog.this,
							e.getX(), e.getY());
			}
		});

	}

	protected void close() {
		MainFrame.the().removeKnownWindow(this);
		dispose();
		part.setVisualize(false);
	}

	@Override
	public void autoDispose() {
		MainFrame.the().removeKnownWindow(this);
		dispose();
	}

	public Diagram getDiagram() {
		return diagram;
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
		final long elapsed = part.getPipe().getFeedback().getElapsed();
		final double rx = Double.isNaN(x) ? elapsed : x;
		dataSets.get(index).addPoint(new Point2D.Double(rx, y));
		diagram.getXAxis().setMin(Math.max(0, elapsed - TIME_SPAN) / 1000.0);
		diagram.getXAxis().setMax(Math.max(TIME_SPAN, elapsed) / 1000.0);
	}

	public void reset(final int dataSetCount) {
		if (dataSetCount < 0 || dataSetCount > 128)
			throw new IllegalArgumentException("dataSetCount is "
					+ dataSetCount);
		// remove all old data
		for (final DiagramDataSet ds : dataSets)
			ds.setPoints(new ArrayList<Point2D.Double>());

		// make sure that the number of DataSets is correct
		while (dataSetCount > dataSets.size()) {
			final DiagramDataSet ds = new DiagramDataSet(diagram, String
					.format("#%d", dataSets.size() + 1));
			ds.setValuePerUnitX(1000);
			dataSets.add(ds);
		}
		while (dataSetCount < dataSets.size())
			dataSets.remove(dataSets.size() - 1);
	}

}
