package pleocmd.itfc.gui.icons;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class IconLoader {

	private IconLoader() {
		// hidden - just static methods
	}

	public static Icon getIcon(final String name) {
		final URL url = IconLoader.class.getResource(name);
		if (url == null) return new ImageIcon();
		return new ImageIcon(url, name);
	}

}
