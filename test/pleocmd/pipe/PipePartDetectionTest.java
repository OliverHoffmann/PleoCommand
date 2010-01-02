package pleocmd.pipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;
import pleocmd.pipe.PipePart.HelpKind;
import pleocmd.pipe.in.FileInput;

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

		assertEquals("File Input", PipePartDetection.callHelp(FileInput.class,
				HelpKind.Name));
		Log.consoleOut("Checked callHelp()");
	}
}
