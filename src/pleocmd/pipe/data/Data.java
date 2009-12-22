package pleocmd.pipe.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

import pleocmd.Log;
import pleocmd.pipe.val.DummyValue;
import pleocmd.pipe.val.Value;

public final class Data extends AbstractList<Value> {

	public static final byte PRIO_DEFAULT = 0;
	public static final byte PRIO_LOWEST = -99;
	public static final byte PRIO_HIGHEST = 99;

	private final List<Value> values;

	private final byte priority;

	public Data(final List<Value> values, final byte priority) {
		this.values = values;
		this.priority = priority;
		Log.detail("New Data created: %s", this);
	}

	public Data(final Data data, final byte priority) {
		Log.detail("Cloned Data '%s' and changed to %d", data, priority);
		values = data.values;
		this.priority = priority;
	}

	/**
	 * Returns the {@link Value} at the given position.
	 * 
	 * @param index
	 *            index of the {@link Value} to return
	 * @return {@link Value} at this position
	 * @throws IndexOutOfBoundsException
	 *             if the given index if invalid
	 */
	@Override
	public Value get(final int index) throws IndexOutOfBoundsException {
		if (index < 0 || index >= values.size())
			throw new IndexOutOfBoundsException(String.format(
					"Argument %d does not exist in data '%s'", index, this));
		return values.get(index);
	}

	/**
	 * Returns the {@link Value} at the given position or a {@link DummyValue}
	 * if the position is invalid.<br>
	 * Never throws an {@link IndexOutOfBoundsException}.
	 * 
	 * @param index
	 *            index of the {@link Value} to return
	 * @return {@link Value} at this position or {@link DummyValue}
	 */
	public Value getSafe(final int index) {
		return index < 0 || index >= values.size() ? new DummyValue() : values
				.get(index);
	}

	@Override
	public int size() {
		return values.size();
	}

	public byte getPriority() {
		return priority;
	}

	/**
	 * Creates a new {@link Data} object from a {@link DataInput}.
	 * 
	 * @param in
	 *            Input Stream with binary data
	 * @return new {@link Data} with a list of {@link Value}s read from stream
	 * @throws IOException
	 *             if data could not be read from {@link DataInput}, is of an
	 *             invalid type or is of an invalid format for its type
	 */
	public static Data createFromBinary(final DataInput in) throws IOException {
		final DataBinaryConverter dbc = new DataBinaryConverter(in);
		return new Data(dbc.getValues(), dbc.getPriority());
	}

	/**
	 * Creates a new {@link Data} object from a {@link DataInput}.
	 * 
	 * @param in
	 *            Input Stream with text data in ISO-8859-1 encoding
	 * @return new {@link Data} with a list of {@link Value}s read from stream
	 * @throws IOException
	 *             if data could not be read from {@link DataInput}, is of an
	 *             invalid type or is of an invalid format for its type
	 * @see DataAsciiConverter
	 */
	public static Data createFromAscii(final DataInput in) throws IOException {
		final DataAsciiConverter dac = new DataAsciiConverter(in);
		return new Data(dac.getValues(), dac.getPriority());
	}

	/**
	 * Creates a new {@link Data} object from a {@link String}.
	 * 
	 * @param string
	 *            {@link String} to read the data block from (optionally with a
	 *            line-break)
	 * @return new {@link Data} with a list of {@link Value}s read from
	 *         {@link String}
	 * @throws IOException
	 *             if data could not be read from {@link String}, is of an
	 *             invalid type or is of an invalid format for its type
	 * @see DataAsciiConverter
	 */
	public static Data createFromAscii(final String string) throws IOException {
		return createFromAscii(new DataInputStream(new ByteArrayInputStream(
				(string + '\n').getBytes("ISO-8859-1"))));
	}

	public void writeToBinary(final DataOutput out) throws IOException {
		new DataBinaryConverter(this).writeToBinary(out);
	}

	public void writeToAscii(final DataOutput out, final boolean writeLF)
			throws IOException {
		new DataAsciiConverter(this).writeToAscii(out, writeLF);
	}

	@Override
	public String toString() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(128);
		try {
			writeToAscii(new DataOutputStream(out), false);
			return out.toString("ISO-8859-1");
		} catch (final IOException e) {
			Log.error(e);
			return String.format("S:%1", e.getMessage());
		}
	}

}
