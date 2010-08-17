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

package pleocmd.itfc.gui.help;

import java.net.URL;

import pleocmd.Log;

public final class HelpLoader {

	private HelpLoader() {
		// hidden - just static methods
	}

	/**
	 * Loads a help file and returns an URL to it.
	 * 
	 * @param name
	 *            name of the help file without any path and optionally without
	 *            extension it it is an HTML file
	 * @return an URL to the file or to a special "help-missing" file if the
	 *         specified name could not be found
	 */
	public static URL getHelp(final String name) {
		if (name == null) return getMissingHelp();
		final String fullName = name.contains(".") ? name : name + ".html";
		final URL url = HelpLoader.class.getResource(fullName);
		if (url == null) {
			Log.error("Cannot find help file: %s", fullName);
			return getMissingHelp();
		}
		return url;
	}

	/**
	 * Checks whether a help file is available (so that {@link #getHelp(String)}
	 * would return an URL other than {@link #getMissingHelp()}).
	 * 
	 * @param name
	 *            name of the help file without any path and optionally without
	 *            extension it it is an HTML file
	 * @return true if help file exists
	 */
	public static boolean isHelpAvailable(final String name) {
		if (name == null) return false;
		return HelpLoader.class.getResource(name.contains(".") ? name : name
				+ ".html") != null;
	}

	public static URL getMissingHelp() {
		final URL url = HelpLoader.class.getResource("help-missing.html");
		if (url == null)
			throw new RuntimeException("Could not find 'help-missing' file");
		return url;
	}

}
