package pleocmd.cfg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;

public final class ConfigurationTest extends Testcases {

	@Test
	public void testCreateWriteAndRead() throws ConfigurationException,
			IOException {
		Configuration.setDefaultConfigFile(File.createTempFile(
				"ConfigurationTest", null));
		Configuration.getDefaultConfigFile().deleteOnExit();

		// create group

		// write to file

		// read from file

		// test group

	}

	@Test
	public void testReadUsercreatedFiles() throws ConfigurationException,
			IOException {
		Configuration.setDefaultConfigFile(File.createTempFile(
				"ConfigurationTest", null));
		Configuration.getDefaultConfigFile().deleteOnExit();

		// create file
		final FileWriter out = new FileWriter(Configuration
				.getDefaultConfigFile());
		out.write("[Test]\n");
		out.write("a:foo\n");
		out.write("b:20\n");
		out.write("c:{\n");
		out.write("1\n");
		out.write("2\n");
		out.write("3\n");
		out.write("}\n");
		out.close();

		// read from file
		Configuration.the();

		// test group
		Log.consoleOut(Configuration.the().toString());

	}

	@Test
	public void testRegistering() {
		// register object A with one group

		// register object B with two groups

		// unregister object A

	}

}
