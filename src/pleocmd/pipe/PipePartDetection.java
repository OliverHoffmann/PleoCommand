package pleocmd.pipe;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import pleocmd.Log;

public final class PipePartDetection {

	private PipePartDetection() {
		// utility class => hidden
	}

	public static <E extends PipePart> List<Class<E>> getAllPipeParts(
			final String subPackage) {
		final List<Class<E>> res = new ArrayList<Class<E>>();

		// Get the full name of the package
		final String pkg = PipePartDetection.class.getPackage().getName() + "."
				+ subPackage;

		// Get all paths which may contain classes for this package
		Enumeration<URL> resources;
		try {
			resources = Thread.currentThread().getContextClassLoader()
					.getResources(pkg.replace(".", "/"));
		} catch (final IOException e) {
			Log.error(e, "Cannot list resource-paths of package '%s'", pkg);
			return res;
		}

		while (resources.hasMoreElements()) {
			String path;
			try {
				path = URLDecoder.decode(resources.nextElement().getFile(),
						"UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new InternalError("UTF-8 encoding not supported");
			}
			final File dir = new File(path);
			if (dir.isDirectory())
				addFromDirectory(res, pkg, dir);
			else if (path.startsWith("file:") && path.contains("!"))
				addFromArchive(res, pkg, path);
			else
				throw new RuntimeException(String.format(
						"Don't know how to list the contents of the "
								+ "resource-path '%s'", path));
		}
		return res;
	}

	private static <E extends PipePart> void addFromDirectory(
			final List<Class<E>> list, final String pkg, final File dir) {
		final File[] files = dir.listFiles();
		if (files == null)
			Log.error("Cannot list the contents of "
					+ "resource-directory '%s'", dir);
		else
			for (final File file : files) {
				final Class<E> cls = loadClass(pkg, file.getName());
				if (cls != null) list.add(cls);
			}
	}

	private static <E extends PipePart> void addFromArchive(
			final List<Class<E>> list, final String pkg, final String path) {
		String jarPath = path.substring(5);
		final String jarPrefix = jarPath
				.substring(jarPath.lastIndexOf('!') + 2);
		jarPath = jarPath.substring(0, jarPath.lastIndexOf('!'));
		try {
			final JarFile jar = new JarFile(new File(jarPath));
			final Enumeration<JarEntry> content = jar.entries();

			while (content.hasMoreElements()) {
				final JarEntry entry = content.nextElement();
				final String name = entry.getName();
				if (name.startsWith(jarPrefix)) {
					final Class<E> cls = loadClass(pkg, name.substring(name
							.lastIndexOf('/') + 1));
					if (cls != null) list.add(cls);
				}
			}
			jar.close();
		} catch (final IOException e) {
			Log.error(e, "Cannot list classes in JAR-Archive '%s'", jarPath);
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends PipePart> Class<E> loadClass(final String pkg,
			final String fileName) {
		if (!fileName.endsWith(".class") || fileName.contains("$"))
			return null;

		try {
			final Class<E> cls = (Class<E>) Class.forName(pkg + '.'
					+ fileName.substring(0, fileName.length() - 6));
			if (!Modifier.isAbstract(cls.getModifiers())
					&& PipePart.class.isAssignableFrom(cls)) return cls;
		} catch (final LinkageError e) {
			Log.error(e);
		} catch (final ClassNotFoundException e) {
			Log.error(e);
		} catch (final ClassCastException e) {
			Log.error(e);
		}
		return null;
	}
}
