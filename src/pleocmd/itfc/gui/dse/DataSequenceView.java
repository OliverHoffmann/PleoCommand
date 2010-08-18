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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Syntax;
import pleocmd.pipe.val.Syntax.Type;

final class DataSequenceView extends PlainView {

	private final UpdateErrorInterface uei;

	public DataSequenceView(final Element elem, final UpdateErrorInterface uei) {
		super(elem);
		this.uei = uei;
	}

	@Override
	protected int drawUnselectedText(final Graphics g, final int xp,
			final int yp, final int start, final int end)
			throws BadLocationException {
		final Document doc = getDocument();
		final Segment lb = getLineBuffer();

		if (g instanceof Graphics2D)
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

		final Element line = doc.getDefaultRootElement().getElement(
				doc.getDefaultRootElement().getElementIndex(start));
		final int lineStart = line.getStartOffset();
		final int lineEnd = line.getEndOffset();
		final String wholeLine = doc.getText(lineStart, lineEnd - lineStart);
		final List<Syntax> syntaxList = new ArrayList<Syntax>();
		try {
			Data.createFromAscii(wholeLine, syntaxList);
		} catch (final Throwable t) {
			uei.updateErrorLabel(t.getMessage());
		}

		int curX = xp;
		int curStart = start;
		Type type = Type.Error;
		for (final Syntax stx : syntaxList) {
			if (stx.getPosition() <= curStart - lineStart) {
				type = stx.getType();
				continue;
			}
			int nextStart = stx.getPosition() + lineStart;
			if (nextStart > end) nextStart = end;
			g.setColor(type.getColor());
			doc.getText(curStart, nextStart - curStart, lb);
			curX = Utilities.drawTabbedText(lb, curX, yp, g, this, curStart);

			curStart = nextStart;
			type = stx.getType();
		}
		if (curStart < end) {
			g.setColor(type.getColor());
			doc.getText(curStart, end - curStart, lb);
			curX = Utilities.drawTabbedText(lb, curX, yp, g, this, curStart);
		}
		return curX;
	}

	@Override
	protected int drawSelectedText(final Graphics g, final int xp,
			final int yp, final int start, final int end)
			throws BadLocationException {
		return drawUnselectedText(g, xp, yp, start, end);
	}

}
