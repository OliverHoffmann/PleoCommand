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

	private Pipe pipe;

	public PipePart() {
		config = new Config();
		config.setOwner(this);
	}

	/**
	 * Tries to call {@link #configure()} and writes an error message to the
	 * {@link Log} if configuring fails.
	 * 
	 * @return true if configuring was successful
	 */
	public final boolean tryConfigure() {
		try {
			configure();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot configure '%s'", getClass().getSimpleName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #init()} and writes an error message to the
	 * {@link Log} if initializing fails.
	 * 
	 * @return true if initializing was successful
	 */
	public final boolean tryInit() {
		try {
			init();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot initialize '%s'", getClass().getSimpleName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #close()} and writes an error message to the
	 * {@link Log} if closing fails.
	 * 
	 * @return true if closing was successful
	 */
	public final boolean tryClose() {
		try {
			close();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot close '%s'", getClass().getSimpleName());
			return false;
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

	/**
	 * Returns the {@link Pipe} to which this {@link PipePart} is currently
	 * connected.
	 * 
	 * @return a {@link Pipe}
	 * @throws IllegalArgumentException
	 *             if this {@link PipePart} is not connected to any {@link Pipe}
	 */
	public final Pipe getPipe() {
		if (pipe == null)
			throw new IllegalArgumentException("Not connected to any pipe");
		return pipe;
	}

	final void connectedToPipe(final Pipe pipe) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;

		if (pipe == null) throw new NullPointerException("pipe");
		if (this.pipe != null)
			throw new IllegalArgumentException(String.format(
					"Cannot connect to pipe '%s': Already connected to '%s'",
					pipe, this.pipe));

		this.pipe = pipe;
	}

	final void disconnectedFromPipe(final Pipe pipe) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;

		if (pipe == null) throw new NullPointerException("pipe");
		if (this.pipe == null)
			throw new IllegalArgumentException(String.format(
					"Cannot disconnect from pipe '%s': Not connected", pipe));
		if (this.pipe != pipe)
			throw new IllegalArgumentException(String.format(
					"Cannot disconnect from pipe '%s': Connected to '%s'",
					pipe, this.pipe));

		this.pipe = null;
	}

}
