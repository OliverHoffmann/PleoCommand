package pleocmd.itfc.gui.icons;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class IconLoader {

	private IconLoader() {
		// hidden - just static methods
	}

	public static Icon getIcon(final String name) {
		if (name == null) return getMissingIcon();
		final URL url = IconLoader.class.getResource(name.contains(".") ? name
				: name + ".png");
		if (url == null) return getMissingIcon();
		return new ImageIcon(url, name);
	}

	public static Icon getMissingIcon() {
		final URL url = IconLoader.class.getResource("image-missing.png");
		if (url == null) return new ImageIcon();
		return new ImageIcon(url, "Missing an icon file in current classpath");
	}

}
