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

package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

public final class Main {

	private Main() {
		// utility class => hidden
	}

	public static void main(final String[] args) {
		try {
			System.setProperty("java.library.path",
					System.getProperty("java.library.path") + ":.");
			System.setProperty("sun.awt.exception.handler",
					MainExceptionHandler.class.getName());

			if (args.length > 0)
				CommandLine.the().parse(args);
			else
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						MainFrame.the().showModalGUI();
					}
				});
		} catch (final Throwable t) { // CS_IGNORE catch all here
			t.printStackTrace(); // CS_IGNORE logging may not work here
			Log.error(t);
		}
	}

}
