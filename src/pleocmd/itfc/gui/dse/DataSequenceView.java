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

	private final DataSequenceEditorPanel panel;

	public DataSequenceView(final Element elem,
			final DataSequenceEditorPanel panel) {
		super(elem);
		this.panel = panel;
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
			panel.updateErrorLabel(t.getMessage());
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