package pleocmd.pipe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.Log;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

/**
 * Helper class for converting {@link Data} objects from and to binary.
 * <p>
 * Data must have the following format:<br>
 * <table>
 * <tr>
 * <th align=left>Description</th>
 * <th align=left>Size</th>
 * </tr>
 * <tr>
 * <td>Flags</td>
 * <td>5 Bits</td>
 * </tr>
 * <tr>
 * <td>Number of Fields (N-1)</td>
 * <td>3 Bits</td>
 * </tr>
 * <tr>
 * <td>Type of Field 1</td>
 * <td>3 Bits</td>
 * </tr>
 * <tr>
 * <td>...</td>
 * </tr>
 * <tr>
 * <td>Type of Field 8</td>
 * <td>3 Bits</td>
 * </tr>
 * <tr>
 * <td>Additional Header Data</td>
 * <td>Depends on Flags (may be 0)</td>
 * </tr>
 * <tr>
 * <td>Content of Field 1</td>
 * <td>Depends on Type 1</td>
 * </tr>
 * <tr>
 * <td>...</td>
 * </tr>
 * <tr>
 * <td>Content of Field N</td>
 * <td>Depends on Type N</td>
 * </tr>
 * </table>
 * <table>
 * <tr>
 * <th align=left>Type-Index</th>
 * <th align=left>Description</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>8-Bit signed Integer</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>32-Bit signed Integer</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>64-Bit signed Integer</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>32-Bit Floating Point (Single)</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>64-Bit Floating Point (Double)</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>UTF-8 encoded String</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>Null-terminated (Ascii) String</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>4 Byte Length + arbitrary Data</td>
 * </tr>
 * </table>
 * <p>
 * <table>
 * <tr>
 * <th align=left>Flag-Bit</th>
 * <th align=left>Description</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>Priority-Byte appended:<br>
 * 1 Byte for Priority is appended after the 4 Header-Bytes</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>reserved, must be 0</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>reserved, must be 0</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>reserved, must be 0</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>reserved, must be 0</td>
 * </tr>
 * </table>
 * 
 * @author oliver
 */
public final class DataBinaryConverter {

	public static final int FLAG_PRIORITY = 0x01;
	public static final int FLAG_RESERVED_1 = 0x02;
	public static final int FLAG_RESERVED_2 = 0x04;
	public static final int FLAG_RESERVED_3 = 0x08;
	public static final int FLAG_RESERVED_4 = 0x10;
	private static final int FLAG_RESERVED_MASK = 0x1E;

	private byte priority;

	private final List<Value> values;

	/**
	 * Creates a new {@link DataBinaryConverter} that wraps an existing
	 * {@link Data} object.
	 * 
	 * @param data
	 *            {@link Data} to read from
	 */
	public DataBinaryConverter(final Data data) {
		priority = data.getPriority();
		values = data;
	}

	/**
	 * Creates a new {@link DataBinaryConverter} and sets all its fields
	 * according to the binary representation of a {@link Data} in the
	 * {@link DataInput}.
	 * 
	 * @param in
	 *            Input Stream with binary data
	 * @throws IOException
	 *             if data could not be read from {@link DataInput}, is of an
	 *             invalid type or is of an invalid format for its type
	 */
	public DataBinaryConverter(final DataInput in) throws IOException {
		final int hdr = in.readInt();
		final int flags = hdr >> 27 & 0x1F;
		final int cnt = (hdr >> 24 & 0x07) + 1;
		priority = Data.PRIO_DEFAULT;
		if ((flags & FLAG_PRIORITY) != 0) {
			priority = in.readByte();
			if (priority < Data.PRIO_LOWEST || priority > Data.PRIO_HIGHEST)
				throw new IOException(String.format(
						"Priority is out of range: %d not between "
								+ "%d and %d", priority, Data.PRIO_LOWEST,
						Data.PRIO_HIGHEST));
		}
		if ((flags & FLAG_RESERVED_MASK) != 0)
			throw new IOException(String.format(
					"Reserved flags have been set: 0x%02X", flags));
		Log.detail("Header is 0x%08X => flags: 0x%02X count: %d", hdr, flags,
				cnt);
		values = new ArrayList<Value>(cnt);
		for (int i = 0; i < cnt; ++i) {
			final ValueType type = ValueType.values()[hdr >> i * 3 & 0x07];
			assert type.getID() == (hdr >> i * 3 & 0x07);
			final Value val = Value.createForType(type);
			if (val == null)
				throw new IOException(
						"Internal error: Type out of range 0 - 0x07");
			val.readFromBinary(in);
			values.add(val);
		}
	}

	public byte getPriority() {
		return priority;
	}

	public List<Value> getValues() {
		return values;
	}

	public void writeToBinary(final DataOutput out) throws IOException {
		Log.detail("Writing data to binary output stream");
		if (values.size() > 8)
			throw new IOException(
					"Cannot handle more than 8 values for binary data");

		// write header
		int flags = 0;
		if (priority != Data.PRIO_DEFAULT) flags |= FLAG_PRIORITY;
		int hdr = (flags & 0x1F) << 27 | (values.size() - 1 & 0x07) << 24;
		for (int i = 0; i < values.size(); ++i)
			hdr |= (values.get(i).getType().getID() & 0x07) << i * 3;
		out.writeInt(hdr);

		// write priority
		if (priority != Data.PRIO_DEFAULT) out.write(priority);

		// write the field content
		for (final Value value : values)
			value.writeToBinary(out);
	}

}
