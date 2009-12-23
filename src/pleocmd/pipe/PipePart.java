package pleocmd.pipe;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * Base class of all {@link Input}s, {@link Converter} and {@link Output}s which
 * can be connected to the {@link Pipe}.
 * 
 * @author oliver
 */
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

	/**
	 * Tries to call {@link #close()} and writes an error message to the
	 * {@link Log} if closing fails.
	 */
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

	/**
	 * @return the {@link Config} associated with this {@link PipePart} and used
	 *         during {@link #configure()}. Changes made to it after
	 *         configuration will be ignored until {@link #configure()} is
	 *         called again.
	 */
	public final Config getConfig() {
		return config;
	}

}
