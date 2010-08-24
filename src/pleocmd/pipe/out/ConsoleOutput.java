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

package pleocmd.pipe.out;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pleocmd.Log;
import pleocmd.StringManip;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.InternalException;
import pleocmd.exc.OutputException;
import pleocmd.itfc.gui.MainFrame;
import pleocmd.pipe.data.Data;

public final class ConsoleOutput extends Output {

	private final ConfigEnum<PrintType> cfgType;

	private Data lastRoot;

	public ConsoleOutput() {
		addConfig(cfgType = new ConfigEnum<PrintType>(PrintType.class));
		constructed();
	}

	public ConsoleOutput(final PrintType type) {
		this();
		cfgType.setEnum(type);
	}

	@Override
	protected void close0() {
		lastRoot = null;
	}

	@Override
	public String getInputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgType.getContent();
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		Data root;
		switch (cfgType.getEnum()) {
		case Ascii:
			printAscii(data);
			break;
		case Binary: {
			printBinary(data);
			break;
		}
		case AsciiOriginal:
			if (lastRoot != (root = data.getRoot()))
				printAscii(lastRoot = root);
			break;
		case BinaryOriginal:
			if (lastRoot != (root = data.getRoot()))
				printBinary(lastRoot = root);
			break;
		case PleoMonitorCommands:
			if ("PMC".equals(data.getSafe(0).asString()))
				Log.consoleOut(data.get(1).asString());
			break;
		default:
			throw new InternalException(cfgType.getEnum());
		}
		return true;
	}

	private static void printBinary(final Data data) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.writeToBinary(new DataOutputStream(out));
		if (MainFrame.hasGUI())
			Log.consoleOut2(out.toString("ISO-8859-1"),
					StringManip.printSyntaxHighlightedBinary(data));
		else
			Log.consoleOut(out.toString("ISO-8859-1"));
	}

	private static void printAscii(final Data data) throws IOException {
		if (MainFrame.hasGUI())
			Log.consoleOut2(data.asString(),
					StringManip.printSyntaxHighlightedAscii(data));
		else
			Log.consoleOut(data.asString());
	}

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "Console Output";
		case Description:
			return "Writes Data blocks to the standard output";
		case Config1:
			return "'Ascii' if Data blocks will be in ASCII format or\n"
					+ "   'Binary' if Data blocks will be written as binary";
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
