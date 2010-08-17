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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.PipeException;
import pleocmd.pipe.PipePart;
import test.pleocmd.Testcases;

public final class PipePartTest extends Testcases {

	private final ConfigString cfg0 = new ConfigString("Config A", false);
	private final ConfigString cfg1 = new ConfigString("Config B", true);

	@SuppressWarnings("synthetic-access")
	private PipePart createPipePart() {
		return new PipePart() {
			{
				addConfig(cfg0);
				addConfig(cfg1);
				constructed();
			}

			@Override
			protected void init0() throws PipeException, IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			protected void close0() throws PipeException, IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getInputDescription() {
				return "";
			}

			@Override
			public String getOutputDescription() {
				return "";
			}

			@Override
			protected String getShortConfigDescr0() {
				return "";
			}

			@Override
			protected boolean isConnectionAllowed0(final PipePart trg) {
				return false;
			}

			@Override
			public String isConfigurationSane() {
				return null;
			}

			@Override
			protected int getVisualizeDataSetCount() {
				return 0;
			}

		};
	}

	@Test
	public void testPipePart() throws ConfigurationException {
		final PipePart pp = createPipePart();
		Log.consoleOut("Constructed new PipePart '%s'", pp);

		try {
			pp.addConfig(new ConfigString("CanNeverBeAdded", false));
			fail("addConfig() allows adding after construction");
		} catch (final IllegalStateException e) {
			assertTrue(e.toString(), e.getMessage().startsWith("Cannot add"));
		}
		Log.consoleOut("Checked owner management of PipePart '%s'", pp);

		assertEquals(2, pp.getGuiConfigs().size());
		cfg0.setContent("foo");
		cfg1.setContent("foo\nbar");
		assertEquals("ConfigValue not referenced", cfg0.getContent(),
				((ConfigString) pp.getGroup().get("Config A")).getContent());
		Log.consoleOut("Checked Group of PipePart '%s'", pp);
	}
}
