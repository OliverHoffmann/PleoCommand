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

import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.MultiFloatData;
import pleocmd.pipe.data.SingleFloatData;

public final class DataBlockSplitter extends Converter { // NO_UCD

	private static final int MAX_VIS = 8;

	private final ConfigInt cfgChannelNr;

	private final ConfigInt cfgDelay;

	public DataBlockSplitter() {
		addConfig(cfgChannelNr = new ConfigInt("Channel-Number", 1, 1, 32));
		addConfig(cfgDelay = new ConfigInt("Delay (in ms)", 0, 0,
				Integer.MAX_VALUE));
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
		return MultiFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		final String res = String
				.format("split #%d", cfgChannelNr.getContent());
		final long time = cfgDelay.getContent();
		return time == 0 ? res : String.format("%s %dms", res, time);
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!MultiFloatData.isMultiFloatData(data)) return null;
		final int cnt = MultiFloatData.getValueCount(data);
		final List<Data> res = new ArrayList<Data>(cnt);
		long time = data.getTime();
		if (time == Data.TIME_NOTIME && cfgDelay.getContent() != 0)
			time = getPipe().getFeedback().getElapsed();
		for (int i = 0; i < cnt; ++i) {
			final double val = MultiFloatData.getValue(data, i);
			res.add(new SingleFloatData(val, cfgChannelNr.getContent(), data,
					Data.PRIO_DEFAULT, time));
			if (isVisualize() && i <= MAX_VIS) plot(i, val);
			time += cfgDelay.getContent();
		}
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Data Block Splitter";
		case Description:
			return "Splits a multi-value data block into its "
					+ "single values, like 'Multi|2.5|7|0.01' to "
					+ "'Single|2.5|1', 'Single|7|1' and 'Single|0.01|1'";
		case Config1:
			return "The Channel-Number which should be used for the "
					+ "newly created data blocks";
		case Config2:
			return "The delay between consecutive data blocks coming from the "
					+ "same multi-value block (in ms)";
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
		return MAX_VIS;
	}

}
