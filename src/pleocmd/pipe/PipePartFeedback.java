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

import pleocmd.pipe.data.Data;

/**
 * Fully synchronized class that provides feedback about a {@link PipePart} that
 * is currently running or has recently run.
 */
public final class PipePartFeedback extends Feedback {

	private int configuredCount;

	private int initializedCount;

	private int closedCount;

	private int dataReceivedCount;

	private int dataSentCount;

	private int dataPlotCount;

	private int execInterruptedCount;

	PipePartFeedback() {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(PipePart.class.getName()) : s1;
	}

	public synchronized int getConfiguredCount() {
		return configuredCount;
	}

	public synchronized int getInitializedCount() {
		return initializedCount;
	}

	public synchronized int getClosedCount() {
		return closedCount;
	}

	public synchronized int getDataReceivedCount() {
		return dataReceivedCount;
	}

	public synchronized int getDataSentCount() {
		return dataSentCount;
	}

	public synchronized int getDataPlotCount() {
		return dataPlotCount;
	}

	/**
	 * @return number of {@link Data} that have been interrupted because a
	 *         {@link Data} with a higher priority has to be executed
	 */
	public synchronized int getExecutionInterruptedCount() {
		return execInterruptedCount;
	}

	synchronized void incConfiguredCount() {
		++configuredCount;
	}

	synchronized void incInitializedCount() {
		++initializedCount;
	}

	synchronized void incClosedCount() {
		++closedCount;
	}

	public synchronized void incDataReceivedCount() {
		++dataReceivedCount;
	}

	public synchronized void incDataSentCount(final int count) {
		dataSentCount += count;
	}

	synchronized void incDataPlotCount() {
		++dataPlotCount;
	}

	synchronized void incExecutionInterruptedCount() {
		++execInterruptedCount;
	}

	@Override
	protected String getAdditionalString1() {
		return String.format(
				" has received %d, sent %d and plotted %s data(s), ",
				getDataReceivedCount(), getDataSentCount(), getDataPlotCount());
	}

	@Override
	protected String getAdditionalString2() {
		return String.format(" It has been %d time(s) been interrupted "
				+ "during processing, %d time(s) configured, "
				+ "%d time(s) initialized and %d time(s) closed.",
				getExecutionInterruptedCount(), getConfiguredCount(),
				getInitializedCount(), getClosedCount());
	}

	@Override
	protected void addAdditionalHTMLTable1(final StringBuilder sb) {
		appendToHTMLTable(sb, "Data Received", getDataReceivedCount());
		appendToHTMLTable(sb, "Data Sent", getDataSentCount());
		appendToHTMLTable(sb, "Data Plotted", getDataPlotCount());
	}

	@Override
	protected void addAdditionalHTMLTable2(final StringBuilder sb) {
		appendToHTMLTable(sb, "Interrupted during processing",
				getExecutionInterruptedCount());
		appendToHTMLTable(sb, "Configured", getConfiguredCount());
		appendToHTMLTable(sb, "Initialized", getInitializedCount());
		appendToHTMLTable(sb, "Closed", getClosedCount());
	}

}
