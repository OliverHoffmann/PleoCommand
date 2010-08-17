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

import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.exc.FormatException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleBoolData;

public final class SingleCommandConverter extends Converter { // NO_UCD

	private final ConfigString cfgCommand;

	public SingleCommandConverter() {
		addConfig(cfgCommand = new ConfigString("Command",
				"PMC|MOTION PLAY foo"));
		constructed();
	}

	@Override
	public String getInputDescription() {
		return SingleBoolData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgCommand.getContent();
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleBoolData.isSingleBoolData(data)) return null;
		if (!SingleBoolData.getValue(data)) return emptyList();
		try {
			return asList(new Data(Data
					.createFromAscii(cfgCommand.getContent()), data));
		} catch (final IOException e) {
			throw new ConverterException(this, true, e, "Invalid command");
		} catch (final FormatException e) {
			throw new ConverterException(this, true, e, "Invalid command");
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Single Command";
		case Description:
			return "Sends a command if the input-value is 'true'";
		case Config1:
			return "The command that should be send";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return cfgCommand.getContent().isEmpty() ? "No command specified"
				: null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

}
