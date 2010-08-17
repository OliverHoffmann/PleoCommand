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

package pleocmd.itfc.gui.icons;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Handles loading of icons.<br>
 * All icons which should be accessible by this class must be in the same
 * package and should be PNG files.
 * 
 * @author oliver
 */
public final class IconLoader {

	private IconLoader() {
		// hidden - just static methods
	}

	/**
	 * Loads an icon and returns an instance to it.
	 * 
	 * @param name
	 *            name of the icon without any path and optionally without
	 *            extension it it is a PNG file
	 * @return instance to the icon or instance to a special "image-missing"
	 *         icon if the icon specified by name could not be found or loaded
	 */
	public static Icon getIcon(final String name) {
		if (name == null) return getMissingIcon();
		final URL url = IconLoader.class.getResource(name.contains(".") ? name
				: name + ".png");
		if (url == null) return getMissingIcon();
		return new ImageIcon(url, name);
	}

	/**
	 * Loads an icon and returns an instance to it.
	 * 
	 * @param name
	 *            name of the icon without any path and optionally without
	 *            extension it it is a PNG file
	 * @return instance to the icon or instance to a special "image-missing"
	 *         icon if the icon specified by name could not be found or loaded
	 */
	public static boolean isIconAvailable(final String name) {
		if (name == null) return false;
		return IconLoader.class.getResource(name.contains(".") ? name : name
				+ ".png") != null;
	}

	/**
	 * Loads a special icon and returns an instance to it.
	 * 
	 * @return instance to a special "image-missing" icon or an empty icon if
	 *         this icon could not be found or loaded
	 */
	public static Icon getMissingIcon() {
		final URL url = IconLoader.class.getResource("image-missing.png");
		if (url == null)
			throw new RuntimeException("Could not load 'image-missing' icon");
		return new ImageIcon(url, "Missing an icon file in current classpath");
	}

}
