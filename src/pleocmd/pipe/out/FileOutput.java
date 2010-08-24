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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pleocmd.Log;
import pleocmd.RunnableWithArgument;
import pleocmd.StringManip;
import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigPath;
import pleocmd.cfg.ConfigPath.PathType;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.exc.OutputException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.in.FileInput;
import pleocmd.pipe.in.Input;

public final class FileOutput extends Output { // NO_UCD

	private final ConfigPath cfgFile;

	private final ConfigEnum<PrintType> cfgType;

	private DataOutputStream out;

	private Data lastRoot;

	public FileOutput() {
		addConfig(cfgFile = new ConfigPath("File", PathType.FileForWriting));
		addConfig(cfgType = new ConfigEnum<PrintType>(PrintType.class));
		cfgFile.setFileFilter(Arrays.asList(new FileFilter[] {
				new FileNameExtensionFilter("ASCII-Textfiles", "txt"),
				new FileNameExtensionFilter("Pleo ASCII Data", "pad"),
				new FileNameExtensionFilter("Pleo Binary Data", "pbd") }));
		cfgFile.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final String path = (String) args[0];
				switch (getCfgType().getEnumGUI()) {
				case Ascii:
				case AsciiOriginal:
				case PleoMonitorCommands:
					if (path.endsWith(".pbd"))
						getCfgType().setEnumGUI(PrintType.Binary);
					break;
				case Binary:
				case BinaryOriginal:
					if (!path.endsWith(".pbd"))
						getCfgType().setEnumGUI(PrintType.Ascii);
					break;
				}
				return null;
			}
		});
		cfgType.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final String path = getCfgFile().getContentGUI().getPath();
				switch (PrintType.valueOf((String) args[0])) {
				case Ascii:
				case AsciiOriginal:
				case PleoMonitorCommands:
					if (path.endsWith(".pbd")) getCfgFile().clearContentGUI();
					break;
				case Binary:
				case BinaryOriginal:
					if (!path.endsWith(".pbd")) getCfgFile().clearContentGUI();
					break;
				}
				return null;
			}
		});
		constructed();
	}

	public FileOutput(final File file, final PrintType type)
			throws ConfigurationException {
		this();
		cfgFile.setContent(file);
		cfgType.setEnum(type);
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file '%s' for output of type '%s'",
				cfgFile.getContent(), cfgType.getEnum());
		out = new DataOutputStream(new FileOutputStream(cfgFile.getContent()));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", cfgFile.getContent());
		out.close();
		out = null;
		lastRoot = null;
	}

	@Override
	public String getInputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("\"%s\"", cfgFile.getContent().getName());
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		Data root;
		switch (cfgType.getEnum()) {
		case Ascii:
			data.writeToAscii(out, true);
			if (Log.canLogDetail())
				Log.detail("<html>Written to file: %s",
						StringManip.printSyntaxHighlightedAscii(data));
			break;
		case Binary:
			data.writeToBinary(out);
			if (Log.canLogDetail())
				Log.detail("<html>Written to file: %s",
						StringManip.printSyntaxHighlightedBinary(data));
			break;
		case AsciiOriginal:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				root.writeToAscii(out, true);
				if (Log.canLogDetail())
					Log.detail("<html>Written to file: %s",
							StringManip.printSyntaxHighlightedAscii(root));
			}
			break;
		case BinaryOriginal:
			if (lastRoot != (root = data.getRoot())) {
				lastRoot = root;
				root.writeToBinary(out);
				if (Log.canLogDetail())
					Log.detail("<html>Written to file: %s",
							StringManip.printSyntaxHighlightedBinary(root));
			}
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

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "File Output";
		case Description:
			return "Writes Data blocks to external files";
		case Config1:
			return "Path to an external file to which commands should "
					+ "be written in either ASCII or binary form";
		case Config2:
			return "'Ascii' if Data blocks will be in ASCII format or\n"
					+ "   'Binary' if Data blocks will be written as binary";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		final File file = cfgFile.getContent();

		if (isConnected())
			for (final Input in : getPipe().getInputList())
				if (in instanceof FileInput
						&& ((FileInput) in).getCfgFile().getContent()
								.equals(file))
					return String.format(
							"Same file has already been specified by '%s'", in);

		if (file.exists())
			return file.canWrite() ? null : String.format(
					"Cannot write to '%s'", file);
		return file.getParentFile() != null && file.getParentFile().canWrite() ? null
				: String.format("Cannot create a file in '%s'",
						file.getParentFile());
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

	public ConfigPath getCfgFile() {
		return cfgFile;
	}

	protected ConfigEnum<PrintType> getCfgType() {
		return cfgType;
	}

}
