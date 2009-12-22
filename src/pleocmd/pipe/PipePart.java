package pleocmd.pipe;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.pipe.cfg.Config;

public abstract class PipePart extends StateHandling {

	private final Config config;

	/**
	 * @param emptyConfig
	 *            an empty dummy configuration which has correct value names and
	 *            identifiers but with no values assigned to it.
	 */
	public PipePart(final Config emptyConfig) {
		config = emptyConfig;
		config.setOwner(this);
		constructed();
	}

	public final void tryClose() {
		try {
			close();
		} catch (final PipeException e) {
			Log.error(e, "Cannot close '%s'", getClass().getSimpleName());
		}
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName();
	}

	public final Config getConfig() {
		return config;
	}

}
