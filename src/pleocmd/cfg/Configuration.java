package pleocmd.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import pleocmd.Log;
import pleocmd.cfg.ConfigPath.PathType;

public final class Configuration {

	private static File defaultConfigFile;
	static {
		defaultConfigFile = new File(System.getProperty("user.home")
				+ "/.pleocommand.cfg");
	}

	private static Configuration config;

	private final List<Group> groupsUnassigned;

	private final Map<String, ConfigurationInterface> groupsRegistered;

	private final Set<ConfigurationInterface> configObjects;

	private Configuration() {
		config = this;
		groupsUnassigned = new ArrayList<Group>();
		groupsRegistered = new HashMap<String, ConfigurationInterface>();
		configObjects = new HashSet<ConfigurationInterface>();
		try {
			readFromDefaultFile();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	public static Configuration the() {
		if (config == null) new Configuration();
		return config;
	}

	public static void setDefaultConfigFile(final File file)
			throws ConfigurationException {
		defaultConfigFile = file;
		if (config != null) config.writeToDefaultFile();
	}

	public static File getDefaultConfigFile() {
		return defaultConfigFile;
	}

	/**
	 * Registers an external object to this {@link Configuration}.<br>
	 * For every {@link Group} named in <i>groupNames</i> which appears during
	 * loading in the configuration file,
	 * {@link ConfigurationInterface#configurationChanged(Group)} will be
	 * called.<br>
	 * During saving, every {@link Group} listed in <i>groupNames</i> will be
	 * removed, before {@link ConfigurationInterface#configurationWriteback()}
	 * will be called, so only these {@link Group}s will be written to disk.
	 * 
	 * @param co
	 *            a configurable object
	 * @param groupNames
	 *            a {@link Set} of {@link Group} names which will be handled by
	 *            the configurable object if they exist in the
	 *            {@link Configuration}.
	 * @throws ConfigurationException
	 *             if reading or writing to default file fails
	 */
	public void registerConfigurableObject(final ConfigurationInterface co,
			final Set<String> groupNames) throws ConfigurationException {
		if (configObjects.contains(co))
			throw new IllegalStateException("Already registered");

		writeToDefaultFile();

		for (final String groupName : groupNames)
			groupsRegistered.put(groupName, co);
		configObjects.add(co);

		// only keep groups which are not registered by the new external object
		final List<Group> groupsKeep = new ArrayList<Group>(groupsUnassigned
				.size());
		for (final Group group : groupsUnassigned)
			if (!groupNames.contains(group.getName())) groupsKeep.add(group);
		groupsUnassigned.clear();
		groupsUnassigned.addAll(groupsKeep);

		readFromDefaultFile();
	}

	/**
	 * Registers an external object to this {@link Configuration}.<br>
	 * For every {@link Group} of name <i>groupName</i> which appears during
	 * loading in the configuration file,
	 * {@link ConfigurationInterface#configurationChanged(Group)} will be
	 * called.<br>
	 * During saving, every {@link Group} listed in <i>groupNames</i> will be
	 * removed, before {@link ConfigurationInterface#configurationWriteback()}
	 * will be called, so only these {@link Group}s will be written to disk.
	 * 
	 * @param co
	 *            a configurable object
	 * @param groupName
	 *            a {@link Group} name which will be handled by the configurable
	 *            object if it exists in the {@link Configuration}.
	 * @throws ConfigurationException
	 *             if reading or writing to default file fails
	 */
	public void registerConfigurableObject(final ConfigurationInterface co,
			final String groupName) throws ConfigurationException {
		final Set<String> set = new HashSet<String>(1);
		set.add(groupName);
		registerConfigurableObject(co, set);
	}

	/**
	 * Removes an external object from this {@link Configuration}.<br>
	 * All registered groups will be removed and
	 * {@link ConfigurationInterface#configurationWriteback()} will be called to
	 * write the last changes.
	 * 
	 * @param co
	 *            a configurable object
	 * @throws ConfigurationException
	 *             if writing back configuration changes to default file fails
	 */
	public void unregisterConfigurableObject(final ConfigurationInterface co)
			throws ConfigurationException {
		if (!configObjects.contains(co))
			throw new IllegalStateException("Not registered");

		writeToDefaultFile();

		final Iterator<Entry<String, ConfigurationInterface>> it = groupsRegistered
				.entrySet().iterator();
		while (it.hasNext())
			if (it.next().getValue() == co) it.remove();
		configObjects.remove(co);

		readFromDefaultFile();
	}

	public void readFromDefaultFile() throws ConfigurationException {
		readFromFile(defaultConfigFile);
	}

	public void readFromFile(final File file) throws ConfigurationException {
		try {
			final BufferedReader in = new BufferedReader(new FileReader(file));
			readFromReader(in);
			in.close();
		} catch (final IOException e) {
			throw new ConfigurationException(e, "Cannot read from '%s'", file);
		}
	}

	public void readFromReader(final BufferedReader in)
			throws ConfigurationException, IOException {
		Log.detail("Reading configuration");

		for (final ConfigurationInterface co : configObjects)
			co.configurationAboutToBeChanged();
		groupsUnassigned.clear();

		final int[] nr = new int[1];
		String line;
		// read lines before the first group
		while ((line = in.readLine()) != null) {
			++nr[0];
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			break;
		}
		// read all the groups
		while (true) {
			if (line == null) break;
			if (line.charAt(0) != '[' || line.charAt(line.length() - 1) != ']')
				throw new ConfigurationException(nr[0], line,
						"Expected a group name between '[' and ']'");
			line = readGroup(in, nr, line.substring(1, line.length() - 1)
					.trim());
		}
		Log.detail("Done reading %d configuration line(s)", nr[0]);
	}

	private String readGroup(final BufferedReader in, final int[] nr,
			final String groupName) throws ConfigurationException, IOException {
		Log.detail("Reading config group '%s' at line %d", groupName, nr[0]);

		Group group = null;
		final ConfigurationInterface co = groupsRegistered.get(groupName);
		if (co != null) group = co.getSkeleton(groupName);
		final boolean hasSkeleton = group != null;
		if (group == null)
			group = new Group(groupName);
		else if (!group.getName().equals(groupName))
			throw new ConfigurationException(nr[0], "???",
					"Skeleton Group-Name '%s' mismatches current "
							+ "Group-Name '%s'", group.getName(), groupName);

		String line;
		while ((line = in.readLine()) != null) {
			++nr[0];
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			if (line.charAt(0) == '[') break; // done with this group
			final int colIdx = line.indexOf(':');
			if (colIdx == -1)
				throw new ConfigurationException(nr[0], line,
						"Expected a label name followed by ':' or "
								+ "a group name between '[' and ']'");
			String label = line.substring(0, colIdx).trim();
			final String content = line.substring(colIdx + 1).trim();
			String type = null;
			if (label.isEmpty())
				throw new ConfigurationException(nr[0], line,
						"Expected a non-empty label name followed by ':'");
			if (label.charAt(label.length() - 1) == '>') {
				final int brkIdx = label.indexOf('<');
				if (brkIdx == -1)
					throw new ConfigurationException(nr[0], line,
							"Missing '<' for type in label name");
				type = label.substring(brkIdx + 1, label.length() - 1).trim();
				label = label.substring(0, brkIdx).trim();
			}
			Log.detail("Parsed label '%s', type '%s', content '%s'", label,
					type, content);
			ConfigValue value = null;
			final boolean singleLined = !"{".equals(content);
			if (hasSkeleton) {
				value = group.get(label);
				if (value == null)
					Log.warn("Ignoring value with unknown label '%s' "
							+ "for group '%s'", label, groupName);
			}
			if (value == null)
				if ("int".equals(type))
					value = new ConfigInt(label, 0, Integer.MIN_VALUE,
							Integer.MAX_VALUE);
				else if ("float".equals(type))
					value = new ConfigFloat(label, .0, Double.MIN_VALUE,
							Double.MAX_VALUE);
				else if ("dir".equals(type))
					value = new ConfigPath(label, PathType.Directory);
				else if ("read".equals(type))
					value = new ConfigPath(label, PathType.FileForReading);
				else if ("write".equals(type))
					value = new ConfigPath(label, PathType.FileForWriting);
				else
					// "str", "item", null
					value = new ConfigString(label, !singleLined);
			if (singleLined)
				value.setFromString(content);
			else
				value.setFromStrings(readList(in, nr));
			group.set(value);
		}

		if (co == null)
			groupsUnassigned.add(group);
		else
			co.configurationChanged(group);
		Log.detail("Done reading config group '%s' at line %d "
				+ "registered by '%s'", group, nr[0], co);
		return line;
	}

	private List<String> readList(final BufferedReader in, final int[] nr)
			throws IOException, ConfigurationException {
		final List<String> items = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			++nr[0];
			line = line.trim();
			if (!line.isEmpty() && line.charAt(0) == '#') continue;
			if ("}".equals(line)) return items;
			items.add(line);
		}
		++nr[0];
		throw new ConfigurationException(nr[0], line,
				"Missing line with '}' for end of value list");
	}

	public void writeToDefaultFile() throws ConfigurationException {
		writeToFile(defaultConfigFile);
	}

	public void writeToFile(final File file) throws ConfigurationException {
		try {
			final FileWriter out = new FileWriter(file);
			writeToWriter(out);
			out.close();
		} catch (final IOException e) {
			throw new ConfigurationException(e, "Cannot write to '%s'", file);
		}
	}

	public void writeToWriter(final Writer out) throws IOException {
		Log.detail("Writing configuration");

		for (final ConfigurationInterface co : configObjects)
			try {
				final List<Group> list = co.configurationWriteback();
				Log.detail("Got groups by '%s': %s", co, list);
				for (final Group group : list)
					writeGroup(out, group);
			} catch (final ConfigurationException e) {
				Log.error(e, "Part of configuration could not be saved:");
			}

		Log.detail("Unassigned groups which will be " + "kept: %s",
				groupsUnassigned);
		for (final Group group : groupsUnassigned)
			writeGroup(out, group);

		Log.detail("Done writing configuration");

	}

	private void writeGroup(final Writer out, final Group group)
			throws IOException {
		Log.detail("Writing group '%s'", group);

		out.write('[');
		out.write(group.getName());
		out.write(']');
		out.write('\n');

		for (final ConfigValue value : group.getValueMap().values()) {
			out.write(value.getLabel());
			final String id = value.getIdentifier();
			if (id != null) {
				out.write('<');
				out.write(id);
				out.write('>');
			}
			out.write(": ");
			if (value.isSingleLined()) {
				out.write(value.asString());
				out.write('\n');
			} else {
				out.write('{');
				out.write('\n');
				for (final String line : value.asStrings()) {
					out.write('\t');
					out.write(line);
					out.write('\n');
				}
				out.write('}');
				out.write('\n');
			}
		}
		out.write('\n');
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Configuration");
		sb.append(" Registered configurable objects: ");
		sb.append(configObjects.toString());
		sb.append(" Registered groups: ");
		sb.append(groupsRegistered.toString());
		sb.append(" Unassigned groups: ");
		sb.append(groupsUnassigned.toString());
		return sb.toString();
	}

	public static List<Group> asList(final Group group) {
		final List<Group> res = new ArrayList<Group>(1);
		res.add(group);
		return res;
	}

}
