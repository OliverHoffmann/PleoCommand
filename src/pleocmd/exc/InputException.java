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

import pleocmd.pipe.in.Input;

/**
 * Subclass of {@link PipeException} which handles Exceptions thrown by
 * {@link Input} and sub classes.
 * 
 * @author oliver
 */
public final class InputException extends PipeException {

	private static final long serialVersionUID = -5901713172300885500L;

	/**
	 * Constructs a new {@link InputException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#close()} has been
	 *            called).
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public InputException(final Input sender, final boolean permanent,
			final String message, final Object... args) {
		super(sender, permanent, message, args);
	}

	/**
	 * Constructs a new {@link InputException}.
	 * 
	 * @param sender
	 *            the source of the exception or <b>null</b>
	 * @param permanent
	 *            if true the sender will be ignored during all further pipe
	 *            processing (until {@link pleocmd.pipe.Pipe#close()} has been
	 *            called).
	 * @param cause
	 *            another {@link Exception} which caused this one
	 * @param message
	 *            a message associated with this {@link Exception}. If arguments
	 *            are available, it will be interpreted as a format String like
	 *            in {@link String#format(String, Object...)}.
	 * @param args
	 *            arguments for the message
	 */
	public InputException(final Input sender, final boolean permanent,
			final Throwable cause, final String message, final Object... args) {
		super(sender, permanent, cause, message, args);
	}

}
