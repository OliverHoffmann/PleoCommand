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

package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.StringManip;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.exc.InternalException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Syntax;

public final class ConsoleInput extends Input {

	private final ConfigEnum<ReadType> cfgType;

	public ConsoleInput() {
		addConfig(cfgType = new ConfigEnum<ReadType>(ReadType.Ascii));
		constructed();
	}

	public ConsoleInput(final ReadType type) {
		this();
		cfgType.setEnum(type);
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgType.getContent();
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (StandardInput.the().available() <= 0) {
			Log.info("End Of Stream in Console-Input");
			return null;
		}
		final List<Syntax> syntaxList = new ArrayList<Syntax>();
		switch (cfgType.getEnum()) {
		case Ascii:
			try {
				final Data data = Data.createFromAscii(new DataInputStream(
						StandardInput.the()), syntaxList);
				Log.consoleIn(StringManip.printSyntaxHighlightedAscii(data));
				return data;
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from console");
			}
		case Binary:
			try {
				final Data data = Data.createFromBinary(new DataInputStream(
						StandardInput.the()), syntaxList);
				Log.consoleIn(StringManip.printSyntaxHighlightedBinary(data));
				return data;
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from console");
			}
		default:
			throw new InternalException(cfgType.getEnum());
		}
	}

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "Console Input";
		case Description:
			return "Reads Data blocks from the standard input";
		case Config1:
			return "'Ascii' if Data blocks are in ASCII format or\n"
					+ "   'Binary' if Data blocks should be treated as binary";
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
		return 0;
	}

}
