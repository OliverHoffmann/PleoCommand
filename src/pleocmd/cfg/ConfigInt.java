package pleocmd.cfg;

import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

public final class ConfigInt extends ConfigNumber<Integer> {

	public ConfigInt(final String label) {
		this(label, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public ConfigInt(final String label, final int content) {
		this(label);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public ConfigInt(final String label, final int min, final int max) {
		this(label, min, min, max, 1);
	}

	public ConfigInt(final String label, final int content, final int min,
			final int max) {
		this(label, content, min, max, 1);
	}

	public ConfigInt(final String label, final int content, final int min,
			final int max, final int step) {
		super(label, min, max, step);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	@Override
	public String getIdentifier() {
		return "int";
	}

	@Override
	protected boolean lessThan(final Integer nr1, final Integer nr2) {
		return nr1 < nr2;
	}

	@Override
	protected Integer valueOf(final String str) throws ConfigurationException {
		return Integer.valueOf(str);
	}

}
