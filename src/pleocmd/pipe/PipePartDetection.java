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

package pleocmd.pipe;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.Icon;

import pleocmd.Log;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.help.HelpLoader;
import pleocmd.pipe.PipePart.HelpKind;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

public final class PipePartDetection {

	private static final List<Class<Input>> LIST_IN = getAllPipeParts("in");
	private static final List<Class<Converter>> LIST_CVT = getAllPipeParts("cvt");
	private static final List<Class<Output>> LIST_OUT = getAllPipeParts("out");
	private static final List<Class<? extends PipePart>> LIST_PIPEPART;

	static {
		LIST_PIPEPART = new ArrayList<Class<? extends PipePart>>();
		for (final Class<Input> ppc : LIST_IN)
			LIST_PIPEPART.add(ppc);
		for (final Class<Converter> ppc : LIST_CVT)
			LIST_PIPEPART.add(ppc);
		for (final Class<Output> ppc : LIST_OUT)
			LIST_PIPEPART.add(ppc);
		final Comparator<Class<? extends PipePart>> cmp = new Comparator<Class<? extends PipePart>>() {
			@Override
			public int compare(final Class<? extends PipePart> c1,
					final Class<? extends PipePart> c2) {
				return PipePart.getName(c1).compareTo(PipePart.getName(c2));
			}
		};
		Collections.sort(LIST_IN, cmp);
		Collections.sort(LIST_CVT, cmp);
		Collections.sort(LIST_OUT, cmp);
		Collections.sort(LIST_PIPEPART, cmp);
	}

	// CS_IGNORE_BEGIN need private before public here

	public static final List<Class<Input>> ALL_INPUT = Collections
			.unmodifiableList(LIST_IN);
	public static final List<Class<Converter>> ALL_CONVERTER = Collections
			.unmodifiableList(LIST_CVT);
	public static final List<Class<Output>> ALL_OUTPUT = Collections
			.unmodifiableList(LIST_OUT);
	public static final List<Class<? extends PipePart>> ALL_PIPEPART = Collections
			.unmodifiableList(LIST_PIPEPART);

	// CS_IGNORE_END

	private PipePartDetection() {
		// utility class => hidden
	}

	private static <E extends PipePart> List<Class<E>> getAllPipeParts(
			final String subPackage) {
		final List<Class<E>> res = new ArrayList<Class<E>>();

		// Get the full name of the package
		final String pkg = PipePartDetection.class.getPackage().getName() + "."
				+ subPackage;

		// Get all paths which may contain classes for this package
		Log.detail("Searching PipeParts of sub-package '%s' resolved to '%s'",
				subPackage, pkg);
		Enumeration<URL> resources;
		try {
			resources = Thread.currentThread().getContextClassLoader()
					.getResources(pkg.replace(".", "/"));
		} catch (final IOException e) {
			Log.error(e, "Cannot list resource-paths of package '%s'", pkg);
			return res;
		}

		// Check all files in this paths (CLASS files and JAR archives)
		while (resources.hasMoreElements()) {
			String path;
			try {
				path = URLDecoder.decode(resources.nextElement().getFile(),
						"UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new InternalException("UTF-8 encoding not supported");
			}
			Log.detail("Found resource path '%s'", path);
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
		Log.detail("Searching resulted in '%s'", res);
		return res;
	}

	private static <E extends PipePart> void addFromDirectory(
			final List<Class<E>> list, final String pkg, final File dir) {
		Log.detail("Looking in directory '%s'", dir);
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
		Log.detail("Looking in archive '%s'", path);
		String jarPath = path.substring(5);
		final String jarPrefix = jarPath
				.substring(jarPath.lastIndexOf('!') + 2);
		Log.detail("Using prefix '%s'", jarPrefix);
		jarPath = jarPath.substring(0, jarPath.lastIndexOf('!'));
		try {
			final JarFile jar = new JarFile(new File(jarPath));
			final Enumeration<JarEntry> content = jar.entries();
			Log.detail("Archive '%s' contains %d entries", jar.getName(),
					jar.size());

			while (content.hasMoreElements()) {
				final JarEntry entry = content.nextElement();
				final String name = entry.getName();
				if (name.startsWith(jarPrefix)) {
					final Class<E> cls = loadClass(pkg,
							name.substring(name.lastIndexOf('/') + 1));
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
		Log.detail("Loading class from '%s' in '%s'", fileName, pkg);
		if (!fileName.endsWith(".class")) return null;
		final String clsName = fileName.substring(0, fileName.length() - 6);
		try {
			final Class<E> cls = (Class<E>) Class.forName(pkg + '.' + clsName);
			if (!PipePart.class.isAssignableFrom(cls)) return null;
			if (Modifier.isAbstract(cls.getModifiers())) return null;
			final Method m = getHelp(cls);
			if (!Modifier.isStatic(m.getModifiers()))
				throw new NoSuchMethodException(String.format(
						"Method is not static: '%s'", m));
			if (!Modifier.isPublic(m.getModifiers()))
				throw new NoSuchMethodException(String.format(
						"Method is not public: '%s'", m));
			if (!m.getReturnType().equals(String.class))
				throw new NoSuchMethodException(String.format(
						"Method doesn't return String: '%s'", m));
			return cls;
		} catch (final Exception e) { // CS_IGNORE
			// we need to catch all here, because there are too many
			// this which may go wrong during class loading to handle each one
			Log.error(e, "Cannot load class '%s' in '%s'", clsName, pkg);
			return null;
		}
	}

	public static Method getHelp(final Class<? extends PipePart> cpp)
			throws NoSuchMethodException {
		try {
			return cpp.getMethod("help", HelpKind.class);
		} catch (final NoSuchMethodException e) {
			throw new NoSuchMethodException(String.format(
					"No 'public static String help(HelpKind)'" + " in '%s'",
					cpp));
		}
	}

	public static String callHelp(final Class<? extends PipePart> cpp,
			final HelpKind kind) {
		try {
			return (String) PipePartDetection.getHelp(cpp).invoke(null, kind);
		} catch (final IllegalArgumentException e) {
			Log.error(e);
		} catch (final IllegalAccessException e) {
			Log.error(e);
		} catch (final InvocationTargetException e) {
			Log.error(e);
		} catch (final NoSuchMethodException e) {
			Log.error(e);
		}
		return "";
	}

	public static void checkStaticValidity() {
		for (final Class<? extends PipePart> cpp : LIST_PIPEPART)
			checkStaticValidity(cpp);
	}

	public static void checkStaticValidity(final Class<? extends PipePart> cpp) {
		final List<String> res = new ArrayList<String>();

		checkString(cpp, HelpKind.Name, res);

		checkString(cpp, HelpKind.Description, res);

		final String helpFile = PipePart.getHelpFile(cpp);
		if (!HelpLoader.isHelpAvailable(helpFile))
			res.add(String.format("Help-file '%s' does not exist", helpFile));

		final Icon icon = PipePart.getIcon(cpp);
		if (icon == null)
			res.add("Icon does not exist or could not be loaded");

		final Icon image = PipePart.getConfigImage(cpp);
		if (image == null)
			res.add("Config-Image does not exist or could not be loaded");

		try {
			final PipePart pp = cpp.newInstance();
			final int ci1 = HelpKind.Config1.ordinal();
			final int ciL = Math.min(ci1 + pp.getGuiConfigs().size(),
					HelpKind.values().length);
			for (int i = ci1; i < ciL; ++i)
				checkString(cpp, HelpKind.values()[i], res);
			for (int i = ciL; i < ci1 + pp.getGuiConfigs().size(); ++i)
				res.add(String.format("No Config enum available for '%s'", pp
						.getGuiConfigs().get(i)));
			for (int i = ciL; i < HelpKind.values().length; ++i)
				if (callHelp(cpp, HelpKind.values()[i]) != null)
					res.add(String.format("Config enum not referenced by GUI "
							+ "defined: '%s'", HelpKind.values()[i]));
		} catch (final InstantiationException e) {
			res.add(e.toString());
		} catch (final IllegalAccessException e) {
			res.add(e.toString());
		}

		if (!res.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Failed static checks for PipePart \"");
			sb.append(PipePart.getName(cpp));
			sb.append("\" - ");
			sb.append(cpp.getName());
			sb.append(":");
			for (final String s : res) {
				sb.append("\n");
				sb.append(s);
			}
			Log.warn(sb.toString());
		}
	}

	private static void checkString(final Class<? extends PipePart> cpp,
			final HelpKind hk, final List<String> res) {
		final String s = callHelp(cpp, hk);
		if (s == null)
			res.add(String.format("callHelp() for '%s' is null", hk));
		else if (s.isEmpty())
			res.add(String.format("callHelp() for '%s' is empty", hk));
		else if (s.contains("?"))
			res.add(String.format("callHelp() for '%s' contains '?'", hk));
	}

}
