package pleocmd.pipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;
import pleocmd.exc.PipeException;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.in.StaticInput;
import pleocmd.pipe.out.InternalCommandOutput;

public class PipeTest extends Testcases {

	@Test
	public final void testPipeAllData() throws PipeException,
			InterruptedException, IOException {
		PipeFeedback fb;

		// test empty pipe
		fb = testSimplePipe("", -1, -1, 0, 0, 0, 0, 0, 0, 0, 0);

		// test simple pipe
		fb = testSimplePipe("SC|SLEEP|100\nSC|ECHO|Echo working\n", 90, -1, 2,
				0, 2, 0, 0, 0, 0, 0);

		// test bad input
		fb = testSimplePipe("SC|HELP", -1, -1, 0, 0, 0, 1, 0, 0, 0, 0);

		// test interrupt
		fb = testSimplePipe("[P-10]SC|SLEEP|10000\nSC|ECHO|HighPrio\n"
				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\n"
				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\n", -1, 9000,
				10, 0, -1, 0, 0, 1, 0, 0);
		assertTrue(fb.getDataOutputCount() == 9
				|| fb.getDataOutputCount() == 10);

		// test low priority drop
		fb = testSimplePipe("SC|SLEEP|400\n[P-10]SC|SLEEP|30000\n", 350, 25000,
				2, 0, 1, 0, 0, 0, 1, 0);

		// test queue clearing
		fb = testSimplePipe("SC|SLEEP|10000\nSC|SLEEP|1\nSC|SLEEP|1\n"
				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\n[P05]ECHO|HighPrio\n",
				-1, -1, 7, 0, 2, 0, 0, 1, 5, 0);

	}

	// CS_IGNORE_NEXT this many parameters are ok here - only a test case
	private PipeFeedback testSimplePipe(final String staticData,
			final long minTime, final long maxTime, final int dataIn,
			final int dataCvt, final int dataOut, final int tempErr,
			final int permErr, final int intrCnt, final int dropCnt,
			final int behindCnt) throws PipeException, InterruptedException,
			IOException {
		// create pipe
		final Pipe pipe = new Pipe();
		if (!staticData.isEmpty()) {
			final Input in = new StaticInput();
			in.getConfig().get(0).setFromString(staticData);
			pipe.addInput(in);
			pipe.addOutput(new InternalCommandOutput());
		}

		// execute pipe
		pipe.configure();
		pipe.pipeAllData();

		// print log
		Log.consoleOut(pipe.getFeedback().toString());
		Log.consoleOut("Tested Pipe '%s' containing '%s'", pipe, staticData
				.replaceAll("\n(.)", "; $1").replace("\n", ""));

		// check result
		final PipeFeedback fb = pipe.getFeedback();
		if (minTime != -1)
			assertTrue("Took not long enough", fb.getElapsed() >= minTime);
		if (maxTime != -1)
			assertTrue("Took too long", fb.getElapsed() <= maxTime);
		if (dataIn != -1) assertEquals(dataIn, fb.getDataInputCount());
		if (dataCvt != -1) assertEquals(dataCvt, fb.getDataConvertedCount());
		if (dataOut != -1) assertEquals(dataOut, fb.getDataOutputCount());
		if (tempErr != -1)
			assertEquals(tempErr, fb.getTemporaryErrors().size());
		if (permErr != -1)
			assertEquals(permErr, fb.getPermanentErrors().size());
		if (intrCnt != -1) assertEquals(intrCnt, fb.getInterruptionCount());
		if (dropCnt != -1) assertEquals(dropCnt, fb.getDropCount());
		if (behindCnt != -1) assertEquals(behindCnt, fb.getBehindCount());
		return fb;
	}

	@Test
	public final void testReadWriteFiles() {
		// fail("Not yet implemented");
	}

}
