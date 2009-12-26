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
import pleocmd.pipe.Pipe;
import pleocmd.pipe.val.DummyValue;
import pleocmd.pipe.val.Value;

public final class Data extends AbstractList<Value> {

	public static final byte PRIO_DEFAULT = 0;
	public static final byte PRIO_LOWEST = -99;
	public static final byte PRIO_HIGHEST = 99;

	public static final long TIME_NOTIME = -1;

	private final List<Value> values;

	private final Data parent;

	private final byte priority;

	private final long time;

	/**
	 * Creates a new {@link Data} object where all fields can be set
	 * individually.
	 * 
	 * @param values
	 *            list of {@link Value}
	 * @param parent
	 *            the parent which was the cause while this {@link Data} has
	 *            been created - may be <b>null</b>
	 * @param priority
	 *            priority of the new {@link Data} (must be between
	 *            {@link #PRIO_LOWEST} and {@link #PRIO_HIGHEST}) - if
	 *            {@link #PRIO_DEFAULT}, it will inherit the parent's priority
	 *            if a parent is defined
	 * @param time
	 *            time at which this {@link Data} has been created relative to
	 *            the start of the {@link Pipe} - if {@link #TIME_NOTIME} it
	 *            will inherit the parent's time if a parent is defined
	 */
	public Data(final List<Value> values, final Data parent,
			final byte priority, final long time) {
		this.values = values;
		this.parent = parent;
		this.priority = priority == PRIO_DEFAULT && parent != null ? parent
				.getPriority() : priority;
		this.time = time == TIME_NOTIME && parent != null ? parent.getTime()
				: time;
		Log.detail("New Data created: %s (parent: '%s' priority: %d time: %d)",
				this, parent, priority, time);
	}

	/**
	 * Creates a new {@link Data} object where all fields will be inherited from
	 * the parent if one is defined or set to defaults otherwise.
	 * 
	 * @param values
	 *            list of {@link Value}
	 * @param parent
	 *            the parent which was the cause while this {@link Data} has
	 *            been created - may be <b>null</b>
	 */
	public Data(final List<Value> values, final Data parent) {
		this.values = values;
		this.parent = parent;
		priority = parent != null ? parent.getPriority() : PRIO_DEFAULT;
		time = parent != null ? parent.getTime() : TIME_NOTIME;
		Log.detail("New Data created: %s (parent: '%s')", this, parent);
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
	// CS_IGNORE_NEXT unchecked exception thrown intentionally
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

	public Data getParent() {
		return parent;
	}

	public byte getPriority() {
		return priority;
	}

	public long getTime() {
		return time;
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
		return new DataBinaryConverter(in).createDataFromFields();
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
		return new DataAsciiConverter(in).createDataFromFields();
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

	public Data getRoot() {
		return parent == null ? this : parent.getRoot();
	}

}
