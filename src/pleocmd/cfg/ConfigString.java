package pleocmd.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import pleocmd.itfc.gui.Layouter;

public final class ConfigString extends ConfigValue {

	private final boolean multiLine;

	private String content;

	private JTextComponent tc;

	public ConfigString(final String label, final boolean multiLine) {
		super(label);
		this.multiLine = multiLine;
		content = "";
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) throws ConfigurationException {
		if (content == null) throw new NullPointerException("content");
		if (!multiLine && content.contains("\n"))
			throw new ConfigurationException("content contains line-feeds");
		this.content = content;
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
		final List<String> res = new ArrayList<String>();
		final StringTokenizer st = new StringTokenizer(content, "\n");
		while (st.hasMoreTokens())
			res.add(st.nextToken());
		return res;
	}

	@Override
	void setFromStrings(final List<String> strings) {
		final StringBuilder sb = new StringBuilder();
		for (final String str : strings) {
			sb.append(str);
			sb.append('\n');
		}
		try {
			setContent(sb.toString());
		} catch (final ConfigurationException e) {
			throw new InternalError("Caught exception which should never occur");
		}
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
			throw new InternalError("Caught exception which should never occur");
		}
	}

}
