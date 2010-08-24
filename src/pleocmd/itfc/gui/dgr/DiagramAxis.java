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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import pleocmd.itfc.gui.dgr.DiagramDataSet.DiagramType;

public final class DiagramAxis {

	public enum AxisScale {
		LinearScale, LogarithmicScale
	}

	private final Diagram diagram;

	private String axisName;

	private String unitName = "";

	private AxisScale scale = AxisScale.LinearScale;

	private double min = -Double.MAX_VALUE;

	private double max = Double.MAX_VALUE;

	private boolean reversed;

	private int subsPerUnit = 5;

	private double offset;

	private double cachedMinUnit;

	private double cachedMaxUnit;

	private double cachedMinVisUnit;

	private double cachedMaxVisUnit;

	private int cachedUnitsPerGrid;

	private double cachedPixelPerUnit;

	private double cachedPixelPerGrid;

	private double cachedPixelPerSubGrid;

	protected DiagramAxis(final Diagram diagram, final String axisName) {
		this.diagram = diagram;
		this.axisName = axisName;
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(final String axisName) {
		synchronized (diagram) {
			this.axisName = axisName;
			diagram.repaint();
		}
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(final String unitName) {
		synchronized (diagram) {
			this.unitName = unitName;
			diagram.repaint();
		}
	}

	public AxisScale getScale() {
		return scale;
	}

	public void setScale(final AxisScale scale) {
		synchronized (diagram) {
			this.scale = scale;
			diagram.repaint();
		}
	}

	public double getMin() {
		return min;
	}

	public void setMin(final double min) {
		synchronized (diagram) {
			this.min = min;
			diagram.repaint();
		}
	}

	public double getMax() {
		return max;
	}

	public void setMax(final double max) {
		synchronized (diagram) {
			this.max = max;
			diagram.repaint();
		}
	}

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(final boolean reversed) {
		synchronized (diagram) {
			this.reversed = reversed;
			diagram.repaint();
		}
	}

	public int getSubsPerUnit() {
		return subsPerUnit;
	}

	public void setSubsPerUnit(final int subsPerUnit) {
		synchronized (diagram) {
			this.subsPerUnit = subsPerUnit;
			diagram.repaint();
		}
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(final double offset) {
		synchronized (diagram) {
			this.offset = offset;
			diagram.repaint();
		}
	}

	void updateCache(final int availPixels) {
		cachedMinUnit = min;
		cachedMaxUnit = max;
		final DiagramAxis xAxis = diagram.getXAxis();
		final boolean isXAxis = xAxis == this;
		if (max >= Double.MAX_VALUE - Double.MIN_NORMAL
				|| min <= Double.MIN_NORMAL - Double.MAX_VALUE) {
			double low = Double.MAX_VALUE;
			double high = -Double.MAX_VALUE;
			for (final DiagramDataSet ds : diagram.getDataSets()) {
				if (!isXAxis && ds.getType() == DiagramType.IntersectionDiagram)
					continue;
				if (!ds.isValid()) continue;
				for (final Point2D.Double pt : ds.getPoints()) {
					final double unitX = pt.x / ds.getValuePerUnitX();
					if (isXAxis) {
						// all values count
						low = Math.min(low, unitX);
						high = Math.max(high, unitX);
					} else {
						// only visible values count
						final double unitY = pt.y / ds.getValuePerUnitY();
						if (unitX >= xAxis.getCachedMinVisUnit()
								&& unitX <= xAxis.getCachedMaxVisUnit()) {
							low = Math.min(low, unitY);
							high = Math.max(high, unitY);
						}
					}
				}
			}
			if (low == Double.MAX_VALUE) low = 0;
			if (high == -Double.MAX_VALUE) high = 1;
			if (min <= Double.MIN_NORMAL - Double.MAX_VALUE)
				cachedMinUnit = low;
			if (max >= Double.MAX_VALUE - Double.MIN_NORMAL)
				cachedMaxUnit = high;
		}
		cachedPixelPerUnit = availPixels / (cachedMaxUnit - cachedMinUnit)
				* diagram.getZoom();
		final double visibleUnits = availPixels / cachedPixelPerUnit;
		cachedMinVisUnit = cachedMinUnit + offset;
		cachedMaxVisUnit = cachedMinVisUnit + visibleUnits;
		cachedUnitsPerGrid = Math
				.max(1,
						(int) (Diagram.MIN_GRID_DELTA
								* Math.max(subsPerUnit, 1) / cachedPixelPerUnit));
		cachedPixelPerGrid = cachedPixelPerUnit * cachedUnitsPerGrid;
		cachedPixelPerSubGrid = subsPerUnit > 1 ? cachedPixelPerGrid
				/ subsPerUnit : 0;
	}

	double unitToPixel(final double unit) {
		return (unit - cachedMinVisUnit) * cachedPixelPerUnit;
	}

	double getCachedMinUnit() {
		return cachedMinUnit;
	}

	double getCachedMaxUnit() {
		return cachedMaxUnit;
	}

	double getCachedMinVisUnit() {
		return cachedMinVisUnit;
	}

	double getCachedMaxVisUnit() {
		return cachedMaxVisUnit;
	}

	int getCachedUnitsPerGrid() {
		return cachedUnitsPerGrid;
	}

	double getCachedPixelPerUnit() {
		return cachedPixelPerUnit;
	}

	double getCachedPixelPerGrid() {
		return cachedPixelPerGrid;
	}

	double getCachedPixelPerSubGrid() {
		return cachedPixelPerSubGrid;
	}

	protected void createMenu(final JPopupMenu parent) {
		final JMenu menu = new JMenu(getAxisName() + "-Axis");
		parent.add(menu);
		JMenuItem item = new JMenuItem("Set Minimum");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String newMin = JOptionPane.showInputDialog(
						String.format("New Minimum Of %s-Axis", getAxisName()),
						getCachedMinUnit());
				if (newMin != null) setMin(Double.valueOf(newMin));
			}
		});
		item = new JMenuItem("Set Minimum To Automatic");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setMin(-Double.MAX_VALUE);
			}
		});
		item = new JMenuItem("Set Maximum");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String newMax = JOptionPane.showInputDialog(
						String.format("New Maximum Of %s-Axis", getAxisName()),
						getCachedMaxUnit());
				if (newMax != null) setMax(Double.valueOf(newMax));
			}
		});
		item = new JMenuItem("Set Maximum To Automatic");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setMax(Double.MAX_VALUE);
			}
		});
		item = new JMenuItem("Set Linear Scale");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setScale(AxisScale.LinearScale);
			}
		});
		item = new JMenuItem("Set Logarithmic Scale");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setScale(AxisScale.LogarithmicScale);
			}
		});

	}
}
