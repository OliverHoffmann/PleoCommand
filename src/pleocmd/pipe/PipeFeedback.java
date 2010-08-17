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

import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * Fully synchronized class that provides feedback about a {@link Pipe} that is
 * currently running or has recently run.
 * 
 * @author oliver
 */
public final class PipeFeedback extends Feedback {

	private int dataInputCount;

	private int dataConvertedCount;

	private int dataOutputCount;

	private long lastNormalDataOutput;

	PipeFeedback() {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;
	}

	/**
	 * @return number of {@link Data} read from an {@link Input}
	 */
	public synchronized int getDataInputCount() {
		return dataInputCount;
	}

	/**
	 * @return number of {@link Data} passed to a {@link Converter}
	 */
	public synchronized int getDataConvertedCount() {
		return dataConvertedCount;
	}

	/**
	 * @return number of {@link Data} written to an {@link Output}
	 */
	public synchronized int getDataOutputCount() {
		return dataOutputCount;
	}

	synchronized void incDataInputCount() {
		++dataInputCount;
	}

	synchronized void incDataConvertedCount() {
		++dataConvertedCount;
	}

	synchronized void incDataOutputCount(final boolean isNormalData) {
		++dataOutputCount;
		if (isNormalData) lastNormalDataOutput = System.currentTimeMillis();
	}

	public synchronized long getLastNormalDataOutput() {
		return lastNormalDataOutput;
	}

	@Override
	protected String getAdditionalString1() {
		return String.format(
				" has read %d, converted %d and written %d data(s), ",
				getDataInputCount(), getDataConvertedCount(),
				getDataOutputCount());
	}

	@Override
	protected String getAdditionalString2() {
		return "";
	}

	@Override
	protected void addAdditionalHTMLTable1(final StringBuilder sb) {
		appendToHTMLTable(sb, "Data read from Inputs", getDataInputCount());
		appendToHTMLTable(sb, "Data converted in Converters",
				getDataConvertedCount());
		appendToHTMLTable(sb, "Data written from Outputs", getDataOutputCount());
	}

	@Override
	protected void addAdditionalHTMLTable2(final StringBuilder sb) {
		// nothing to do
	}

}
