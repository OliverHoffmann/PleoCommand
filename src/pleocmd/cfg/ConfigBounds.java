package pleocmd.cfg;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JLabel;

import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter;

public final class ConfigBounds extends ConfigValue {

	private Rectangle content;

	public ConfigBounds(final String label) {
		super(label);
		clearContent();
	}

	public ConfigBounds(final String label, final Rectangle content) {
		super(label);
		setContent(content);
	}

	public Rectangle getContent() {
		return content;
	}

	public void setContent(final Rectangle content) {
		if (content == null) throw new NullPointerException();
		this.content = content;
	}

	public void assignContent(final Component comp) {
		final Rectangle r = content;
		if (r.x == -1 && r.y == -1 && r.width == -1 && r.height == -1) {
			// don't set, instead use the current one
			content = comp.getBounds();
			return;
		}
		if (r.x == -1 && r.y == -1) {
			// center on screen
			final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			r.x = (dim.width - r.width) / 2;
			r.y = (dim.height - r.height) / 2;
		}
		comp.setBounds(r);
	}

	public void clearContent() {
		setContent(new Rectangle(-1, -1, -1, -1));
	}

	@Override
	public String asString() {
		return String.format("%d, %d / %d x %d", (int) content.getX(),
				(int) content.getY(), (int) content.getWidth(), (int) content
						.getHeight());
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		try {
			final int index1 = string.indexOf(',');
			final int index2 = string.indexOf('/');
			final int index3 = string.indexOf('x');
			if (index1 == -1 || index2 < index1 || index3 < index2)
				throw new ConfigurationException("Invalid format: '%s'", string);
			setContent(new Rectangle(Integer.valueOf(string
					.substring(0, index1).trim()), Integer.valueOf(string
					.substring(index1 + 1, index2).trim()), Integer
					.valueOf(string.substring(index2 + 1, index3).trim()),
					Integer.valueOf(string.substring(index3 + 1).trim())));
		} catch (final NumberFormatException e) {
			throw new ConfigurationException("Invalid number in '%s'", string);
		}
	}

	@Override
	List<String> asStrings() {
		throw new UnsupportedOperationException();
	}

	@Override
	void setFromStrings(final List<String> strings) {
		throw new UnsupportedOperationException();
	}

	@Override
	String getIdentifier() {
		return "bounds";
	}

	@Override
	boolean isSingleLined() {
		return true;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		lay.add(new JLabel(asString()), true);
	}

	@Override
	public void setFromGUIComponents() {
		// GUI support is read-only
	}

}
