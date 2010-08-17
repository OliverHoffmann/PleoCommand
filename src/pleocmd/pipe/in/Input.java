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

package pleocmd.pipe.in;

import java.io.IOException;

import pleocmd.exc.InputException;
import pleocmd.exc.StateException;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Input extends PipePart {

	private int threadReferenceCounter;

	@Override
	protected void configure1() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	protected void init0() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	protected void close0() throws InputException, IOException {
		// do nothing by default
	}

	@Override
	public final String getInputDescription() {
		return "";
	}

	@Override
	public final boolean isConnectionAllowed0(final PipePart trg) {
		return true;
	}

	public final Data readData() throws InputException {
		try {
			ensureInitialized();
			final Data res = readData0();
			if (res != null) // got a valid data block
				getFeedback().incDataSentCount(1);
			return res;
		} catch (final IOException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		} catch (final StateException e) {
			throw new InputException(this, true, e, "Cannot read data block");
		}
	}

	protected abstract Data readData0() throws InputException, IOException;

	/**
	 * Increment the thread-reference counter (number of input threads which
	 * will be using this Input).<br>
	 * Should only be used from {@link Pipe}.
	 * 
	 * @return new reference counter
	 */
	public int incThreadReferenceCounter() {
		return ++threadReferenceCounter;
	}

	/**
	 * Decrement the thread-reference counter (number of input threads which
	 * will be using this Input).<br>
	 * Should only be used from {@link Pipe}.
	 * 
	 * @return new reference counter
	 */
	public int decThreadReferenceCounter() {
		return ++threadReferenceCounter;
	}

}
