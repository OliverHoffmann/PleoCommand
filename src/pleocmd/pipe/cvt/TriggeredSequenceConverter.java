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

import pleocmd.cfg.ConfigDataMap;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.pipe.data.CommandData;
import pleocmd.pipe.data.Data;

public final class TriggeredSequenceConverter extends Converter { // NO_UCD

	private final ConfigString cfgTriggerPrefix;

	private final ConfigDataMap cfgMap;

	public TriggeredSequenceConverter() {
		addConfig(cfgTriggerPrefix = new ConfigString("Trigger Prefix Word",
				"DO"));
		addConfig(cfgMap = new ConfigDataMap("Data-Sequences"));
		constructed();
	}

	@Override
	public String getInputDescription() {
		return cfgTriggerPrefix.getContent();
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("%s: %dx", cfgTriggerPrefix.getContent(), cfgMap
				.getAllKeys().size());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!CommandData.isCommandData(data, cfgTriggerPrefix.getContent()))
			return null;
		final String s = CommandData.getArgument(data);
		if (!cfgMap.hasContent(s))
			throw new ConverterException(this, false,
					"No entry for '%s' found", s);
		return ConfigDataMap.cloneDataList(cfgMap.getContent(s), data);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Triggered Sequence Converter";
		case Description:
			return "Converts a trigger command like 'DO|foo' into a sequence of "
					+ "commands based on a table lookup for 'foo' if the trigger "
					+ "prefix word is set to 'DO'";
		case Config1:
			return "The trigger prefix word - triggers without this word will "
					+ "be ignored";
		case Config2:
			return "The mapping from a trigger word to a sequence of commands";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		if (cfgTriggerPrefix.getContent().isEmpty())
			return "A trigger prefix word must be defined";
		if (cfgMap.getAllKeys().isEmpty())
			return "The map must contain at least one trigger word";
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return cfgMap.getAllKeys().size();
	}

}
