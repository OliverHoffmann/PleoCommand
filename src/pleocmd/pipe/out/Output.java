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

package pleocmd.pipe.out;

import java.io.IOException;

import pleocmd.exc.OutputException;
import pleocmd.exc.StateException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Output extends PipePart {

	@Override
	protected void configure1() throws OutputException, IOException {
		// do nothing by default
	}

	@Override
	protected void init0() throws OutputException, IOException {
		// do nothing by default
	}

	@Override
	protected void close0() throws OutputException, IOException {
		// do nothing by default
	}

	@Override
	public final String getOutputDescription() {
		return "";
	}

	@Override
	public final boolean isConnectionAllowed0(final PipePart trg) {
		return false;
	}

	public final boolean write(final Data data) throws OutputException {
		try {
			ensureInitialized();
			getFeedback().incDataReceivedCount();
			final boolean res = write0(data);
			if (res) getFeedback().incDataSentCount(1);
			return res;
		} catch (final IOException e) {
			throw new OutputException(this, false, e,
					"Cannot write data block '%s'", data);
		} catch (final StateException e) {
			throw new OutputException(this, true, e,
					"Cannot write data block '%s'", data);
		}
	}

	protected abstract boolean write0(Data data) throws OutputException,
			IOException;

	// CS_IGNORE_NEXT
	@Override
	protected boolean topDownCheck_outputReached() { // CS_IGNORE
		return true;
	}

}
