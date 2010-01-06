package pleocmd.cfg;

import java.awt.Rectangle;
import java.util.List;

import pleocmd.itfc.gui.Layouter;

public final class ConfigBounds extends ConfigValue {

	private Rectangle content;

	public ConfigBounds(final String label) {
		super(label);
		content = new Rectangle(-1, -1, -1, -1);
	}

	public Rectangle getContent() {
		return content;
	}

	public void setContent(final Rectangle content) {
		if (content == null) throw new NullPointerException();
		this.content = content;
	}

	@Override
	String asString() {
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
		// TODO GUI support
	}

	@Override
	public void setFromGUIComponents() {
		// TODO GUI support
	}

}
