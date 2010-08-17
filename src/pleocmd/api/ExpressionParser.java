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

package pleocmd.api;

import pleocmd.Log;
import pleocmd.exc.ParserException;

public final class ExpressionParser {

	private static final int ERROR_SUCCESS = 0;
	private static final int ERROR_SYNTAX = 1;
	private static final int ERROR_FUNCTION_UNKNOWN = 2;
	private static final int ERROR_SYMB_TOO_LONG = 3;

	private static boolean libraryLoaded;

	private long instrHandle;

	public ExpressionParser(final String expression) throws ParserException {
		if (!libraryLoaded) {
			libraryLoaded = true;
			try {
				System.loadLibrary("ExprParser");
			} catch (final UnsatisfiedLinkError e) {
				Log.error(e, "Cannot find external library in '%s'", System
						.getProperty("java.library.path"));
			}
		}

		instrHandle = parse(expression);
		final String msg;
		switch (getLastError(instrHandle)) {
		case ERROR_SUCCESS:
			if (Log.canLogDetail()) Log.detail(getInstructions(instrHandle));
			return;
		case ERROR_SYNTAX:
			msg = "Syntax Error";
			break;
		case ERROR_FUNCTION_UNKNOWN:
			msg = "Name of function unknown";
			break;
		case ERROR_SYMB_TOO_LONG:
			msg = "Name of symbol too long";
			break;
		default:
			msg = "Unknown Error";
			break;
		}
		final int pos = getLastErrorPos(instrHandle);
		throw new ParserException("%s at '%c', position %d in '%s'", msg,
				pos >= 0 && pos < expression.length() ? expression.charAt(pos)
						: '?', pos, expression);
	}

	public double execute(final double[] channelData) {
		if (channelData.length > 32)
			throw new IndexOutOfBoundsException(
					"Maximum of 32 channels allowed");
		return execute(instrHandle, channelData);
	}

	public void free() {
		freeHandle(instrHandle);
		instrHandle = 0;
	}

	@Override
	protected void finalize() throws Throwable {
		freeHandle(instrHandle);
		instrHandle = 0;
		super.finalize();
	}

	private native long parse(final String expression);

	private native double execute(final long handle, final double[] channelData);

	private native int getLastError(final long handle);

	private native int getLastErrorPos(final long handle);

	private native String getInstructions(final long handle);

	private native void freeHandle(final long handle);

}
