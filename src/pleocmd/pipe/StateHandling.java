package pleocmd.pipe;

import pleocmd.exc.PipeException;

public class StateHandling {

	public enum State {
		Constructing, Constructed, Configured, Initialized
	}

	private State state = State.Constructing;

	public final State getState() {
		return state;
	}

	/**
	 * Only allowed changes are:<br>
	 * Constructing -> Constructed -> Configured <-> Initialized
	 * 
	 * @param from
	 *            old {@link State}
	 * @param to
	 *            new {@link State}
	 * @return true if change is allowed
	 */
	@SuppressWarnings("incomplete-switch")
	private static boolean checkValidChange(final State from, final State to) {
		switch (from) {
		case Constructing:
			switch (to) {
			case Constructing:
			case Constructed:
				return true;
			}
			break;
		case Constructed:
			switch (to) {
			case Constructed:
			case Configured:
				return true;
			}
			break;
		case Configured:
		case Initialized:
			switch (to) {
			case Configured:
			case Initialized:
				return true;
			}
			break;
		}
		return false;
	}

	protected final void setState(final State state) throws PipeException {
		if (!checkValidChange(this.state, state))
			throw new PipeException(null, true,
					"Cannot change state from '%s' to '%s'", this.state, state);
		this.state = state;
	}

	public final void ensureConstructing() throws PipeException {
		switch (state) {
		case Constructing:
			break;
		case Constructed:
		case Configured:
		case Initialized:
			throw new PipeException(null, true, "Already constructed");
		default:
			throw new PipeException(null, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	public final void ensureConstructed() throws PipeException {
		switch (state) {
		case Constructing:
			throw new PipeException(null, true, "Constructing");
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throw new PipeException(null, true, "Already initialized");
		default:
			throw new PipeException(null, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	public final void ensureConfigured() throws PipeException {
		switch (state) {
		case Constructing:
			throw new PipeException(null, true, "Constructing");
		case Constructed:
			throw new PipeException(null, true, "Not configured");
		case Configured:
			break;
		case Initialized:
			throw new PipeException(null, true, "Already initialized");
		default:
			throw new PipeException(null, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	public final void ensureInitialized() throws PipeException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			throw new PipeException(null, true, "Not initialized");
		case Initialized:
			break;
		default:
			throw new PipeException(null, true,
					"Internal error: Unknown state: %s", state);
		}
	}

	public final void ensureNoLongerInitialized() throws PipeException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throw new PipeException(null, true, "Still initialized");
		default:
			throw new PipeException(null, true,
					"Internal error: Unknown state: %s", state);
		}
	}

}
