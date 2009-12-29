package pleocmd.pipe.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;
import pleocmd.exc.PipeException;
import pleocmd.pipe.PipePart;
import pleocmd.pipe.cfg.ConfigPath.PathType;

public final class ConfigTest extends Testcases {

	private Config createConfig() {
		return new PipePart() { // CS_IGNORE just a test-case class

			{
				final Config cfg = getConfig();
				cfg.add(new ConfigString("Test-String", false));
				cfg.add(new ConfigInt("Test-Int", 5, 12));
				cfg.add(new ConfigFloat("Test-Float", 0.3, 0.5));
				cfg.add(new ConfigDataSeq("Test-DataSeq"));
				cfg.add(new ConfigList("Test-List", false, new String[] { "A",
						"B", "C", "D", "E" }));
				cfg.add(new ConfigPath("Test-Path", PathType.Directory));
				constructed();
			}

			@Override
			protected void init0() throws PipeException, IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			protected void configure0() throws PipeException, IOException {
				// just ignore
			}

			@Override
			protected void close0() throws PipeException, IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				return "Test-PipePart";
			}
		}.getConfig();
	}

	@Test
	public void testConfig() throws IOException {
		final Config cfg = createConfig();
		Log.consoleOut("Constructed new config '%s'", cfg);

		try {
			cfg.setOwner(null);
			fail("setOwner() can be called twice");
		} catch (final IllegalStateException e) {
			assertTrue(e.toString(), e.getMessage().contains("has already"));
		}

		try {
			cfg.add(new ConfigString("CanNeverBeAdded", false));
			fail("addV() allows adding after construction");
		} catch (final IllegalStateException e) {
			assertTrue(e.toString(), e.getMessage().startsWith("Cannot ad"));
		}

		Log.consoleOut("Checked owner management of config '%s'", cfg);

		assertEquals(6, cfg.size());

		final long l = ((ConfigInt) cfg.get(1)).getContent();
		assertTrue("ConfigInt out of bounds", l >= 5 && l <= 12);

		final double d = ((ConfigFloat) cfg.get(2)).getContent();
		assertTrue("ConfigFloat out of bounds", d >= 0.3 && d <= 0.5);

		cfg.get(1).setFromString("5");
		try {
			cfg.get(1).setFromString("30");
			fail("ConfigInt can be set out of bounds");
		} catch (final IndexOutOfBoundsException e) {
			assertTrue(e.toString(), e.getMessage().contains("must be between"));
		}

		cfg.get(2).setFromString("0.5");
		try {
			cfg.get(2).setFromString("0.2");
			fail("ConfigFloat can be set out of bounds");
		} catch (final IndexOutOfBoundsException e) {
			assertTrue(e.toString(), e.getMessage().contains("must be between"));
		}
		Log.consoleOut("Checked bounds of config '%s'", cfg);
	}

	@Test
	public void testReadWriteFiles() throws IOException, PipeException {
		final Config cfg = createConfig();

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		cfg.writeToFile(new OutputStreamWriter(out));
		Log.consoleOut("Written configuration '%s' to buffer", cfg);

		final ByteArrayInputStream in = new ByteArrayInputStream(out
				.toByteArray());
		cfg.readFromFile(new BufferedReader(new InputStreamReader(in)));
		Log.consoleOut("Read configuration '%s' from buffer", cfg);

		in.reset();
		cfg.readFromFile(new BufferedReader(new InputStreamReader(in)));
		Log.consoleOut("Read configuration '%s' from buffer", cfg);
	}
}
