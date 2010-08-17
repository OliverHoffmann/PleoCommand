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

package pleocmd.cfg;

import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

public final class ConfigLong extends ConfigNumber<Long> {

	public ConfigLong(final String label) {
		this(label, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public ConfigLong(final String label, final long content) {
		this(label);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public ConfigLong(final String label, final long min, final long max) {
		this(label, min, min, max, 1);
	}

	public ConfigLong(final String label, final long content, final long min,
			final long max) {
		this(label, content, min, max, 1);
	}

	public ConfigLong(final String label, final long content, final long min,
			final long max, final long step) {
		super(label, min, max, step);
		try {
			setContent(content);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	@Override
	public String getIdentifier() {
		return "long";
	}

	@Override
	protected boolean lessThan(final Long nr1, final Long nr2) {
		return nr1 < nr2;
	}

	@Override
	protected Long valueOf(final String str) throws ConfigurationException {
		return Long.valueOf(str);
	}

}
