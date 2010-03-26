package pleocmd.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

public abstract class ConfigCollection<E> extends ConfigValue {

	public enum Type {
		Set, List
	}

	private final Type type;

	private final Collection<E> content;

	private JTextArea tc;

	public ConfigCollection(final String label, final Type type) {
		super(label);
		this.type = type;
		switch (type) {
		case List:
			content = new ArrayList<E>();
			break;
		case Set:
			content = new HashSet<E>();
			break;
		default:
			throw new InternalException(type);
		}
	}

	public ConfigCollection(final String label, final Type type,
			final Collection<E> content) {
		this(label, type);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public final Type getType() {
		return type;
	}

	public final Collection<E> getContent() {
		return Collections.unmodifiableCollection(content);
	}

	public final void setContent(final Collection<? extends E> content)
			throws ConfigurationException {
		clearContent();
		addContent(content);
	}

	public final <F extends E> void addContent(final F item)
			throws ConfigurationException {
		checkValidString(item.toString(), false);
		content.add(item);
	}

	public final void addContent(final Collection<? extends E> contentToAdd)
			throws ConfigurationException {
		for (final Object o : contentToAdd)
			checkValidString(o.toString(), false);
		content.addAll(contentToAdd);
	}

	public final <F extends E> boolean removeContent(final F item) {
		if (item == null) throw new NullPointerException();
		return content.remove(item);
	}

	public final <F extends E> boolean contains(final F item) {
		if (item == null) throw new NullPointerException();
		return content.contains(item);
	}

	public final void clearContent() {
		content.clear();
	}

	@Override
	public final String asString() {
		return content.toString();
	}

	@Override
	final void setFromString(final String string) throws ConfigurationException {
		final List<E> list = new ArrayList<E>();
		final StringTokenizer st = new StringTokenizer(string, "\n");
		while (st.hasMoreTokens())
			list.add(createItem(st.nextToken()));
		setContent(list);
	}

	protected abstract E createItem(String itemAsString)
			throws ConfigurationException;

	@Override
	final List<String> asStrings() {
		final List<String> list = new ArrayList<String>(content.size());
		for (final E item : content)
			list.add(item.toString());
		return list;
	}

	@Override
	final void setFromStrings(final List<String> strings)
			throws ConfigurationException {
		final List<E> list = new ArrayList<E>(strings.size());
		for (final String str : strings)
			list.add(createItem(str));
		setContent(list);
	}

	@Override
	final String getIdentifier() {
		switch (type) {
		case List:
			return "list";
		case Set:
			return "set";
		default:
			throw new InternalException(type);
		}
	}

	@Override
	final boolean isSingleLined() {
		return false;
	}

	@Override
	public final void insertGUIComponents(final Layouter lay) {
		tc = new JTextArea(asString(), 50, 5);
		lay.add(tc, true);
	}

	@Override
	public final void setFromGUIComponents() {
		try {
			setFromString(tc.getText());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

}
