package pleocmd.pipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.pipe.PipePart.HelpKind;
import pleocmd.pipe.in.FileInput;

public final class PipePartDetectionTest {

	@Test
	public void testGetAllPipeParts() throws NoSuchMethodException {
		assertFalse(PipePartDetection.getAllPipeParts("in").isEmpty());
		Log.consoleOut("Got list of all PipePart in 'in'");
		assertFalse(PipePartDetection.getAllPipeParts("cvt").isEmpty());
		Log.consoleOut("Got list of all PipePart in 'cvt'");
		assertFalse(PipePartDetection.getAllPipeParts("out").isEmpty());
		Log.consoleOut("Got list of all PipePart in 'out'");

		PipePartDetection.getHelp(FileInput.class);
		Log.consoleOut("Checked getHelp()");

		assertEquals("File Input", PipePartDetection.callHelp(FileInput.class,
				HelpKind.Name));
		Log.consoleOut("Checked callHelp()");
	}
}
