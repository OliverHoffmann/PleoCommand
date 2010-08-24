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

import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleBoolData;
import pleocmd.pipe.data.SingleFloatData;

public final class BoolConverter extends Converter { // NO_UCD

	private enum Comparator {
		Equals {
			@Override
			public String toString() {
				return "=";
			}
		},
		LessThan {
			@Override
			public String toString() {
				return "<=";
			}
		},
		BiggerThan {
			@Override
			public String toString() {
				return ">=";
			}
		}
	}

	private final ConfigEnum<Comparator> cfgComparator;

	private final ConfigDouble cfgConstant;

	private boolean lastRes;

	public BoolConverter() {
		addConfig(cfgComparator = new ConfigEnum<Comparator>(Comparator.class));
		addConfig(cfgConstant = new ConfigDouble("Constant", 0));
		constructed();
	}

	@Override
	protected void init0() throws ConverterException, IOException {
		lastRes = false;
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel(getShortConfigDescr0());
	}

	@Override
	public String getInputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleBoolData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("%s %s?", cfgComparator.asString(),
				cfgConstant.asString());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		final double val = SingleFloatData.getValue(data);
		final boolean res;
		switch (cfgComparator.getEnum()) {
		case Equals:
			res = Math.abs(val - cfgConstant.getContent()) <= Double.MIN_NORMAL;
			break;
		case LessThan:
			res = val <= cfgConstant.getContent() + Double.MIN_NORMAL;
			break;
		case BiggerThan:
			res = val >= cfgConstant.getContent() - Double.MIN_NORMAL;
			break;
		default:
			throw new ConverterException(this, true,
					"Invalid comparator: '%s'", cfgComparator.getContent());
		}
		final boolean changed = res && !lastRes;
		lastRes = res;
		if (isVisualize()) plot(0, changed ? 1 : 0);
		return asList(SingleBoolData.create(changed, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Bool Converter";
		case Description:
			return "Creates a boolean value from a float (double) value";
		case Config1:
			return "Type of comparison";
		case Config2:
			return "Constant to compare each value with";
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
