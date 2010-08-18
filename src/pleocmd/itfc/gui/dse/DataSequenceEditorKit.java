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

package pleocmd.itfc.gui.dse;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

final class DataSequenceEditorKit extends StyledEditorKit implements
		ViewFactory {

	private static final long serialVersionUID = -7824068672374046824L;

	private final UpdateErrorInterface uei;

	public DataSequenceEditorKit(final UpdateErrorInterface uei) {
		this.uei = uei;
	}

	@Override
	public ViewFactory getViewFactory() {
		return this;
	}

	@Override
	public View create(final Element elem) {
		return new DataSequenceView(elem, uei);
	}

	@Override
	public String getContentType() {
		return "text/datasequence";
	}

}
