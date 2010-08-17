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

import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class BCISingleChannel extends Converter { // NO_UCD

	private final ConfigInt cfgChannelNr;

	public BCISingleChannel() {
		addConfig(cfgChannelNr = new ConfigInt("Channel-Number", 1, 1, 32));
		constructed();
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) {
			ds.setLabel(String.format("Channel %d", cfgChannelNr.getContent()));
			ds.setPen(getVisualizationDialog().getDiagram().detectPen(
					cfgChannelNr.getContent()));
		}
	}

	@Override
	public String getInputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("only #%d", cfgChannelNr.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		if (SingleFloatData.getUser(data) != cfgChannelNr.getContent())
			return emptyList();
		if (isVisualize()) plot(0, SingleFloatData.getValue(data));
		return asList(data);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Single Channel";
		case Description:
			return "Lets only data from a single channel pass and "
					+ "blocks all other data blocks";
		case Config1:
			return "Number of channel that is allowed to pass";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 1;
	}

}
