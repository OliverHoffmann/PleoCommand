package pleocmd.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import pleocmd.pipe.cmd.Command;
import pleocmd.pipe.cmd.PleoMonitorCommand;

public class CommandSequenceMap {

	private final Map<String, List<Command>> map = new TreeMap<String, List<Command>>();

	public CommandSequenceMap() {
		reset();
	}

	public Set<String> getAllTriggers() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public void addTrigger(final String trigger) {
		if (!map.containsKey(trigger))
			map.put(trigger, new ArrayList<Command>());
	}

	public boolean removeTrigger(final String trigger) {
		return map.remove(trigger) != null;
	}

	public List<Command> getCommands(final String trigger) {
		final List<Command> res = map.get(trigger);
		return res == null ? null : Collections.unmodifiableList(res);
	}

	public List<Command> findCommands(final String trigger)
			throws IndexOutOfBoundsException {
		List<Command> res = getCommands(trigger);
		if (res == null) res = getCommands(trigger.toUpperCase());
		if (res == null) res = getCommands(trigger.toLowerCase());
		if (res == null)
			throw new IndexOutOfBoundsException(String.format(
					"Cannot find any commands assigned "
							+ "to the trigger '%s'", trigger));
		return Collections.unmodifiableList(res);
	}

	public boolean clearCommands(final String trigger) {
		final List<Command> commands = map.get(trigger);
		if (commands != null) commands.clear();
		return commands != null;
	}

	public void addCommand(final String trigger, final Command command) {
		List<Command> commands = map.get(trigger);
		if (commands == null) {
			commands = new ArrayList<Command>();
			map.put(trigger, commands);
		}
		commands.add(command);
	}

	public void addCommand(final String trigger, final String command) {
		addCommand(trigger, new PleoMonitorCommand(null, command));
	}

	public void reset() {
		map.clear();
	}

	public void writeToFile(final File file) throws IOException {
		final FileWriter out = new FileWriter(file);
		for (final Entry<String, List<Command>> trigger : map.entrySet()) {
			out.write("[");
			out.write(trigger.getKey());
			out.write("]\n");
			for (final Command command : trigger.getValue()) {
				out.write(command.asPleoMonitorCommand());
				out.write("\n");
			}
		}
		out.close();
	}

	public void loadFromFile(final File file) throws IOException {
		reset();
		addFromFile(file);
	}

	public void addFromFile(final File file) throws IOException {
		final BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		List<Command> commands = null;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.length() >= 2 && line.charAt(0) == '['
					&& line.charAt(line.length() - 1) == ']') {
				final String tn = line.substring(1, line.length() - 1);
				commands = map.get(tn);
				if (commands == null) {
					commands = new ArrayList<Command>();
					map.put(tn, commands);
				}
			} else {
				if (line.isEmpty() || line.charAt(0) == '#') continue;
				if (commands == null)
					throw new IOException("Cannot load command sequence: "
							+ "Expected trigger name in [...]");
				commands.add(new PleoMonitorCommand(null, line));
			}
		}
		in.close();
	}

	public void assignFromMap(final CommandSequenceMap other) {
		reset();
		addFromMap(other);
	}

	public void addFromMap(final CommandSequenceMap other) {
		for (final Entry<String, List<Command>> entry : other.map.entrySet()) {
			List<Command> commands = map.get(entry.getKey());
			if (commands == null) {
				commands = new ArrayList<Command>(entry.getValue().size());
				map.put(entry.getKey(), commands);
			}
			commands.addAll(entry.getValue());
		}
	}

}
