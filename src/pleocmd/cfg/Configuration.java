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
import pleocmd.exc.ConfigurationException;

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

	public static void setDefaultConfigFile(final File file) {
		defaultConfigFile = file;
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

		// register the new object and assign group-names to it
		for (final String groupName : groupNames)
			groupsRegistered.put(groupName, co);
		configObjects.add(co);

		// load the external object from the currently unassigned groups
		// only keep groups which are not registered by the new external object
		final List<Group> groupsKeep = new ArrayList<Group>(groupsUnassigned
				.size());
		co.configurationAboutToBeChanged();
		for (final Group group : groupsUnassigned)
			if (groupNames.contains(group.getName())) {
				final Group skelGroup = co.getSkeleton(group.getName());
				if (skelGroup == null)
					// no skeleton? just feed co with the unassigned group
					co.configurationChanged(group);
				else {
					// we have to copy data from unassigned to skeleton group
					skelGroup.assign(group);
					co.configurationChanged(skelGroup);
				}
			} else
				groupsKeep.add(group);
		groupsUnassigned.clear();
		groupsUnassigned.addAll(groupsKeep);
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

		// write back configuration for all groups of the object
		final List<Group> groups = co.configurationWriteback();

		// put all groups of the object into the list of unassigned groups
		for (final Group group : groups)
			groupsUnassigned.add(group);

		// remove registration of this object
		final Iterator<Entry<String, ConfigurationInterface>> it = groupsRegistered
				.entrySet().iterator();
		while (it.hasNext())
			if (it.next().getValue() == co) it.remove();
		configObjects.remove(co);
	}

	public void readFromDefaultFile() throws ConfigurationException {
		readFromFile(defaultConfigFile);
	}

	public void readFromFile(final File file) throws ConfigurationException {
		readFromFile(file, null);
	}

	public void readFromFile(final File file,
			final ConfigurationInterface coOnly) throws ConfigurationException {
		try {
			final BufferedReader in = new BufferedReader(new FileReader(file));
			readFromReader(in, coOnly);
			in.close();
		} catch (final IOException e) {
			throw new ConfigurationException(e, "Cannot read from '%s'", file);
		}
	}

	public void readFromReader(final BufferedReader in,
			final ConfigurationInterface coOnly) throws ConfigurationException,
			IOException {
		Log.detail("Reading configuration");

		if (coOnly == null) {
			for (final ConfigurationInterface co : configObjects)
				co.configurationAboutToBeChanged();
			groupsUnassigned.clear();
		} else
			coOnly.configurationAboutToBeChanged();

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
					.trim(), coOnly);
		}
		Log.detail("Done reading %d configuration line(s)", nr[0]);
	}

	private String readGroup(final BufferedReader in, final int[] nr,
			final String groupName, final ConfigurationInterface coOnly)
			throws ConfigurationException, IOException {
		Log.detail("Reading config group '%s' at line %d", groupName, nr[0]);

		final ConfigurationInterface co = groupsRegistered.get(groupName);
		if (coOnly != null && co != coOnly) {
			Log.warn("Ignoring group '%s' because it doesn't belong to '%s'",
					groupName, coOnly);
			return fastSkipGroup(in, nr);
		}

		Group group = co == null ? null : co.getSkeleton(groupName);
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
			readValue(in, nr, group, hasSkeleton, line);
		}

		if (co == null) {
			assert coOnly == null;
			groupsUnassigned.add(group);
		} else
			co.configurationChanged(group);
		Log.detail("Done reading config group '%s' at line %d "
				+ "registered by '%s'", group, nr[0], co);
		return line;
	}

	private String fastSkipGroup(final BufferedReader in, final int[] nr)
			throws IOException {
		String line;
		while ((line = in.readLine()) != null) {
			++nr[0];
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			if (line.charAt(0) == '[') break; // done with this group
		}
		return line;
	}

	private void readValue(final BufferedReader in, final int[] nr,
			final Group group, final boolean hasSkeleton, final String line)
			throws ConfigurationException, IOException {
		final int colIdx = line.indexOf(':');
		if (colIdx == -1)
			throw new ConfigurationException(nr[0], line,
					"Expected a label name followed by ':' or "
							+ "a group name between '[' and ']'");
		String label = line.substring(0, colIdx).trim();
		final String content = line.substring(colIdx + 1).trim();
		String identifier = null;
		if (label.isEmpty())
			throw new ConfigurationException(nr[0], line,
					"Expected a non-empty label name followed by ':'");
		if (label.charAt(label.length() - 1) == '>') {
			final int brkIdx = label.indexOf('<');
			if (brkIdx == -1)
				throw new ConfigurationException(nr[0], line,
						"Missing '<' for type in label name");
			identifier = label.substring(brkIdx + 1, label.length() - 1).trim();
			label = label.substring(0, brkIdx).trim();
		}
		Log.detail("Parsed label '%s', identifier '%s', content '%s'", label,
				identifier, content);

		ConfigValue value = null;
		final boolean singleLined = !"{".equals(content);
		if (hasSkeleton) {
			value = group.get(label);
			if (value == null)
				Log.warn("Ignoring value with unknown label '%s' "
						+ "for group '%s'", label, group.getName());
		}
		if (value == null)
			value = ConfigValue.createValue(identifier, label, singleLined);
		if (singleLined)
			value.setFromString(content);
		else
			value.setFromStrings(readList(in, nr));
		group.set(value);
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
		writeToFile(file, null);
	}

	public void writeToFile(final File file, final ConfigurationInterface coOnly)
			throws ConfigurationException {
		try {
			final FileWriter out = new FileWriter(file);
			writeToWriter(out, coOnly);
			out.close();
		} catch (final IOException e) {
			throw new ConfigurationException(e, "Cannot write to '%s'", file);
		}
	}

	public void writeToWriter(final Writer out,
			final ConfigurationInterface coOnly) throws IOException {
		Log.detail("Writing configuration");
		if (coOnly == null)
			writeAll(out);
		else
			writeConfigurationInterface(out, coOnly);
		out.flush();
		Log.detail("Done writing configuration");
	}

	private void writeAll(final Writer out) throws IOException {
		for (final ConfigurationInterface co : configObjects)
			writeConfigurationInterface(out, co);

		Log.detail("Writing unassigned groups");
		for (final Group group : groupsUnassigned)
			writeGroup(out, group);
	}

	private void writeConfigurationInterface(final Writer out,
			final ConfigurationInterface co) throws IOException {
		try {
			final List<Group> list = co.configurationWriteback();
			Log.detail("Got groups by '%s': %s", co, list);
			for (final Group group : list)
				writeGroup(out, group);
		} catch (final ConfigurationException e) {
			Log.error(e, "Part of configuration could not be saved:");
		}
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

	/**
	 * Only for test-cases.
	 */
	public void reset() {
		groupsUnassigned.clear();
		groupsRegistered.clear();
		configObjects.clear();
	}

}
