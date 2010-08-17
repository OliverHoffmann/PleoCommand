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

import pleocmd.api.ExpressionParser;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.exc.ParserException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.MultiFloatData;
import pleocmd.pipe.data.SingleFloatData;

public final class ExpressionConverter extends Converter { // NO_UCD

	private final ConfigString cfgExpression;

	private ExpressionParser parser;

	public ExpressionConverter() {
		addConfig(cfgExpression = new ConfigString("Expression", ""));
		constructed();
	}

	@Override
	protected void init0() throws ConverterException {
		try {
			parser = new ExpressionParser(cfgExpression.getContent());
		} catch (final ParserException e) {
			throw new ConverterException(this, true, e,
					"Cannot parse expression");
		}
	}

	@Override
	protected void close0() {
		parser.free();
		parser = null;
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Codomain adapted");
	}

	@Override
	public String getInputDescription() {
		return "";
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("%s", cfgExpression.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		double[] channelData;
		if (SingleFloatData.isSingleFloatData(data))
			channelData = new double[] { SingleFloatData.getValue(data) };
		else if (MultiFloatData.isMultiFloatData(data)) {
			channelData = new double[MultiFloatData.getValueCount(data)];
			for (int i = 0; i < channelData.length; ++i)
				channelData[i] = MultiFloatData.getValue(data, i);
		} else
			return null;
		final double res = parser.execute(channelData);
		if (isVisualize()) plot(0, res);
		return asList(SingleFloatData.create(res, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Expression Converter";
		case Description:
			return "Calculates an output value based on an "
					+ "expression and all input values";
		case Config1:
			return "Expression to use for conversion";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		try {
			new ExpressionParser(cfgExpression.getContent()).free();
		} catch (final ParserException e) {
			return e.getMessage();
		}
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 1;
	}

}
