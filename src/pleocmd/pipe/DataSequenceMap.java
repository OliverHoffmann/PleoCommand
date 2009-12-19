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

import pleocmd.exc.ConverterException;

public final class DataSequenceMap {

	private final Map<String, List<Data>> map = new TreeMap<String, List<Data>>();

	public DataSequenceMap() {
		reset();
	}

	public Set<String> getAllTriggers() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public void addTrigger(final String trigger) {
		if (!map.containsKey(trigger)) map.put(trigger, new ArrayList<Data>());
	}

	public boolean removeTrigger(final String trigger) {
		return map.remove(trigger) != null;
	}

	public List<Data> getDataList(final String trigger) {
		final List<Data> res = map.get(trigger);
		return res == null ? null : Collections.unmodifiableList(res);
	}

	public List<Data> findDataList(final String trigger)
			throws ConverterException {
		List<Data> res = getDataList(trigger);
		if (res == null) res = getDataList(trigger.toUpperCase());
		if (res == null) res = getDataList(trigger.toLowerCase());
		if (res == null)
			throw new ConverterException(null, false,
					"Cannot find any data assigned to the trigger '%s'",
					trigger);
		return Collections.unmodifiableList(res);
	}

	public boolean clearDataList(final String trigger) {
		final List<Data> dataList = map.get(trigger);
		if (dataList != null) dataList.clear();
		return dataList != null;
	}

	public void addData(final String trigger, final Data data) {
		List<Data> dataList = map.get(trigger);
		if (dataList == null) {
			dataList = new ArrayList<Data>();
			map.put(trigger, dataList);
		}
		dataList.add(data);
	}

	public void reset() {
		map.clear();
	}

	public void writeToFile(final File file) throws IOException {
		final FileWriter out = new FileWriter(file);
		for (final Map.Entry<String, List<Data>> trigger : map.entrySet()) {
			out.write("[");
			out.write(trigger.getKey());
			out.write("]\n");
			for (final Data data : trigger.getValue()) {
				out.write(data.toString());
				out.write("\n");
			}
		}
		out.close();
	}

	public void readFromFile(final File file) throws IOException {
		reset();
		addFromFile(file);
	}

	public void addFromFile(final File file) throws IOException {
		final BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		List<Data> dataList = null;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.length() >= 2 && line.charAt(0) == '['
					&& line.charAt(line.length() - 1) == ']') {
				final String tn = line.substring(1, line.length() - 1);
				dataList = map.get(tn);
				if (dataList == null) {
					dataList = new ArrayList<Data>();
					map.put(tn, dataList);
				}
			} else {
				if (line.isEmpty() || line.charAt(0) == '#') continue;
				if (dataList == null)
					throw new IOException("Cannot read data sequence: "
							+ "Expected trigger name in [...]");
				dataList.add(Data.createFromAscii(line));
			}
		}
		in.close();
	}

	public void assignFromMap(final DataSequenceMap other) {
		reset();
		addFromMap(other);
	}

	public void addFromMap(final DataSequenceMap other) {
		for (final Map.Entry<String, List<Data>> entry : other.map.entrySet()) {
			List<Data> dataList = map.get(entry.getKey());
			if (dataList == null) {
				dataList = new ArrayList<Data>(entry.getValue().size());
				map.put(entry.getKey(), dataList);
			}
			dataList.addAll(entry.getValue());
		}
	}

	/**
	 * Creates a deep copy of the given list while changing the priority of
	 * every {@link Data} in the list which has the default priority to the
	 * given new one.
	 * 
	 * @param org
	 *            the original list of {@link Data}
	 * @param newPriority
	 *            priority for every new {@link Data} which had the default
	 *            priority
	 * @return a new list of {@link Data} copies
	 */
	public static List<Data> cloneList(final List<Data> org,
			final byte newPriority) {
		final List<Data> res = new ArrayList<Data>(org.size());
		for (final Data data : org) {
			final byte orgPriority = data.getPriority();
			res.add(new Data(data,
					orgPriority == Data.PRIO_DEFAULT ? newPriority
							: orgPriority));
		}
		return res;
	}

}
