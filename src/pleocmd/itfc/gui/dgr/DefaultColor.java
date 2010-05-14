package pleocmd.itfc.gui.dgr;

import java.awt.Color;

final class DefaultColor extends Color {

	private static final long serialVersionUID = -3241104615913127514L;

	private static final double MAKE_DARKER = 1.834;

	private final String name;

	public DefaultColor(final int r, final int g, final int b, final String name) {
		super(r, g, b);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Color makeDarker() {
		return new Color((int) (getRed() / MAKE_DARKER),
				(int) (getGreen() / MAKE_DARKER),
				(int) (getBlue() / MAKE_DARKER));
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
