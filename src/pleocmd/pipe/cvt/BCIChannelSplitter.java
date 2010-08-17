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

import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.MultiFloatData;
import pleocmd.pipe.data.SingleFloatData;

public final class BCIChannelSplitter extends Converter { // NO_UCD

	private static final int MAX_VIS = 8;

	public BCIChannelSplitter() {
		constructed();
	}

	@Override
	protected void initVisualize0() {
		for (int i = 0; i < MAX_VIS; ++i) {
			final DiagramDataSet ds = getVisualizeDataSet(i);
			if (ds != null)
				ds.setLabel(String.format("BCI Channel %d", i + 1));
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
		return getName();
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!MultiFloatData.isMultiFloatData(data)) return null;
		final int cnt = MultiFloatData.getValueCount(data);
		final List<Data> res = new ArrayList<Data>(cnt);
		for (int i = 0; i < cnt; ++i) {
			final double val = MultiFloatData.getValue(data, i);
			res.add(new SingleFloatData(val, i + 1, data));
			if (isVisualize() && i <= MAX_VIS) plot(i, val);
		}
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Channel Splitter";
		case Description:
			return "Splits a multi-channel data block into its "
					+ "single channels, like 'Multi|2.5|7|0.01' to "
					+ "'Single|2.5|1', 'Single|7|2' and 'Single|0.01|3'";
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
