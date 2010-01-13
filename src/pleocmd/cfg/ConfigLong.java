package pleocmd.cfg;

import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

public final class ConfigLong extends ConfigNumber<Long> {

	public ConfigLong(final String label) {
		this(label, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public ConfigLong(final String label, final long content) {
		this(label);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public ConfigLong(final String label, final long min, final long max) {
		this(label, min, min, max, 1);
	}

	public ConfigLong(final String label, final long content, final long min,
			final long max) {
		this(label, content, min, max, 1);
	}

	public ConfigLong(final String label, final long content, final long min,
			final long max, final long step) {
		super(label, min, max, step);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	@Override
	String getIdentifier() {
		return "long";
	}

	@Override
	protected boolean lessThan(final Long nr1, final Long nr2) {
		return nr1 < nr2;
	}

	@Override
	protected Long valueOf(final String str) throws ConfigurationException {
		return Long.valueOf(str);
	}

}
