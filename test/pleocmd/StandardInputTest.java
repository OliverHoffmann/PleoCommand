package pleocmd;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import pleocmd.itfc.gui.MainFrame;

public final class StandardInputTest extends Testcases {

	private static final int BUF_SIZE = 16 * 1024;

	private static final long RAND_SEED = 2921756112651594520L;

	@Test(timeout = 60000)
	public void testPutAndRead() throws IOException {
		// ensure we are in GUI mode
		MainFrame.the();

		// prepare
		final byte[] buf0 = new byte[BUF_SIZE];
		final byte[] buf1 = new byte[BUF_SIZE];
		new Random(RAND_SEED).nextBytes(buf0);

		// execute twice
		for (int i = 0; i < 2; ++i) {
			Log.consoleOut("Starting test loop for StandardInput '%s'",
					StandardInput.the());
			StandardInput.the().put(buf0);
			Log.consoleOut("Put bytes into StandardInput '%s'", StandardInput
					.the());
			assertEquals(buf0.length, StandardInput.the().available());
			Log.consoleOut("Checked available bytes");
			StandardInput.the().read(buf1);
			Log.consoleOut("Read bytes from StandardInput '%s'", StandardInput
					.the());
			assertArrayEquals(buf0, buf1);

			StandardInput.the().close();
			Log.consoleOut("Closed StandardInput '%s'", StandardInput.the());

			// now available() should no longer block ...
			assertEquals(0, StandardInput.the().available());

			// ... and read should fail
			assertEquals(-1, StandardInput.the().read());

			StandardInput.the().resetCache();
			Log.consoleOut("Reset StandardInput '%s'", StandardInput.the());
		}
		Log.consoleOut("Done testing StandardInput");
	}

}
