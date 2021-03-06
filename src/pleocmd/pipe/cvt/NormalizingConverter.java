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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import pleocmd.Log;
import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class NormalizingConverter extends Converter { // NO_UCD

	private final ConfigInt cfgTimeFrameLength;

	// CS_IGNORE_BEGIN class meant as struct

	public static class Params {
		public double sum; // sum of all x_i, where i = 0..m_timeFrame
		public int valPos; // position in ring buffer m_val
		public double[] values; // ring buffer for m_sum
		public boolean feeded; // first round or enough data?
	}

	// CS_IGNORE_END

	private final Map<Integer, Params> map = new Hashtable<Integer, Params>();

	public NormalizingConverter() {
		addConfig(cfgTimeFrameLength = new ConfigInt("Length of Time-Frame",
				100, 1, 1024 * 1024));
		constructed();
	}

	@Override
	protected void init0() {
		map.clear();
	}

	@Override
	protected void close0() {
		map.clear(); // make garbage collector happy
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null)
			ds.setLabel(String.format("Normalized Data [%d]",
					cfgTimeFrameLength.getContent()));
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
		return String.format("...%d...", cfgTimeFrameLength.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		double val = SingleFloatData.getValue(data);
		Params p = map.get(SingleFloatData.getUser(data));
		if (p == null) {
			p = new Params();
			p.values = new double[cfgTimeFrameLength.getContent()];
			map.put((int) SingleFloatData.getUser(data), p);
		}
		if (p.feeded) p.sum -= p.values[p.valPos];
		p.sum += val;
		p.values[p.valPos] = val;
		p.valPos = (p.valPos + 1) % p.values.length;
		if (p.valPos == 0) p.feeded = true; // at least one loop now

		if (Double.isInfinite(p.sum) || Double.isNaN(p.sum)) {
			Log.warn("Average sum exceeded range of "
					+ "Double data type => Resetting");
			p.sum = .0;
			p.valPos = 0;
			p.feeded = false;
		}

		if (!p.feeded) return emptyList();

		val -= p.sum / p.values.length;
		if (isVisualize()) plot(0, val);
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Normalizing Converter";
		case Description:
			return "Normalizes the Data to the baseline by reducing it "
					+ "by the average over the most recent Data blocks";
		case Config1:
			return "Number of Data blocks to use for average calculation";
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
