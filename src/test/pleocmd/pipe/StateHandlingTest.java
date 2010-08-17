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

package test.pleocmd.pipe;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.exc.StateException;
import pleocmd.pipe.StateHandling;
import test.pleocmd.Testcases;

public class StateHandlingTest extends Testcases {

	static class StateHandlingTestClass extends StateHandling {

		@Override
		protected void configure0() throws PipeException, IOException {
			// nothing to do
		}

		@Override
		protected void init0() throws PipeException, IOException {
			// nothing to do
		}

		@Override
		protected void close0() throws PipeException, IOException {
			// nothing to do
		}

	}

	private StateHandlingTestClass shtc;

	@Test
	public final void testEnsureStates() throws PipeException {

		shtc = new StateHandlingTestClass();
		shtc.ensureConstructing();
		ensureNotConstructed();
		ensureNotConfigured();
		ensureNotInitialized();
		Log.consoleOut("Constructing '%s'", shtc);

		shtc.constructed(); // fake call within ctor
		shtc.ensureConstructed();
		ensureNotConstructing();
		ensureNotConfigured();
		ensureNotInitialized();
		Log.consoleOut("Constructed '%s'", shtc);

		for (int i = 0; i < 3; ++i) {
			shtc.configure();
			shtc.ensureConfigured();
			ensureNotConstructing();
			ensureNotInitialized();
			Log.consoleOut("Configured (#1) '%s'", shtc);

			shtc.configure();
			shtc.ensureConfigured();
			ensureNotConstructing();
			ensureNotInitialized();
			Log.consoleOut("Configured (#2) '%s'", shtc);

			shtc.init();
			shtc.ensureInitialized();
			ensureNotConstructing();
			ensureNotConstructed();
			ensureNotConfigured();
			ensureStillInitialized();
			Log.consoleOut("Initialized '%s'", shtc);

			shtc.close();
			shtc.ensureNoLongerInitialized();
			ensureNotConstructing();
			ensureNotInitialized();
			Log.consoleOut("Closed '%s'", shtc);
		}

		Log.consoleOut("Done testing StateHandler");
	}

	private void ensureNotConstructing() {
		try {
			shtc.ensureConstructing();
			fail("Expected StateException");
		} catch (final StateException e) {
			assertTrue(e.toString(), e.getMessage().contains("wrong state"));
		}
	}

	private void ensureNotConstructed() {
		try {
			shtc.ensureConstructed();
			fail("Expected StateException");
		} catch (final StateException e) {
			assertTrue(e.toString(), e.getMessage().contains("wrong state"));
		}
	}

	private void ensureNotConfigured() {
		try {
			shtc.ensureConfigured();
			fail("Expected StateException");
		} catch (final StateException e) {
			assertTrue(e.toString(), e.getMessage().contains("wrong state"));
		}
	}

	private void ensureNotInitialized() {
		try {
			shtc.ensureInitialized();
			fail("Expected StateException");
		} catch (final StateException e) {
			assertTrue(e.toString(), e.getMessage().contains("wrong state"));
		}
	}

	private void ensureStillInitialized() {
		try {
			shtc.ensureNoLongerInitialized();
			fail("Expected StateException");
		} catch (final StateException e) {
			assertTrue(e.toString(), e.getMessage().contains("wrong state"));
		}
	}

}
