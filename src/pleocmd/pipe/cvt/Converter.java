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

package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.exc.StateException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.data.Data;

/**
 * @author oliver
 */
public abstract class Converter extends PipePart {

	@Override
	protected void configure1() throws ConverterException, IOException {
		// do nothing by default
	}

	@Override
	protected void init0() throws ConverterException, IOException {
		// do nothing by default
	}

	@Override
	protected void close0() throws ConverterException, IOException {
		// do nothing by default
	}

	@Override
	public final boolean isConnectionAllowed0(final PipePart trg) {
		return true;
	}

	public final List<Data> convert(final Data data) throws ConverterException {
		try {
			getFeedback().incDataReceivedCount();
			ensureInitialized();
			final List<Data> res = convert0(data);
			if (res != null) getFeedback().incDataSentCount(res.size());
			return res;
		} catch (final IOException e) {
			throw new ConverterException(this, false, e,
					"Cannot convert data block");
		} catch (final StateException e) {
			throw new ConverterException(this, true, e,
					"Cannot convert data block");
		}
	}

	protected abstract List<Data> convert0(final Data data)
			throws ConverterException, IOException;

}
