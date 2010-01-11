package pleocmd.cfg;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;

public final class ConfigurationTest extends Testcases {

	class DummyObject implements ConfigurationInterface {

		private final Group group;

		public DummyObject(final Group group) {
			this.group = group;
		}

		@Override
		public Group getSkeleton(final String groupName) {
			return group;
		}

		@Override
		public void configurationAboutToBeChanged() {
			// nothing to do
		}

		@Override
		public void configurationChanged(final Group changedGroup) {
			// nothing to do
		}

		@Override
		public List<Group> configurationWriteback() {
			return Configuration.asList(group);
		}

	}

	private void testUserGroup(final String description, final Group group)
			throws ConfigurationException, IOException {
		Configuration.the().reset();
		Log.consoleOut("Testing user-created group: %s", description);
		Log.consoleOut(group.toString());
		final DummyObject dummy = new DummyObject(group);
		Configuration.the().registerConfigurableObject(dummy, group.getName());

		// write to file
		Log.consoleOut("Test writing to file");
		Configuration.the().writeToDefaultFile();

		// print file
		Log.consoleOut("Resulting configuration file:");
		final BufferedReader in = new BufferedReader(new FileReader(
				Configuration.getDefaultConfigFile()));
		String line;
		while ((line = in.readLine()) != null)
			Log.consoleOut("$" + line);
		in.close();
		Log.consoleOut("End of configuration file");

		// read from file
		Log.consoleOut("Test reading from file");
		Configuration.the().readFromDefaultFile();

		Configuration.the().unregisterConfigurableObject(dummy);
	}

	@Test
	public void testCreateWriteAndRead() throws IOException,
			ConfigurationException {
		Configuration.setDefaultConfigFile(File.createTempFile(
				"ConfigurationTest", null));
		Configuration.getDefaultConfigFile().deleteOnExit();

		testUserGroup("Empty Group", new Group("test"));

		final ConfigString cfg = new ConfigString("foo", true);
		cfg.setContent("Some\nMulti\nLine\nString");
		testUserGroup("Multi-Line Group", new Group("test").add(cfg));

		try {
			cfg.setContent("Some\0Nullterminated\0String");
			fail("ConfigurationException not thrown");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage()
					.contains("must not contain"));
		}

		try {
			cfg.setContent("Illegal\nLine:\n}\nCannot\nParse");
			fail("ConfigurationException not thrown");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains(
					"no line must equal"));
		}

	}

	private void testUserFile(final String description, final String[] content)
			throws IOException, ConfigurationException {
		Log.consoleOut("Testing user-created file: %s", description);

		// create file
		final FileWriter out = new FileWriter(Configuration
				.getDefaultConfigFile());
		for (final String s : content) {
			out.write(s);
			out.write('\n');
		}
		out.close();

		// read from file
		Configuration.the().readFromDefaultFile();

		// test group
		Log.consoleOut(Configuration.the().toString());
	}

	@Test
	public void testReadUsercreatedFiles() throws IOException,
			ConfigurationException {
		Configuration.setDefaultConfigFile(File.createTempFile(
				"ConfigurationTest", null));
		Configuration.getDefaultConfigFile().deleteOnExit();

		// create file
		testUserFile("Empty", new String[] {});
		testUserFile("Simple", new String[] { "[Test]", "a:foo", "b:20", "c:{",
				"1", "2", "3", "}" });
		testUserFile("List", new String[] { "[foo]", "list:{", "[no group]",
				"}" });
	}

	@Test
	public void testRegistering() {
		// register object A with one group

		// register object B with two groups

		// unregister object A

		// TODO
	}

}
