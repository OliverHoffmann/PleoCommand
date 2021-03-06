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
import java.io.File;
import java.io.FileInputStream;
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
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.dse.DataFileBinaryDialog;
import pleocmd.itfc.gui.dse.DataFileEditDialog;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.out.FileOutput;
import pleocmd.pipe.out.Output;

public final class FileInput extends Input { // NO_UCD

	private final ConfigPath cfgFile;

	private final ConfigEnum<ReadType> cfgType;

	private DataInputStream in;

	public FileInput() {
		addConfig(cfgFile = new ConfigPath("File", PathType.FileForReading));
		addConfig(cfgType = new ConfigEnum<ReadType>(ReadType.class));
		cfgFile.setFileFilter(Arrays.asList(new FileFilter[] {
				new FileNameExtensionFilter("ASCII-Textfiles", "txt"),
				new FileNameExtensionFilter("Pleo ASCII Data", "pad"),
				new FileNameExtensionFilter("Pleo Binary Data", "pbd") }));
		cfgFile.setModifyFile(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final String fileName = (String) args[0];
				if (fileName.endsWith(".pbd"))
					new DataFileBinaryDialog(new File(fileName));
				else
					new DataFileEditDialog(new File(fileName));
				return null;
			}

		});
		cfgFile.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final String path = (String) args[0];
				switch (getCfgType().getEnumGUI()) {
				case Ascii:
					if (path.endsWith(".pbd"))
						getCfgType().setEnumGUI(ReadType.Binary);
					break;
				case Binary:
					if (!path.endsWith(".pbd"))
						getCfgType().setEnumGUI(ReadType.Ascii);
					break;
				}
				return null;
			}
		});
		cfgType.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final String path = getCfgFile().getContentGUI().getPath();
				switch (ReadType.valueOf((String) args[0])) {
				case Ascii:
					if (path.endsWith(".pbd")) getCfgFile().clearContentGUI();
					break;
				case Binary:
					if (!path.endsWith(".pbd")) getCfgFile().clearContentGUI();
					break;
				}
				return null;
			}
		});
		constructed();
	}

	public FileInput(final File file, final ReadType type)
			throws ConfigurationException {
		this();
		cfgFile.setContent(file);
		cfgType.setEnum(type);
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Opening file '%s' for input", cfgFile.getContent());
		in = new DataInputStream(new FileInputStream(cfgFile.getContent()));
	}

	@Override
	protected void close0() throws IOException {
		Log.detail("Closing file '%s'", cfgFile.getContent());
		in.close();
		in = null;
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("\"%s\"", cfgFile.getContent().getName());
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (in.available() <= 0) {
			Log.info("End Of File in File-Input");
			return null;
		}
		switch (cfgType.getEnum()) {
		case Ascii:
			try {
				final Data data = Data.createFromAscii(in);
				if (Log.canLogDetail())
					Log.detail("<html>Read from file: %s",
							StringManip.printSyntaxHighlightedAscii(data));
				return data;
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from file");
			}
		case Binary:
			try {
				final Data data = Data.createFromBinary(in);
				if (Log.canLogDetail())
					Log.detail("<html>Read from file: %s",
							StringManip.printSyntaxHighlightedBinary(data));
				return data;
			} catch (final FormatException e) {
				throw new InputException(this, false, e,
						"Cannot read from file");
			}
		default:
			throw new InternalException(cfgType.getEnum());
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "File Input";
		case Description:
			return "Reads Data blocks from external files";
		case Config1:
			return "Path to an external file from which commands should "
					+ "be read in either ASCII or binary form";
		case Config2:
			return "'Ascii' if Data blocks are in ASCII format or\n"
					+ "   'Binary' if Data blocks should be treated as binary";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		final File file = cfgFile.getContent();

		if (isConnected())
			for (final Output out : getPipe().getOutputList())
				if (out instanceof FileOutput
						&& ((FileOutput) out).getCfgFile().getContent()
								.equals(file))
					return String
							.format("Same file has already been specified by '%s'",
									out);

		return file.canRead() ? null : String.format("Cannot read from '%s'",
				cfgFile.getContent());
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

	public ConfigPath getCfgFile() {
		return cfgFile;
	}

	protected ConfigEnum<ReadType> getCfgType() {
		return cfgType;
	}

}
