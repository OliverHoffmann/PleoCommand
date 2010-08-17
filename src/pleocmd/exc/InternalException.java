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

package pleocmd.exc;

public class InternalException extends RuntimeException {

	private static final long serialVersionUID = -5339939772785000962L;

	public InternalException(final Throwable cause) {
		super("Caught an exception which should never occur", cause);
	}

	public InternalException(final Enum<?> unexpectedEnum) {
		this("Encountered an unexpected enum: '%s' of type '%s'",
				unexpectedEnum, unexpectedEnum == null ? null : unexpectedEnum
						.getDeclaringClass().getSimpleName());
	}

	public InternalException(final Throwable cause, final String msg,
			final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args), cause);
	}

	public InternalException(final String msg, final Object... args) {
		super(args.length == 0 ? msg : String.format(msg, args));
	}

}
