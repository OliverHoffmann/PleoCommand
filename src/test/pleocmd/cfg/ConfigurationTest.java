package test.pleocmd.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.cfg.ConfigString;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import test.pleocmd.Testcases;

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
		public void configurationRead() {
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
		final Configuration config = new Configuration();
		Log.consoleOut("Testing user-created group: %s", description);
		Log.consoleOut(group.toString());
		final DummyObject dummy = new DummyObject(group);
		config.registerConfigurableObject(dummy, group.getName());

		// write to file
		final File file = File.createTempFile("PleoCommand-ConfigTest", null);
		file.deleteOnExit();
		Log.consoleOut("Test writing to file");
		config.writeToFile(file);

		// print file
		Log.consoleOut("Resulting configuration file:");
		final BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		while ((line = in.readLine()) != null)
			Log.consoleOut("$" + line);
		in.close();
		Log.consoleOut("End of configuration file");

		// read from file
		Log.consoleOut("Test reading from file");
		config.readFromDefaultFile();

		config.unregisterConfigurableObject(dummy);
	}

	@Test
	public void testCreateWriteAndRead() throws IOException,
			ConfigurationException {
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

	private Configuration testUserFile(final String description,
			final String[] content) throws IOException, ConfigurationException {
		Log.consoleOut("");
		Log.consoleOut("Testing user-created file: %s", description);
		final Configuration config = new Configuration();

		// create file
		final File file = File.createTempFile("PleoCommand-ConfigTest", null);
		file.deleteOnExit();
		final FileWriter out = new FileWriter(file);
		for (final String s : content) {
			out.write(s);
			out.write('\n');
		}
		out.close();

		// read from file
		config.readFromFile(file);

		// test group
		final StringWriter sw = new StringWriter();
		config.writeToWriter(sw, null);
		for (final String s : sw.toString().split("\n"))
			Log.consoleOut("$" + s);

		return config;
	}

	private void assertGroupCount(final Configuration cfg, final int count) {
		assertEquals("Number of groups is wrong:", count, cfg
				.getGroupsUnassigned().size());
	}

	private void assertGroupSize(final Configuration cfg, final String name,
			final int size) {
		assertEquals("Number of values in group is wrong:", size, cfg
				.getGroupUnassignedSafe(name).getSize());
	}

	private void assertValue(final Configuration cfg, final String group,
			final String label, final String content) {
		assertEquals("Content of value in group is wrong:", content, cfg
				.getGroupUnassignedSafe(group).get(label,
						new ConfigString(label, false)).asString());
	}

	@Test
	public void testReadUsercreatedFiles() throws IOException,
			ConfigurationException {
		Configuration cfg = testUserFile("Empty Config", new String[] {});
		assertGroupCount(cfg, 0);

		cfg = testUserFile("Simple Config", new String[] { "[Test]", "a:foo",
				"b:20", "c:{", "1", "2", "3", "}" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "Test", 3);

		cfg = testUserFile("Config with List", new String[] { "[foo]",
				"list:{", "[no group]", "}" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "foo", 1);

		cfg = testUserFile("Invalid identifier", new String[] { "[group]",
				"val<xy>:20", "next:5" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "group", 2);
		assertValue(cfg, "group", "val", "20");
		assertValue(cfg, "group", "next", "5");

		cfg = testUserFile("Wrong identifier", new String[] { "[group]",
				"val<list>:20", "next:5" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "group", 2);
		assertValue(cfg, "group", "val", "[20]");
		assertValue(cfg, "group", "next", "5");

		cfg = testUserFile("Wrong identifier", new String[] { "[group]",
				"val<bool>:20", "next:5" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "group", 2);
		assertValue(cfg, "group", "val", "false");
		assertValue(cfg, "group", "next", "5");

		cfg = testUserFile("Data out of range", new String[] { "[group]",
				"val<bool>:20", "next:5" });
		assertGroupCount(cfg, 1);
		assertGroupSize(cfg, "group", 2);
		assertValue(cfg, "group", "val", "false");
		assertValue(cfg, "group", "next", "5");
	}

	@Test
	public void testRegistering() {
		// register object A with one group

		// register object B with two groups

		// unregister object A

		// TODO ENH ConfigurationTest
	}

}
