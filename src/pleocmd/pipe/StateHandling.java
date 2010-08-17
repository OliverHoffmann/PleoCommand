// This file is part of PleoCommand:
// Interactively control Pleo with psychobiological parameters
//
// Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Boston, USA.

package pleocmd.pipe;

import java.io.IOException;

import pleocmd.Log;
import pleocmd.exc.InternalException;
import pleocmd.exc.PipeException;
import pleocmd.exc.StateException;

/**
 * This class helps sub classes to deny calling methods according to a state in
 * which the object currently is.<br>
 * All methods which should be protected must have an appropriate ensure...()
 * call as their first statement.
 * 
 * @author oliver
 */
public abstract class StateHandling {

	/**
	 * All valid states an object can be in.
	 */
	protected enum State {
		/**
		 * This object is currently being constructor (i.e. the constructor has
		 * not yet be finished)
		 */
		Constructing,
		/**
		 * This object is constructed but not yet configured.
		 */
		Constructed,
		/**
		 * This object is constructed and configured and therefore ready to be
		 * initialized.
		 */
		Configured,
		/**
		 * This object is constructed, configured and initialized and therefore
		 * ready to be used.
		 */
		Initialized
	}

	private State state = State.Constructing;

	/**
	 * Must be called as the last statement in all constructors of all sub
	 * classes.
	 */
	public final void constructed() {
		try {
			ensureConstructing();
			setState(State.Constructed);
		} catch (final StateException e) {
			throw new InternalException(e);
		}
	}

	/**
	 * @return current {@link State} of this object.
	 */
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

	public final void configure() throws PipeException {
		ensureConstructed();
		try {
			configure0();
			setState(State.Configured);
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot configure '%s'",
					toString());
		}
	}

	/**
	 * Can contain special code which should be invoked in sub-classes during
	 * configuration.
	 * 
	 * @throws PipeException
	 *             if configuration fails
	 * @throws IOException
	 *             if configuration fails
	 */
	protected void configure0() throws PipeException, IOException {
		// do nothing by default
	}

	public final void init() throws PipeException {
		ensureConfigured();
		try {
			init0();
			setState(State.Initialized);
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot initialize '%s'",
					toString());
		}
	}

	/**
	 * Can contain special code which should be invoked in sub-classes during
	 * initialization.
	 * 
	 * @throws PipeException
	 *             if initialization fails
	 * @throws IOException
	 *             if initialization fails
	 */
	protected void init0() throws PipeException, IOException {
		// do nothing by default
	}

	public final void close() throws PipeException {
		ensureInitialized();
		try {
			setState(State.Configured);
			close0();
		} catch (final IOException e) {
			throw new PipeException(this, true, e, "Cannot close '%s'",
					toString());
		}
	}

	/**
	 * Can contain special code which should be invoked in sub-classes during
	 * closing.
	 * 
	 * @throws PipeException
	 *             if closing fails
	 * @throws IOException
	 *             if closing fails
	 */
	protected void close0() throws PipeException, IOException {
		// do nothing by default
	}

	private void setState(final State state) throws StateException {
		if (!checkValidChange(this.state, state))
			throw new StateException(this, true,
					"Cannot change state from '%s' to '%s'", this.state, state);
		Log.detail("'%s' changed state: '%s' => '%s'", toString(), this.state,
				state);
		this.state = state;
	}

	private void throwException(final String msg) throws StateException {
		throw new StateException(this, true, "'%s' is in a wrong state: %s",
				toString(), msg);
	}

	private void throwUnknownState() throws StateException {
		throw new StateException(this, true, "'%s' is in an unknown state: %s",
				toString(), state);
	}

	/**
	 * Ensures that this object is in a valid {@link State}.
	 * 
	 * @throws StateException
	 *             if the object is already constructed
	 */
	public final void ensureConstructing() throws StateException {
		switch (state) {
		case Constructing:
			break;
		case Constructed:
		case Configured:
		case Initialized:
			throwException("Already constructed");
			break;
		default:
			throwUnknownState();
			break;
		}
	}

	/**
	 * Ensures that this object is in a valid {@link State}.
	 * 
	 * @throws StateException
	 *             if the object is being constructed or already initialized
	 */
	public final void ensureConstructed() throws StateException {
		switch (state) {
		case Constructing:
			throwException("Constructing");
			break;
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throwException("Already initialized");
			break;
		default:
			throwUnknownState();
			break;
		}
	}

	/**
	 * Ensures that this object is in a valid {@link State}.
	 * 
	 * @throws StateException
	 *             if the object is being constructed, not yet configured or
	 *             already initialized
	 */
	public final void ensureConfigured() throws StateException {
		switch (state) {
		case Constructing:
			throwException("Constructing");
			break;
		case Constructed:
			throwException("Not configured");
			break;
		case Configured:
			break;
		case Initialized:
			throwException("Already initialized");
			break;
		default:
			throwUnknownState();
			break;
		}
	}

	/**
	 * Ensures that this object is in a valid {@link State}.
	 * 
	 * @throws StateException
	 *             if the object is not already initialized
	 */
	public final void ensureInitialized() throws StateException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			throwException("Not initialized");
			break;
		case Initialized:
			break;
		default:
			throwUnknownState();
			break;
		}
	}

	/**
	 * Ensures that this object is in a valid {@link State}.
	 * 
	 * @throws StateException
	 *             if the object is currently initialized
	 */
	public final void ensureNoLongerInitialized() throws StateException {
		switch (state) {
		case Constructing:
		case Constructed:
		case Configured:
			break;
		case Initialized:
			throwException("Still initialized");
			break;
		default:
			throwUnknownState();
			break;
		}
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
	public String toString() { // CS_IGNORE_PREV keep overridable
		return String.format("%s <%s>", getClass().getSimpleName(), getState());
	}

}
