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

package pleocmd.itfc.gui.log;

import java.awt.Color;

final class LogTableStyledCell {

	private final String text;

	private final Color foreground;

	private final Color background;

	private final boolean bold;

	private final boolean italic;

	private final boolean multiLine;

	public LogTableStyledCell(final String text, final boolean multiLine,
			final Color foreground, final Color background, final boolean bold,
			final boolean italic) {
		this.text = text == null ? "" : text;
		this.multiLine = multiLine;
		this.foreground = foreground;
		this.background = background;
		this.bold = bold;
		this.italic = italic;
	}

	public String getText() {
		return text;
	}

	public boolean isMultiLine() {
		return multiLine;
	}

	public Color getForeground() {
		return foreground;
	}

	public Color getBackground() {
		return background;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

}
