package pleocmd.pipe;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.PipeException;

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
		try {
			setState(State.Constructed);
		} catch (final PipeException e) {
			Log.error(e);
		}
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
		ensureConstructed();
		try {
			configured0();
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot configure");
		}
		setState(State.Configured);
	}

	protected abstract void configured0() throws PipeException, IOException;

	public final void init() throws PipeException {
		ensureConfigured();
		try {
			init0();
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot initialize");
		}
		setState(State.Initialized);
	}

	protected abstract void init0() throws PipeException, IOException;

	public final void tryClose() {
		try {
			close();
		} catch (final PipeException e) {
			Log.error(e, "Cannot close '%s'", getClass().getSimpleName());
		}
	}

	public final void close() throws PipeException {
		ensureInitialized();
		try {
			close0();
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot close");
		}
		setState(State.Configured);
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
			ensureNoLongerInitialized();
		} finally {
			super.finalize();
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
