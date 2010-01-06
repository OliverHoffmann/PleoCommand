package pleocmd.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Group {

	private final String name;

	private final Map<String, ConfigValue> valueMap;

	private final Object user;

	public Group(final String name) {
		valueMap = new HashMap<String, ConfigValue>();
		this.name = name;
		user = null;
	}

	public Group(final String name, final Object user) {
		valueMap = new HashMap<String, ConfigValue>();
		this.name = name;
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public Object getUser() {
		return user;
	}

	public Map<String, ConfigValue> getValueMap() {
		return Collections.unmodifiableMap(valueMap);
	}

	public boolean isEmpty() {
		return valueMap.isEmpty();
	}

	public int getSize() {
		return valueMap.size();
	}

	/**
	 * Adds a {@link ConfigValue} to this {@link Group}.<br>
	 * If one with the same label already exists, it will be replaced.
	 * 
	 * @param value
	 *            the new {@link ConfigValue}
	 * @return the {@link Group} itself
	 */
	public Group add(final ConfigValue value) {
		valueMap.put(value.getLabel(), value);
		return this;
	}

	/**
	 * Returns the {@link ConfigValue} to a given label if one exists.
	 * 
	 * @param label
	 *            the label of the {@link ConfigValue}
	 * @return a {@link ConfigValue} or <b>null</b>
	 */
	public ConfigValue get(final String label) {
		if (label == null) throw new NullPointerException();
		return valueMap.get(label);
	}

	/**
	 * Returns the {@link ConfigValue} to a given label or the default one if it
	 * doesn't exist.
	 * 
	 * @param label
	 *            the label of the {@link ConfigValue}
	 * @param def
	 *            the default {@link ConfigValue} if a fitting one could not be
	 *            found
	 * @return a {@link ConfigValue}, never <b>null</b>
	 */
	public ConfigValue get(final String label, final ConfigValue def) {
		if (label == null || def == null) throw new NullPointerException();
		final ConfigValue res = valueMap.get(label);
		return res == null ? def : res;
	}

	/**
	 * Removes a {@link ConfigValue} with the given label if it exists.
	 * 
	 * @param label
	 *            the label of the {@link ConfigValue}
	 */
	public void remove(final String label) {
		if (label == null) throw new NullPointerException();
		valueMap.remove(label);
	}

	@Override
	public String toString() {
		return name + " " + valueMap.values().toString();
	}

}
