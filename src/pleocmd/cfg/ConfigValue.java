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

package pleocmd.cfg;

import java.util.List;

import pleocmd.Log;
import pleocmd.RunnableWithArgument;
import pleocmd.cfg.ConfigCollection.Type;
import pleocmd.cfg.ConfigPath.PathType;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.pipe.PipePart;

/**
 * Base class for a value inside a {@link PipePart}'s configuration.
 * <p>
 * All of the sub classes must have a constructor which uses only a label and
 * sets all other fields to default values and should have a constructor which
 * takes an initial content as an additional parameter.<br>
 * The constructors must not throw anything but {@link RuntimeException}s (like
 * {@link NullPointerException}, {@link IllegalArgumentException} or
 * {@link IndexOutOfBoundsException}).
 * <p>
 * All of the sub classes have something like getContent() to retrieve the
 * current value and setContent() to set it to a new one.<br>
 * Implementations of setContent() may throw {@link ConfigurationException} if
 * the input is invalid.
 * <p>
 * If the {@link ConfigValue} is single-lined ({@link #isSingleLined()} returns
 * true) it must support {@link #asString()} and {@link #setFromString(String)},
 * and if it is multi-lined ( {@link #isSingleLined()} returns false) it must at
 * least support {@link #asString()}, {@link #asStrings()} and
 * {@link #setFromStrings(List)}, whereby "supporting" means not throwing an
 * {@link UnsupportedOperationException}.
 * 
 * @author oliver
 */
public abstract class ConfigValue {

	private final String label;

	private RunnableWithArgument changingContent;

	protected ConfigValue(final String label) {
		this.label = label;
	}

	public final String getLabel() {
		return label;
	}

	@Override
	public final String toString() {
		final String str = asString();
		return str == null ? getLabel() : getLabel() + ": " + str;
	}

	public abstract String asString();

	public abstract void setFromString(final String string)
			throws ConfigurationException;

	abstract List<String> asStrings();

	abstract void setFromStrings(final List<String> strings)
			throws ConfigurationException;

	public abstract String getIdentifier();

	abstract boolean isSingleLined();

	public abstract boolean insertGUIComponents(final Layouter lay);

	public abstract void setFromGUIComponents();

	public abstract void setGUIEnabled(final boolean enabled);

	public abstract Object getContent();

	public final void assign(final ConfigValue src)
			throws ConfigurationException {
		if (isSingleLined())
			setFromString(src.asString());
		else
			setFromStrings(src.asStrings());
	}

	protected final void checkValidString(final String str,
			final boolean allowLineFeed) throws ConfigurationException {
		final String trimmed = str.trim();
		if (!str.equals(trimmed) && !str.equals(trimmed + "\n"))
			throw new ConfigurationException("Cannot save this string "
					+ "in a configuration file - will be trimmed: '%s'", str);
		if ("{".equals(trimmed))
			throw new ConfigurationException("Cannot save this string "
					+ "in a configuration file - must not equal '{': '%s'", str);
		if (str.contains("\0"))
			throw new ConfigurationException("Cannot save this string "
					+ "in a configuration file - must not contain "
					+ "null-terminator: '%s'", str);
		if (!allowLineFeed && str.contains("\n"))
			throw new ConfigurationException("Cannot save this string "
					+ "in a configuration file - must not contain "
					+ "line-feed: '%s'", str);
		if (allowLineFeed
				&& (trimmed.contains("\n}\n") || trimmed.startsWith("}\n") || trimmed
						.endsWith("\n}")))
			throw new ConfigurationException(
					"Cannot save this string in a configuration file - "
							+ "no line must equal '}': '%s'", str);
	}

	// CS_IGNORE_NEXT NPath complexity - just a listing
	public static ConfigValue createValue(final String identifier,
			final String label, final boolean singleLined) {
		if ("int".equals(identifier)) return new ConfigInt(label);

		if ("long".equals(identifier)) return new ConfigLong(label);

		if ("double".equals(identifier)) return new ConfigDouble(label);

		if ("bool".equals(identifier)) return new ConfigBoolean(label);

		if ("dir".equals(identifier))
			return new ConfigPath(label, PathType.Directory);

		if ("read".equals(identifier))
			return new ConfigPath(label, PathType.FileForReading);

		if ("write".equals(identifier))
			return new ConfigPath(label, PathType.FileForWriting);

		if ("bounds".equals(identifier)) return new ConfigBounds(label);

		if ("color".equals(identifier)) return new ConfigColor(label);

		if ("datablock".equals(identifier)) return new ConfigDataBlock(label);

		if ("list".equals(identifier))
			return new ConfigCollection<String>(label, Type.List) {
				@Override
				protected String createItem(final String itemAsString)
						throws ConfigurationException {
					return itemAsString;
				}
			};

		if ("set".equals(identifier))
			return new ConfigCollection<String>(label, Type.Set) {
				@Override
				protected String createItem(final String itemAsString)
						throws ConfigurationException {
					return itemAsString;
				}
			};

		if (identifier != null)
			Log.warn("Ignoring unknown identifier '%s' and "
					+ "parsing value as simple string", identifier);
		return new ConfigString(label, !singleLined);
	}

	/**
	 * Sets a method which is invoked during every modification of the GUI's
	 * copy of the content of this {@link ConfigValue}, before the content has
	 * been applied to the {@link ConfigValue} itself by clicking "OK" in the
	 * GUI.<br>
	 * The method <b>must not</b> modify content of other {@link ConfigValue}s
	 * directly, but use methods like setContentGUI(...) instead, otherwise
	 * clicking "Cancel" can no longer reset the changes made while the GUI has
	 * been visible.<br>
	 * The method is always called with one argument (it's type depends on the
	 * {@link ConfigValue}'s subclass) for every GUI modification and once
	 * during GUI creation.<br>
	 * The return value may be one of:
	 * <ul>
	 * <li>null</li>
	 * <li>a String, {@link #setFromString(String)} will be called</li>
	 * <li>a List&lt;String&gt;, {@link #setFromStrings(List)} will be called</li>
	 * </ul>
	 * 
	 * @param changingContent
	 *            a {@link RunnableWithArgument} which is invoked on every GUI
	 *            driven change of the content.
	 */
	public final void setChangingContent(
			final RunnableWithArgument changingContent) {
		this.changingContent = changingContent;
	}

	@SuppressWarnings("unchecked")
	protected final void invokeChangingContent(final Object... args) {
		if (changingContent != null) {
			final Object res = changingContent.run(args);
			if (res instanceof String)
				try {
					setFromString((String) res);
				} catch (final ConfigurationException e) {
					Log.error(e);
				}
			else if (res instanceof List<?>) {
				final List<?> l = (List<?>) res;
				if (!l.isEmpty() && l.get(0) instanceof String) try {
					setFromStrings((List<String>) l);
				} catch (final ConfigurationException e) {
					Log.error(e);
				}
			} else if (res != null)
				Log.error("invokeChangingContent() can only handle null, "
						+ "a String or a List of Strings as return value");
		}
	}

}
