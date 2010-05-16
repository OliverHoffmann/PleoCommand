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
import pleocmd.pipe.val.Value;
import test.pleocmd.Testcases;

public final class DataQueueTest extends Testcases {

	private static final int BUF_SIZE = 1024;

	@Test(timeout = 60000)
	public void testPutAndGet() throws IOException, InterruptedException {
		// no need for detailed output

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
		return new Data(new ArrayList<Value>(0), null,
				(byte) (Data.PRIO_DEFAULT + prioDelta), Data.TIME_NOTIME);
	}

}
