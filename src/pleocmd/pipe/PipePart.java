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

	public enum HelpKind {
		Name, Description, Configuration
	}

	private final Config config;

	public PipePart() {
		config = new Config();
		config.setOwner(this);
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
	public String toString() { // CS_IGNORE_PREV keep overridable
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
