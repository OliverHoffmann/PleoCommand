package test.pleocmd.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.cfg.ConfigBoolean;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.ConfigCollection;
import pleocmd.cfg.ConfigDataMap;
import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigInt;
import pleocmd.cfg.ConfigItem;
import pleocmd.cfg.ConfigLong;
import pleocmd.cfg.ConfigMap;
import pleocmd.cfg.ConfigPath;
import pleocmd.cfg.ConfigString;
import pleocmd.cfg.ConfigValue;
import pleocmd.cfg.ConfigCollection.Type;
import pleocmd.cfg.ConfigPath.PathType;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.Layouter;
import test.pleocmd.Testcases;

public final class ConfigValueTest extends Testcases {

	private enum TestEnum {
		Test1, Test2, Test3, Test4;
	}

	private final ConfigString cfgStrS = new ConfigString("Test-String", false);
	private final ConfigString cfgStrM = new ConfigString("Test-String", true);
	private final ConfigInt cfgInt = new ConfigInt("Test-Int", 5, 5, 12);
	private final ConfigDouble cfgFloat = new ConfigDouble("Test-Float", 0.4,
			0.3, 0.5);

	private final ConfigMap<String, String> cfgMap = new ConfigMap<String, String>(
			"Test-DataSeq") {
		@Override
		protected String createKey(final String keyAsString)
				throws ConfigurationException {
			return keyAsString;
		}

		@Override
		protected String createValue(final String valueAsString)
				throws ConfigurationException {
			return valueAsString;
		}

		@Override
		protected String convertKey(final String key) {
			return key;
		}

		@Override
		protected String convertValue(final String value) {
			return value;
		}

		@Override
		protected void contentChanged() {
			// nothing to do, we have no GUI here
		}

		@Override
		public boolean insertGUIComponents(final Layouter lay) {
			// nothing to do, we have no GUI here
			return true;
		}

		@Override
		public void setFromGUIComponents() {
			// nothing to do, we have no GUI here
		}

	};

	private final ConfigItem<String> cfgItem = new ConfigItem<String>(
			"Test-List", "A", Arrays.asList(new String[] { "A", "B", "C", "D",
					"E" }));

	private final ConfigEnum<TestEnum> cfgEnum = new ConfigEnum<TestEnum>(
			TestEnum.class);

	private final ConfigPath cfgFileRead = new ConfigPath("Test-ReadFile",
			PathType.FileForReading);

	private final ConfigPath cfgFileWrite = new ConfigPath("Test-WriteFile",
			PathType.FileForWriting);

	private final ConfigPath cfgDir = new ConfigPath("Test-Directory",
			PathType.Directory);

	private final ConfigBounds cfgBounds = new ConfigBounds("Test-Bounds");

	private final ConfigBoolean cfgBool = new ConfigBoolean("Test-Boolean");

	// CS_IGNORE_NEXT very long but well-structured test-case
	@Test
	public void testAllConfigValues() throws ConfigurationException,
			IOException {
		cfgStrS.setContent("Some Single Lined String");
		try {
			cfgStrS.setContent("Some\nMultiple\nLined\nString");
			fail("single-lines ConfigString can be set to multiple lines");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("line-feed"));
		}
		Log.consoleOut("Tested ConfigString (single-lined)");

		cfgStrM.setContent("Some Single Lined String");
		cfgStrM.setContent("Some\nMultiple\nLined\nString");
		Log.consoleOut("Tested ConfigString (multi-lined)");

		cfgInt.setContent(5);
		try {
			cfgInt.setContent(30);
			fail("ConfigInt can be set out of bounds");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("not between"));
		}
		Log.consoleOut("Tested ConfigInt");

		cfgFloat.setContent(0.5);
		try {
			cfgFloat.setContent(0.);
			fail("ConfigFloat can be set out of bounds");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("not between"));
		}
		Log.consoleOut("Tested ConfigDouble");

		cfgMap.clearContent();
		cfgMap.createContent("foo");
		assertTrue("New created content not empty", cfgMap.getContent("foo")
				.isEmpty());
		cfgMap.addContent("foo", "1");
		cfgMap.addContent("foo", "2");
		assertEquals("addContent() failed", 2, cfgMap.getContent("foo").size());
		cfgMap.setContent("foo", new ArrayList<String>());
		assertTrue("setContent() failed", cfgMap.getContent("foo").isEmpty());
		Log.consoleOut("Tested ConfigMap");

		final int size = cfgItem.getIdentifiers().size();
		cfgItem.setContent("D");
		cfgItem.setContent("X");
		assertEquals("List of identifiers changeable via setContent()", size,
				cfgItem.getIdentifiers().size());
		Log.consoleOut("Tested ConfigItem");

		cfgEnum.setContent("Test3");
		try {
			cfgEnum.setContent("Test999");
			fail("ConfigEnum can be set any string");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage()
					.contains("Invalid constant"));
		}
		Log.consoleOut("Tested ConfigEnum");

		final File fileDir = File.listRoots()[0];
		final File fileMiss = File.createTempFile("CVTest", null);
		fileMiss.delete();
		final File fileTemp = File.createTempFile("CVTest", null);
		fileTemp.deleteOnExit();

		cfgFileRead.setContent(fileTemp);
		try {
			cfgFileRead.setContent(fileMiss);
			fail("ConfigPath in Read-File mode can be set to a missing file");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("not exist"));
		}
		try {
			cfgFileRead.setContent(fileDir);
			fail("ConfigPath in Read-File mode can be set to a directory");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("is a directory"));
		}
		Log.consoleOut("Tested ConfigPath in Read-File mode");

		cfgFileWrite.setContent(fileTemp);
		cfgFileWrite.setContent(fileMiss);
		try {
			cfgFileWrite.setContent(fileDir);
			fail("ConfigPath in Write-File mode can be set to a directory");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("is a directory"));
		}
		Log.consoleOut("Tested ConfigPath in Write-File mode");

		cfgDir.setContent(fileDir);
		try {
			cfgDir.setContent(fileTemp);
			fail("ConfigPath in Directory mode can be set to a file");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("not a directory"));
		}
		try {
			cfgDir.setContent(fileMiss);
			fail("ConfigPath in Directory mode can be set to a missing file");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("not exist"));
		}
		Log.consoleOut("Tested ConfigPath in Directory mode");
		fileTemp.delete();

		cfgBounds.setContent(new Rectangle());
		cfgBounds.setFromString("0,0/0x0");
		cfgBounds.setFromString(" 0 , 0 / 0 x 0 ");
		try {
			cfgBounds.setFromString("0/0x0,0");
			fail("ConfigBounds can be set via an invalid format");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("Invalid format"));
		}
		try {
			cfgBounds.setFromString("0,/0x0");
			fail("ConfigBounds can be set via an invalid format");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("Invalid number"));
		}
		Log.consoleOut("Tested ConfigBounds");

		cfgBool.setContent(true);
		cfgBool.setFromString("true");
		cfgBool.setFromString("false");
		try {
			cfgBool.setFromString("TRUE");
			fail("ConfigBoolean can be set via an invalid boolean string");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("Invalid boolean"));
		}
		try {
			cfgBool.setFromString("falseXYZ");
			fail("ConfigBoolean can be set via an invalid boolean string");
		} catch (final ConfigurationException e) {
			assertTrue(e.toString(), e.getMessage().contains("Invalid boolean"));
		}
		Log.consoleOut("Tested ConfigBoolean");

		Log.consoleOut("Checked all ConfigValue implementations");
	}

	private void compareIdentifier(final ConfigValue cv,
			final Class<? extends ConfigValue> expected) {
		Log.consoleOut("Testing class '%s'", cv.getClass().getName());
		final String ident = cv.getIdentifier();
		final ConfigValue cvFromIdent = ConfigValue.createValue(ident, "foo",
				true);
		Log.consoleOut("Got identifier '%s' which constructs '%s' which "
				+ "in return has identifier '%s'", ident, cvFromIdent
				.getClass().getName(), cvFromIdent.getIdentifier());
		if (expected != null)
			assertEquals("Wrong class returned by createValue():", expected,
					cvFromIdent.getClass());
		assertEquals("Newly constructed by createValue() has "
				+ "other identifier:", ident, cvFromIdent.getIdentifier());
	}

	@Test
	public void testIdentifiers() {
		compareIdentifier(new ConfigBoolean("foo"), ConfigBoolean.class);
		compareIdentifier(new ConfigBounds("foo"), ConfigBounds.class);
		compareIdentifier(new ConfigDataMap("foo"), ConfigString.class);
		compareIdentifier(new ConfigEnum<TestEnum>("foo", TestEnum.class),
				ConfigString.class);
		compareIdentifier(new ConfigDouble("foo"), ConfigDouble.class);
		compareIdentifier(new ConfigInt("foo"), ConfigInt.class);
		compareIdentifier(new ConfigItem<String>("foo", true, Arrays
				.asList(new String[] { "FOO" })), ConfigString.class);
		compareIdentifier(new ConfigLong("foo"), ConfigLong.class);
		compareIdentifier(new ConfigPath("foo", PathType.Directory),
				ConfigPath.class);
		compareIdentifier(new ConfigPath("foo", PathType.FileForReading),
				ConfigPath.class);
		compareIdentifier(new ConfigPath("foo", PathType.FileForWriting),
				ConfigPath.class);
		compareIdentifier(new ConfigString("foo", true), ConfigString.class);
		compareIdentifier(new ConfigString("foo", false), ConfigString.class);
		compareIdentifier(new ConfigCollection<String>("foo", Type.List) {
			@Override
			protected String createItem(final String itemAsString)
					throws ConfigurationException {
				return itemAsString;
			}
		}, null);
		compareIdentifier(new ConfigCollection<String>("foo", Type.Set) {
			@Override
			protected String createItem(final String itemAsString)
					throws ConfigurationException {
				return itemAsString;
			}
		}, null);
	}
}
