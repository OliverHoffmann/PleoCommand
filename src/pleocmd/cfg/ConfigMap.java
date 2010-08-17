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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

public abstract class ConfigMap<K extends Comparable<? super K>, V> extends
		ConfigValue {

	private final Map<K, List<V>> content;

	public ConfigMap(final String label) {
		super(label);
		content = new HashMap<K, List<V>>();
	}

	public final Set<K> getAllKeys() {
		return Collections.unmodifiableSet(content.keySet());
	}

	public final List<K> getAllKeysSorted() {
		final List<K> list = new ArrayList<K>(content.size());
		for (final K k : content.keySet())
			list.add(k);
		Collections.sort(list);
		return list;
	}

	@Override
	public final Map<K, List<V>> getContent() {
		return Collections.unmodifiableMap(content);
	}

	public final List<V> getContent(final K key) {
		final List<V> list = content.get(key);
		return Collections.unmodifiableList(list == null ? new ArrayList<V>(0)
				: list);
	}

	public final boolean hasContent(final K key) {
		return content.containsKey(key);
	}

	public final void setContent(final K key, final List<V> list)
			throws ConfigurationException {
		checkValidString(convertKey(key), false);
		for (final V v : list)
			checkValidString(convertValue(v), false);
		content.put(key, list);
		contentChanged();
	}

	public final void renameContent(final K key, final K newKey)
			throws ConfigurationException {
		checkValidString(convertKey(newKey), false);
		final List<V> list = content.remove(key);
		if (list == null) {
			contentChanged();
			throw new IllegalArgumentException(String.format(
					"Key '%s' was not in the map", key));
		}
		content.put(newKey, list);
		contentChanged();
	}

	public final void addContent(final K key, final V value)
			throws ConfigurationException {
		List<V> list = content.get(key);
		if (list == null) {
			checkValidString(convertKey(key), false);
			list = new ArrayList<V>();
			content.put(key, list);
		}
		checkValidString(convertValue(value), false);
		list.add(value);
		contentChanged();
	}

	public final void createContent(final K key) throws ConfigurationException {
		if (!content.containsKey(key)) {
			checkValidString(convertKey(key), false);
			content.put(key, new ArrayList<V>());
			contentChanged();
		}
	}

	public final void removeContent(final K key) {
		content.remove(key);
		contentChanged();
	}

	public final void clearContent(final K key) {
		final List<V> list = content.get(key);
		if (list != null) list.clear();
		contentChanged();
	}

	public final void clearContent() {
		content.clear();
		contentChanged();
	}

	public final <F extends K, W extends V> void assignFrom(
			final ConfigMap<F, W> map) {
		clearContent();
		try {
			for (final F key : map.getAllKeys()) {
				createContent(key);
				for (final W v : map.getContent(key))
					addContent(key, v);
			}
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	@Override
	public final String asString() {
		final StringBuilder sb = new StringBuilder("");
		for (final K key : getAllKeysSorted()) {
			sb.append(convertKey(key));
			sb.append(" => ");
			final List<V> vals = getContent(key);
			if (vals.isEmpty())
				sb.append("<empty>");
			else if (vals.size() <= 1)
				sb.append(convertValue(vals.get(0)));
			else {
				sb.append("{");
				for (final V val : vals) {
					sb.append(convertValue(val));
					sb.append(", ");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
				sb.append("}");
			}
			sb.append("\n");
		}
		if (!content.isEmpty()) sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	@Override
	final void setFromString(final String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	final List<String> asStrings() {
		final List<String> res = new ArrayList<String>();
		for (final Entry<K, List<V>> entry : content.entrySet()) {
			res.add(convertKey(entry.getKey()) + " =>");
			for (final V s : entry.getValue())
				res.add("\t" + convertValue(s));
		}
		return res;
	}

	@Override
	final void setFromStrings(final List<String> strings)
			throws ConfigurationException {
		clearContent();
		List<V> list = null;
		for (final String str : strings)
			if (str.endsWith("=>")) {
				list = new ArrayList<V>();
				setContent(
						createKey(str.substring(0, str.length() - 2).trim()),
						list);
			} else {
				if (list == null)
					throw new ConfigurationException("Found list entries "
							+ "before the first key");
				list.add(createValue(str));
			}
	}

	protected abstract K createKey(String keyAsString)
			throws ConfigurationException;

	protected abstract V createValue(String valueAsString)
			throws ConfigurationException;

	protected abstract String convertKey(K key);

	protected abstract String convertValue(V value);

	protected abstract void contentChanged();

	@Override
	public final String getIdentifier() {
		return null;
	}

	@Override
	final boolean isSingleLined() {
		return false;
	}

}
