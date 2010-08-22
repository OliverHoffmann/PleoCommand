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

package pleocmd.itfc.cli;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import pleocmd.Log;
import pleocmd.Log.Type;
import pleocmd.cfg.ConfigValue;
import pleocmd.cfg.Configuration;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.MainFrame;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePart;

public final class CommandLine {

	private static CommandLine commandLine;

	private boolean needGUI;

	private Pipe pipe;

	private CommandLine() {
		commandLine = this;
	}

	public static CommandLine the() {
		if (commandLine == null) new CommandLine();
		return commandLine;
	}

	private Pipe getPipe() {
		if (needGUI) return MainFrame.the().getPipe();
		if (pipe == null) pipe = new Pipe(Configuration.getMain());
		return pipe;
	}

	private void startPipe() {
		if (needGUI)
			MainFrame.the().startPipeThread();
		else
			try {
				getPipe().configure();
				getPipe().pipeAllData();
			} catch (final Throwable t) { // CS_IGNORE
				Log.error(t);
			}
	}

	public void parse(final String[] args) {
		try {
			Log.setMinLogType(Type.Info);

			needGUI = false;
			for (final String arg : args)
				if (arg.equals("--gui") || arg.equals("-g")) {
					needGUI = true;
					break;
				}
			if (needGUI) MainFrame.the();
			Log.setGUIStatusKnown();

			boolean havePCA = false;
			for (final String arg : args) {
				final File file = new File(arg);
				if (file.isFile())
					if (file.getName().endsWith(".pca")) {
						havePCA = true;
						Configuration.getMain().readFromFile(file);
					} else
						throw new RuntimeException(String.format(
								"Cannot recognize filetype of '%s'", file));
				else
					parseArgument(arg);
			}
			if (havePCA) startPipe();
		} catch (final Throwable throwable) { // CS_IGNORE
			// we need to print the Exception additionally to
			// logging it because logging itself may have caused
			// the exception or it just is not yet initialized
			throwable.printStackTrace(); // CS_IGNORE
			Log.error(throwable, "Failed to parse command line arguments");
		}
	}

	private void parseArgument(final String arg) {
		if ("-d".equals(arg) || "--detailed".equals(arg) || "-v".equals(arg))
			Log.setMinLogType(Type.Detail);
		else if ("-h".equals(arg) || "-?".equals(arg) || "--help".equals(arg))
			displayHelp();
		else if ("-V".equals(arg) || "--version".equals(arg))
			printVersion();
		else if ("-g".equals(arg) || "--gui".equals(arg))
			displayGUI();
		else if (arg.startsWith("--configure="))
			configure(arg.substring(12));
		else if (arg.startsWith("--c="))
			configure(arg.substring(4));
		else
			throw new RuntimeException(String.format(
					"Cannot recognize argument '%s'", arg));
	}

	private void printVersion() {
		System.err.println(Version.VERSION_STR);
		System.err.println("Copyright (C) 2010 Oliver Hoffmann "
				+ "- Hoffmann_Oliver@gmx.de");
		System.err.println("Licensed under GNU General Public License "
				+ "version 2 or later");
	}

	private void displayHelp() {
		printVersion();
		System.err.println();
		System.err
				.println("-d --detailed\tPrint detailed (debug) information.");
		System.err.println("-h -? --help\tPrint this help message.");
		System.err.println("-V --version\tPrint version and copyright.");
		System.err.println("-g --gui\tDisplay a GUI and print error "
				+ "messages in dialogs.");
		System.err.println("<pca-file>\tLoad a previously saved "
				+ "Pipe and execute it.");
		System.err.println("\t\tPipe is executed after all other arguments "
				+ "have been processed, ");
		System.err.println("\t\tso additional PCA files "
				+ "on the command line replace the previous one ");
		System.err.println("\t\tand don't start a second Pipe.");
		System.err.println("-c --configure\tChange configuration of "
				+ "the Pipe.");
		System.err
				.println("\t\tMust be specified after the PCA file. Syntax is:");
		System.err.println("\t\t<Name of PipePart>:<Label>:<Value> OR");
		System.err.println("\t\t<UID of PipePart>:<Label>:<Value> ");
		System.err.println("\t\tIf the name of the PipePart is unknown or "
				+ "not unique, an error will be thrown.");
		System.err
				.println("\t\tIf the UID of the PipePart is invalid or unknown, "
						+ "an error will be thrown.");
		System.err.println("\t\tIf the label is unknown, "
				+ "an error will be thrown.");
		System.err.println("\t\tIf the value is invalid, "
				+ "an error will be thrown.");
		System.err.println();
		System.err.println("Examples:");
		System.err.println("Execute a Pipe within the GUI:");
		System.err.println("\tpleocommand MyPipe.pca --gui");
		System.err.println("Execute a Pipe on the console "
				+ "with additional output:");
		System.err.println("\tpleocommand MyPipe.pca --detailed");
		System.err.println("Execute a Pipe on the console but "
				+ "change configuration first:");
		System.err.println("\tpleocommand MyPipe.pca "
				+ "--c=TcpIpInput:Port:1234 "
				+ "--c=TcpIpInput:\"Read-Timeout (sec):60\"");
	}

	private void displayGUI() {
		assert needGUI;
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					MainFrame.the().showModalGUI();
				}
			});
		} catch (final InterruptedException e) {
			Log.error(e, "Cannot start GUI");
		} catch (final InvocationTargetException e) {
			Log.error(e, "Cannot start GUI");
		}
	}

	private void configure(final String cfg) {
		final int idx1 = cfg.indexOf(':');
		final int idx2 = cfg.indexOf(':', idx1 + 1);
		if (idx1 == -1 || idx2 == -1)
			throw new IllegalArgumentException(String.format(
					"Expected name or <UID>uid : label : value but found '%s'",
					cfg));
		final String part = cfg.substring(0, idx1).trim();
		final String label = cfg.substring(idx1 + 1, idx2).trim();
		final String value = cfg.substring(idx2 + 1).trim();

		long uid;
		try {
			uid = Long.parseLong(part);
		} catch (final NumberFormatException e) {
			uid = Long.MIN_VALUE;
		}
		final PipePart pp;
		if (uid != Long.MIN_VALUE) {
			pp = getPipe().findByUID(uid);
			if (pp == null)
				throw new IllegalArgumentException(String.format(
						"Cannot find PipePart with UID %d in '%s'", uid,
						getPipe()));
		} else {
			pp = getPipe().findByClassName(part);
			if (pp == null)
				throw new IllegalArgumentException(String.format(
						"Cannot find PipePart with name '%s' "
								+ "(or there is more than one) in '%s'", part,
						getPipe()));
		}
		final ConfigValue cv = pp.getGroup().get(label);
		if (cv == null)
			throw new IllegalArgumentException(String.format(
					"Cannot find a configuration with label '%s' in '%s'",
					label, pp));
		try {
			cv.setFromString(value);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(String.format(
					"Cannot set configuration '%s' in '%s' to '%s'", label, pp,
					value));
		}

	}
}
