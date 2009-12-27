package pleocmd.pipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pleocmd.pipe.PipePart.HelpKind;
import pleocmd.pipe.in.FileInput;

public final class PipePartDetectionTest {

	@Test
	public final void testGetAllPipeParts() throws NoSuchMethodException {
		assertFalse(PipePartDetection.getAllPipeParts("in").isEmpty());
		System.out.println("Got list of all PipePart in 'in'");
		assertFalse(PipePartDetection.getAllPipeParts("cvt").isEmpty());
		System.out.println("Got list of all PipePart in 'cvt'");
		assertFalse(PipePartDetection.getAllPipeParts("out").isEmpty());
		System.out.println("Got list of all PipePart in 'out'");

		PipePartDetection.getHelp(FileInput.class);
		System.out.println("Checked getHelp()");

		assertEquals("File Input", PipePartDetection.callHelp(FileInput.class,
				HelpKind.Name));
		System.out.println("Checked callHelp()");
	}
}
