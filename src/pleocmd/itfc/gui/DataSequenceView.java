package pleocmd.itfc.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

public final class DataSequenceView extends PlainView {

	// FIXME if only part of line given to draw... pattern doesn't match
	// FIXME redundant parsing with AsciiDataConverter
	// FIXME doesn't detect erroneous input
	// TODO:
	// add "Syntax[] syntax" to AsciiDataConverter's params
	// if syntax != null, create Syntax as a side effect during parsing
	// cache Syntax[] for every line in DataSequenceView
	// update cache on document changes
	// Syntax: Color, Position, Length

	private final Pattern pattern = Pattern.compile( //
			"(^ *\\[[^]]*\\])?" // flags
					+ " *" // spaces
					+ "([IFSB]x?:)?" // type identifier
					+ " *(" // spaces
					+ "(-?[0-9]+)|" // integer
					+ "(-?[0-9.eE-]+)|" // float
					+ "([A-Fa-f0-9]+)|" // hex
					+ "([^|]+)" // string
					+ ") *" // spaces
					+ "(\\||$)" // delimiter
			);

	private final Color[] colors = new Color[] { // color for groups
			// from http://en.wikipedia.org/wiki/Web_colors
			new Color(255, 0, 0), // no matching possible - red
			new Color(250, 140, 0), // flags - dark orange
			new Color(138, 43, 226), // type identifier - blue violet
			null, // ignore this group (super group for value types)
			new Color(0, 100, 0), // integer - dark green
			new Color(46, 139, 87), // float - sea green
			new Color(47, 79, 79), // hex - dark slate gray
			new Color(0, 0, 0), // string - black
			new Color(70, 130, 180), // delimiter - steel blue
			// invalid (avoid OutOfBoundsException)
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED };

	public DataSequenceView(final Element elem) {
		super(elem);
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

		final String text = doc.getText(start, end - start);
		final Matcher matcher = pattern.matcher(text);

		int curX = xp;
		int curStart = start;
		final int cnt = matcher.groupCount();
		while (matcher.find())
			for (int i = 1; i <= cnt; ++i) {
				if (colors[i] == null) continue;
				final int matchStart = start + matcher.start(i);
				final int matchEnd = start + matcher.end(i);
				if (matchStart == -1 || matchEnd == matchStart) continue;
				// draw text between the matches
				if (curStart < matchStart) {
					g.setColor(colors[0]);
					doc.getText(curStart, matchStart - curStart, lb);
					curX = Utilities.drawTabbedText(lb, curX, yp, g, this,
							curStart);
				}
				// draw matched text
				g.setColor(colors[i]);
				doc.getText(matchStart, matchEnd - matchStart, lb);
				curX = Utilities.drawTabbedText(lb, curX, yp, g, this,
						matchStart);
				curStart = matchEnd;
			}
		// draw text behind the last match
		if (curStart < end) {
			g.setColor(colors[0]);
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
