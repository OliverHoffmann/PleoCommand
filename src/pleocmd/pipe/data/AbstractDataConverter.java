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

package pleocmd.pipe.data;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.val.Value;

/**
 * Abstract base class for converting {@link Data} objects to and from
 * sequential forms. Contains modifiable copies of {@link Data}'s fields because
 * they are immutable inside the {@link Data} class.
 * 
 * @author oliver
 */
public abstract class AbstractDataConverter {

	private final List<Value> values;

	private byte priority;

	private long time;

	/**
	 * Creates a converter with all fields set to default values. Mostly used
	 * before overriding some of them from data read from a sequential form.
	 * 
	 * @see Data#PRIO_DEFAULT
	 * @see Data#TIME_NOTIME
	 */
	public AbstractDataConverter() {
		values = new ArrayList<Value>(8);
		priority = Data.PRIO_DEFAULT;
		time = Data.TIME_NOTIME;
	}

	/**
	 * Creates a converter with all fields assigned from an existing
	 * {@link Data}, mostly used for writing the {@link Data} to a sequential
	 * form.
	 * 
	 * @param data
	 *            the original {@link Data} to assign from
	 */
	public AbstractDataConverter(final Data data) {
		values = data;
		priority = data.getPriority();
		time = data.getTime();
	}

	public final List<Value> getValues() {
		return values;
	}

	/**
	 * Reduces the size of the internal array to reduce memory consumption.
	 */
	protected final void trimValues() {
		if (values instanceof ArrayList<?>)
			((ArrayList<?>) values).trimToSize();
	}

	/**
	 * @return the priority as in {@link Data#getPriority()}
	 */
	public final byte getPriority() {
		return priority;
	}

	protected final void setPriority(final byte priority) {
		this.priority = priority;
	}

	/**
	 * @return the priority as in {@link Data#getTime()}
	 */
	public final long getTime() {
		return time;
	}

	protected final void setTime(final long time) {
		this.time = time;
	}

	/**
	 * Creates a new {@link Data} object from all the fields of this
	 * {@link AbstractDataConverter}.
	 * 
	 * @return new {@link Data}
	 * @see Data#Data(List, Data, byte, long)
	 */
	protected final Data createDataFromFields() {
		return new Data(values, null, priority, time);
	}

}
