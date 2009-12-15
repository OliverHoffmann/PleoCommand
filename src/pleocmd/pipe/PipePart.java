package pleocmd.pipe;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.PipeException;

public abstract class PipePart {

	public enum State {
		Constructing, Contructed, Configured, Initialized
	}

	private State state = State.Constructing;

	private final Config config;

	/**
	 * @param emptyConfig
	 *            an empty dummy configuration which has correct value names and
	 *            identifiers but with no values assigned to it.
	 */
	public PipePart(final Config emptyConfig) {
		config = emptyConfig;
		config.setOwner(this);
		state = State.Contructed;
	}

	/**
	 * Tells the {@link PipePart} to accept the configuration data currently
	 * stored in the {@link Config}.
	 * 
	 * @throws PipeException
	 *             if the data stored in {@link Config} is invalid.
	 * @see #getConfig()
	 */
	public final void configured() throws PipeException {
		switch (state) {
		case Constructing:
			throw new PipeException(this, true, "Constructing");
		case Contructed:
		case Configured:
			try {
				configured0();
			} catch (final IOException e) {
				throw new PipeException(this, true, e, "Cannot configure");
			}
			state = State.Configured;
			break;
		case Initialized:
			throw new PipeException(this, true, "Already initialized");
		default:
			throw new PipeException(this, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	protected abstract void configured0() throws PipeException, IOException;

	public final void init() throws PipeException {
		switch (state) {
		case Constructing:
			throw new PipeException(this, true, "Constructing");
		case Contructed:
			throw new PipeException(this, true, "Not configured");
		case Configured:
			try {
				init0();
			} catch (final IOException e) {
				throw new PipeException(this, true, e, "Cannot initialize");
			}
			state = State.Initialized;
			break;
		case Initialized:
			throw new PipeException(this, true, "Already initialized");
		default:
			throw new PipeException(this, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	protected abstract void init0() throws PipeException, IOException;

	public final void tryClose() {
		try {
			close();
		} catch (final PipeException e) {
			Log.error(e);
		}
	}

	public final void close() throws PipeException {
		switch (state) {
		case Constructing:
		case Contructed:
		case Configured:
			throw new PipeException(this, true, "Not initialized");
		case Initialized:
			try {
				close0();
			} catch (final IOException e) {
				throw new PipeException(this, true, e, "Cannot close");
			}
			state = State.Configured;
			break;
		default:
			throw new PipeException(this, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	protected abstract void close0() throws PipeException, IOException;

	public final void reset() throws PipeException {
		close();
		init();
	}

	// CS_IGNORE_NEXT This is the only finalize()
	@Override
	protected final void finalize() throws Throwable { // CS_IGNORE
		try {
			switch (state) {
			case Constructing:
			case Contructed:
			case Configured:
				break;
			case Initialized:
				Log.error("Destroying PipePart which "
						+ "has not been closed before");
				break;
			default:
				throw new PipeException(this, true,
						"Internal error: Unknown state: %s", state);
			}
		} finally {
			super.finalize();
		}
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName();
	}

	public final State getState() {
		return state;
	}

	public final Config getConfig() {
		return config;
	}

}
