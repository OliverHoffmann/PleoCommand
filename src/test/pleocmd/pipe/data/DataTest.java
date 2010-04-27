package pleocmd.pipe.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pleocmd.Log;
import pleocmd.Testcases;
import pleocmd.exc.FormatException;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class DataTest extends Testcases {

	@Test
	public void testConversion() throws IOException, FormatException {

		final List<Value> values = new ArrayList<Value>();
		final Data d1 = new Data(values, null);
		testAsciiConversion(d1);
		try {
			testBinaryConversion(d1);
			fail("IOException not thrown");
		} catch (final IOException e) {
			assertTrue(e.toString(), e.getMessage().contains("without any"));
		}
		Log.consoleOut("Tested conversion of Data #1: '%s'", d1);

		values.clear();
		values.add(Value.createForType(ValueType.Int8).set("0"));
		final Data d2 = new Data(values, null, Data.PRIO_HIGHEST, 12345000);
		testAsciiConversion(d2);
		testBinaryConversion(d2);
		Log.consoleOut("Tested conversion of Data #2: '%s'", d2);

		values.clear();
		values.add(Value.createForType(ValueType.Int8).set("-12"));
		values.add(Value.createForType(ValueType.NullTermString).set(""));
		values.add(Value.createForType(ValueType.Float64).set("0.0000000012"));
		values.add(Value.createForType(ValueType.Data).set(
				new String(new byte[] { 12, -7, 127, 0, -128, 20 })));
		values.add(Value.createForType(ValueType.NullTermString).set(
				"äöü ÄÖÜ ß"));
		values.add(Value.createForType(ValueType.NullTermString).set(
				"and yet another string"));
		final Data d3 = new Data(values, null, Data.PRIO_DEFAULT, 0);
		testAsciiConversion(d3);
		testBinaryConversion(d3);
		Log.consoleOut("Tested conversion of Data #3: '%s'", d3);

		values.clear();
		assertEquals("not shallow copied:", 6, d3.size());

		final Data d4 = Data.createFromAscii("[]");
		Log.consoleOut("Tested string creation of Data #4: '%s'", d4);

		final Data d5 = Data.createFromAscii("[]|||");
		Log.consoleOut("Tested string creation of Data #5: '%s'", d5);

		final Data d6 = Data.createFromAscii("[T5000ms]||Sx:00|Ix:3337");
		Log.consoleOut("Tested string creation of Data #6: '%s'", d6);

		try {
			Data.createFromAscii("ß");
			fail("IOException not thrown");
		} catch (final IOException e) {
			assertTrue(e.toString(), e.getMessage().contains(
					"Invalid character"));
		}

		try {
			Data.createFromAscii("1|2|\0");
			fail("IOException not thrown");
		} catch (final IOException e) {
			assertTrue(e.toString(), e.getMessage().contains(
					"Invalid character"));
		}

		try {
			Data.createFromAscii("Bx:F850BEXYA0");
			fail("IOException not thrown");
		} catch (final IOException e) {
			assertTrue(e.toString(), e.getMessage().endsWith("9: 0x58"));
		}

		try {
			Data.createFromAscii("Bx:F850B");
			fail("IOException not thrown");
		} catch (final IOException e) {
			assertTrue(e.toString(), e.getMessage().endsWith(": 5"));
		}

		Log.consoleOut("Data testing done");
	}

	private void testBinaryConversion(final Data data) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.writeToBinary(new DataOutputStream(out));

		final ByteArrayInputStream in = new ByteArrayInputStream(out
				.toByteArray());
		final Data newData = Data.createFromBinary(new DataInputStream(in));
		assertEquals(data, newData);
	}

	private void testAsciiConversion(final Data data) throws IOException,
			FormatException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.writeToAscii(new DataOutputStream(out), true);

		final ByteArrayInputStream in = new ByteArrayInputStream(out
				.toByteArray());
		final Data newData = Data.createFromAscii(new DataInputStream(in));
		assertEquals(data, newData);
	}

}
