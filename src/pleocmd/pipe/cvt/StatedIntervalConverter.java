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

package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.cfg.ConfigDataBlock;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.itfc.gui.dgr.DiagramDataSet.DiagramType;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class StatedIntervalConverter extends Converter { // NO_UCD

	private final ConfigDataBlock cfgCommand1;
	private final ConfigDataBlock cfgCommand2;

	private double sum;
	private int nextCommand;

	public StatedIntervalConverter() {
		addConfig(cfgCommand1 = new ConfigDataBlock("Command 1",
				"PMC|MOTION PLAY foo"));
		addConfig(cfgCommand2 = new ConfigDataBlock(
				"Command 2 (alternating, optional)", ""));
		constructed();
	}

	@Override
	protected void init0() {
		sum = .0;
		nextCommand = 2;
	}

	@Override
	protected void initVisualize0() {
		DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Sum");
		ds = getVisualizeDataSet(0);
		if (ds != null) {
			ds.setLabel("Command sent");
			ds.setType(DiagramType.IntersectionDiagram);
		}
	}

	@Override
	public String getInputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgCommand2.getContent().isEmpty() ? cfgCommand1.getContent()
				: String.format("%s <-> %s", cfgCommand1.getContent(),
						cfgCommand2.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		sum += 1.0 / SingleFloatData.getValue(data);
		if (isVisualize()) plot(0, sum);
		if (sum < 1) return emptyList();
		sum = .0;
		final List<Data> res;
		switch (nextCommand) {
		case 1:
			res = asList(createCommand(cfgCommand1, data));
			break;
		case 2:
			res = asList(createCommand(cfgCommand2, data));
			break;
		default:
			throw new IllegalStateException("nextCommand is invalid");
		}
		if (isVisualize()) plot(1, nextCommand);
		nextCommand = cfgCommand2.getContent().isEmpty() ? 1 : 3 - nextCommand;
		return res;
	}

	private Data createCommand(final ConfigString cfg, final Data parent)
			throws ConverterException {
		try {
			return new Data(Data.createFromAscii(cfg.getContent()), parent);
		} catch (final IOException e) {
			throw new ConverterException(this, true, e, "Invalid %s",
					cfg.getLabel());
		} catch (final FormatException e) {
			throw new ConverterException(this, true, e, "Invalid %s",
					cfg.getLabel());
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Stated Interval Converter";
		case Description:
			return "Sends one or two commands at an interval specified by "
					+ "the source values, i.e. if input is [2, 2, 2, 2, 1, 1]"
					+ "output would be [-, C, -, C, C, C] where C is command "
					+ "and - marks a dropped data block";
		case Config1:
			return "First command to send (alternating with the second one)";
		case Config2:
			return "Second command to send (if empty, only first one will be used)";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return cfgCommand1.getContent().isEmpty() ? "No command 1 specified"
				: null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 2;
	}

}
