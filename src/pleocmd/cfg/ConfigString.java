package pleocmd.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.Layouter;

public final class ConfigString extends ConfigValue {

	private final boolean multiLine;

	private String content;

	private JTextComponent tc;

	public ConfigString(final String label, final boolean multiLine) {
		super(label);
		this.multiLine = multiLine;
		clearContent();
	}

	public ConfigString(final String label, final String content) {
		super(label);
		multiLine = false;
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public ConfigString(final String label, final List<String> content) {
		super(label);
		multiLine = true;
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	public String getContent() {
		return content;
	}

	public List<String> getContentList() {
		final List<String> res = new ArrayList<String>();
		final StringTokenizer st = new StringTokenizer(content, "\n");
		while (st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	public void setContent(final String content) throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine && content.contains("\n"))
			throw new ConfigurationException("content contains line-feeds");
		checkValidString(content, multiLine);
		this.content = content;
	}

	public void setContent(final List<String> content)
			throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine)
			throw new ConfigurationException("content must be single lined");
		final StringBuilder sb = new StringBuilder();
		for (final String str : content) {
			checkValidString(str, false);
			sb.append(str);
			sb.append('\n');
		}
		try {
			setContent(sb.toString());
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public void clearContent() {
		content = "";
	}

	@Override
	String asString() {
		return content;
	}

	@Override
	void setFromString(final String string) throws ConfigurationException {
		setContent(string);
	}

	@Override
	List<String> asStrings() {
		return getContentList();
	}

	@Override
	void setFromStrings(final List<String> strings)
			throws ConfigurationException {
		setContent(strings);
	}

	@Override
	String getIdentifier() {
		return null;
	}

	@Override
	boolean isSingleLined() {
		return !multiLine;
	}

	@Override
	public void insertGUIComponents(final Layouter lay) {
		tc = multiLine ? new JTextArea(content, 50, 5) : new JTextField(
				content, 50);
		lay.add(tc, true);
	}

	@Override
	public void setFromGUIComponents() {
		try {
			setContent(tc.getText());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

}
