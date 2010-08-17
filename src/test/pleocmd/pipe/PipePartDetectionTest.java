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
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.PipePartDetection;
import pleocmd.pipe.in.FileInput;
import test.pleocmd.Testcases;

public final class PipePartDetectionTest extends Testcases {

	@Test
	public void testGetAllPipeParts() throws NoSuchMethodException {
		assertFalse(PipePartDetection.ALL_INPUT.isEmpty());
		Log.consoleOut("Got list of all PipePart in 'in'");
		assertFalse(PipePartDetection.ALL_CONVERTER.isEmpty());
		Log.consoleOut("Got list of all PipePart in 'cvt'");
		assertFalse(PipePartDetection.ALL_OUTPUT.isEmpty());
		Log.consoleOut("Got list of all PipePart in 'out'");

		PipePartDetection.getHelp(FileInput.class);
		Log.consoleOut("Checked getHelp()");

		assertEquals("File Input", PipePart.getName(FileInput.class));
		Log.consoleOut("Checked callHelp()");
	}
}
