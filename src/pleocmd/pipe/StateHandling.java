package pleocmd.pipe;

import pleocmd.Log;
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
		Log.detail("'%s' changed state: '%s' => '%s'", getClass()
				.getSimpleName(), this.state, state);
		this.state = state;
	}

	private void throwException(final String msg) throws PipeException {
		throw new PipeException(null, true, "'%s' is in a wrong state: %s",
				getClass().getSimpleName(), msg);
	}

	private void throwUnknownState() throws PipeException {
		throw new PipeException(null, true, "'%s' is in an unknown state: %s",
				getClass().getSimpleName(), state);
	}

	public final void ensureConstructing() throws PipeException {
		switch (state) {
		case Constructing:
			break;
		case Constructed:
		case Configured:
		case Initialized:
			throwException("Already constructed");
			//$FALL-THROUGH$ does never occur
		default:
			throwUnknownState();
		}
	}

	public final void ensureConstructed() throws PipeException {
		switch (state) {
		case Constructing:
			throwException("Constructing");
			//$FALL-THROUGH$ does never occur
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throwException("Already initialized");
			//$FALL-THROUGH$ does never occur
		default:
			throwUnknownState();
		}
	}

	public final void ensureConfigured() throws PipeException {
		switch (state) {
		case Constructing:
			throwException("Constructing");
			//$FALL-THROUGH$ does never occur
		case Constructed:
			throwException("Not configured");
			//$FALL-THROUGH$ does never occur
		case Configured:
			break;
		case Initialized:
			throwException("Already initialized");
			//$FALL-THROUGH$ does never occur
		default:
			throwUnknownState();
		}
	}

	public final void ensureInitialized() throws PipeException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			throwException("Not initialized");
			//$FALL-THROUGH$ does never occur
		case Initialized:
			break;
		default:
			throwUnknownState();
		}
	}

	public final void ensureNoLongerInitialized() throws PipeException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throwException("Still initialized");
			//$FALL-THROUGH$ does never occur
		default:
			throwUnknownState();
		}
	}

}
