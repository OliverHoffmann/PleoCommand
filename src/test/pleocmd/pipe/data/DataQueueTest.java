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

package test.pleocmd.pipe.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.DataQueue;
import pleocmd.pipe.data.SingleFloatData;
import pleocmd.pipe.val.Value;
import test.pleocmd.Testcases;

public final class DataQueueTest extends Testcases {

	private static final int BUF_SIZE = 1024;

	@Test(timeout = 60000)
	public void testPutAndGet() throws IOException, InterruptedException {
		// prepare
		final Data[] buf0 = new Data[BUF_SIZE];
		final Data[] buf1 = new Data[BUF_SIZE];
		for (int i = 0; i < BUF_SIZE; ++i)
			buf0[i] = new Data(new ArrayList<Value>(0), null);
		final DataQueue queue = new DataQueue();

		// execute twice
		for (int i = 0; i < 2; ++i) {
			Log.consoleOut("Starting test loop for DataQueue '%s'", queue);
			for (final Data data : buf0)
				queue.put(data);
			Log.consoleOut("Put Data into DataQueue '%s'", queue);
			for (int j = 0; j < BUF_SIZE; ++j)
				buf1[j] = queue.get();
			Log.consoleOut("Read Data from DataQueue '%s'", queue);
			assertArrayEquals(buf0, buf1);

			queue.close();
			Log.consoleOut("Closed DataQueue '%s'", queue);

			// now get should fail
			assertNull(queue.get());

			queue.resetCache();
			Log.consoleOut("Reset DataQueue '%s'", queue);
		}

		// now again with some higher and lower priority objects
		for (final Data data : buf0)
			queue.put(data);
		Data d = createData(1);
		queue.put(d);
		assertSame(d, queue.get());
		Log.consoleOut("Tested High-Priority Data in DataQueue '%s'", queue);

		queue.resetCache();
		Log.consoleOut("Reset DataQueue '%s'", queue);

		// now again with some higher and lower priority objects
		for (final Data data : buf0)
			queue.put(data);
		d = createData(-1);
		queue.put(d);
		assertSame(buf0[0], queue.get());
		Log.consoleOut("Tested Low-Priority Data in DataQueue '%s'", queue);

		Log.consoleOut("Done testing DataQueue");
	}

	private Data createData(final int prioDelta) {
		return new SingleFloatData(0.3, 5, null,
				(byte) (Data.PRIO_DEFAULT + prioDelta), Data.TIME_NOTIME);
	}
}
