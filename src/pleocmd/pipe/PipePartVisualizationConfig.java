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

package pleocmd.pipe;

import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.dgr.Diagram;
import pleocmd.itfc.gui.dgr.PipeVisualizationDialog;
import pleocmd.itfc.gui.dgr.DiagramAxis.AxisScale;

final class PipePartVisualizationConfig {

	private final PipePart part;

	private final ConfigBounds cfgPosition;

	private final ConfigDouble cfgXAxisMin;

	private final ConfigDouble cfgXAxisMax;

	private final ConfigEnum<AxisScale> cfgXAxisScale;

	private final ConfigDouble cfgYAxisMin;

	private final ConfigDouble cfgYAxisMax;

	private final ConfigEnum<AxisScale> cfgYAxisScale;

	public PipePartVisualizationConfig(final PipePart part, final Group group) {
		this.part = part;

		group.add(cfgPosition = new ConfigBounds("Visualization-Position"));

		group.add(cfgXAxisMin = new ConfigDouble("Visualization-X-Axis-Min",
				-Double.MAX_VALUE));
		group.add(cfgXAxisMax = new ConfigDouble("Visualization-X-Axis-Max",
				Double.MAX_VALUE));
		group.add(cfgXAxisScale = new ConfigEnum<AxisScale>(
				"Visualization-X-Axis-Scale", AxisScale.LinearScale));

		group.add(cfgYAxisMin = new ConfigDouble("Visualization-Y-Axis-Min",
				-Double.MAX_VALUE));
		group.add(cfgYAxisMax = new ConfigDouble("Visualization-Y-Axis-Max",
				Double.MAX_VALUE));
		group.add(cfgYAxisScale = new ConfigEnum<AxisScale>(
				"Visualization-Y-Axis-Scale", AxisScale.LinearScale));
	}

	public void writeback() throws ConfigurationException {
		final PipeVisualizationDialog dlg = part.getVisualizationDialog();
		if (dlg == null) return;
		final Diagram dgr = dlg.getDiagram();

		cfgPosition.setContent(dlg.getBounds());

		cfgXAxisMin.setContent(dgr.getXAxis().getMin());
		cfgXAxisMax.setContent(dgr.getXAxis().getMax());
		cfgXAxisScale.setEnum(dgr.getXAxis().getScale());

		cfgYAxisMin.setContent(dgr.getYAxis().getMin());
		cfgYAxisMax.setContent(dgr.getYAxis().getMax());
		cfgYAxisScale.setEnum(dgr.getYAxis().getScale());
	}

	public void assignConfig() {
		final PipeVisualizationDialog dlg = part.getVisualizationDialog();
		if (dlg == null) return;
		final Diagram dgr = dlg.getDiagram();

		cfgPosition.assignContent(dlg);

		dgr.getXAxis().setMin(cfgXAxisMin.getContent());
		dgr.getXAxis().setMax(cfgXAxisMax.getContent());
		dgr.getXAxis().setScale(cfgXAxisScale.getEnum());

		dgr.getYAxis().setMin(cfgYAxisMin.getContent());
		dgr.getYAxis().setMax(cfgYAxisMax.getContent());
		dgr.getYAxis().setScale(cfgYAxisScale.getEnum());
	}

}
