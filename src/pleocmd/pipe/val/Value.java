package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import pleocmd.exc.InternalException;
import pleocmd.pipe.data.Data;

/**
 * Contains one (of possibly more) information from a {@link Data}.<br>
 * This class is immutable as long as {@link #set(String)} is not called
 * directly from the user.
 * 
 * @author oliver
 */
public abstract class Value {

	private ValueType type;

	/**
	 * Creates a new {@link Value}.
	 * 
	 * @param type
	 *            the type of the new {@link Value}. Must fit to the
	 *            implementing class.
	 * @see #createForType(ValueType)
	 */
	protected Value(final ValueType type) {
		this.type = type;
	}

	/**
	 * @return the {@link ValueType} of this {@link Value}
	 */
	public final ValueType getType() {
		return type;
	}

	/**
	 * Changes the {@link ValueType} without changing the value itself.<br>
	 * Only used during implementations of {@link #compact()}.
	 * 
	 * @param newType
	 *            new {@link ValueType} - must be compatible with the current
	 *            one !!!
	 */
	protected final void changeType(final ValueType newType) {
		type = newType;
	}

	// CS_IGNORE_BEGIN This 4 methods need to be overridable

	/**
	 * @return the contents of this {@link Value} as a {@link Long}.
	 * @throws UnsupportedOperationException
	 *             if this {@link Value} can not be represented as a
	 *             {@link Long}
	 */
	public long asLong() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a long integer value");
	}

	/**
	 * @return the contents of this {@link Value} as a {@link Double}.
	 * @throws UnsupportedOperationException
	 *             if this {@link Value} can not be represented as a
	 *             {@link Double}
	 */
	public double asDouble() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a double floating point value");
	}

	/**
	 * @return the contents of this {@link Value} as a {@link String}.
	 * @throws UnsupportedOperationException
	 *             if this {@link Value} can not be represented as a
	 *             {@link String}
	 */
	public String asString() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a string value");
	}

	/**
	 * @return the contents of this {@link Value} as an array of {@link Byte}s.
	 * @throws UnsupportedOperationException
	 *             if this {@link Value} can not be represented as an array of
	 *             {@link Byte}s
	 */
	public byte[] asByteArray() {
		throw new UnsupportedOperationException(
				"Cannot convert data's argument to a byte array value");
	}

	// CS_IGNORE_END

	/**
	 * Creates a new {@link Value} based on a given {@link ValueType}.
	 * 
	 * @param type
	 *            {@link ValueType} of the new {@link Value}
	 * @return new {@link Value}
	 */
	public static Value createForType(final ValueType type) {
		switch (type) {
		case Int8:
		case Int32:
		case Int64:
			return new IntValue(type);
		case Float32:
		case Float64:
			return new FloatValue(type);
		case UTFString:
		case NullTermString:
			return new StringValue(type);
		case Data:
			return new BinaryValue(type);
		default:
			throw new InternalException(type);
		}
	}

	static ValueType detectFromTypeChar(final char c) {
		if (c == IntValue.TYPE_CHAR) return IntValue.RECOMMENDED_TYPE;
		if (c == FloatValue.TYPE_CHAR) return FloatValue.RECOMMENDED_TYPE;
		if (c == StringValue.TYPE_CHAR) return StringValue.RECOMMENDED_TYPE;
		if (c == BinaryValue.TYPE_CHAR) return BinaryValue.RECOMMENDED_TYPE;
		return null;
	}

	static int getAsciiTypeChar(final Value value) {
		try {
			return (Character) value.getClass().getDeclaredField("TYPE_CHAR")
					.get(null);
		} catch (final Throwable t) {
			// CS_IGNORE_PREV Catch everything that may go wrong here
			throw new InternalException("Cannot access field TYPE_CHAR "
					+ "of a subclass of Value: %s", t);
		}
	}

	/**
	 * Should only be called from {@link DataBinaryConverter}.
	 * 
	 * @param in
	 *            from which to read the {@link Value} in binary form.
	 * @return number of bytes read from {@link DataInput}
	 * @throws IOException
	 *             if reading failed
	 */
	abstract int readFromBinary(final DataInput in) throws IOException;

	abstract int writeToBinary(final DataOutput out) throws IOException;

	/**
	 * Should only be called from {@link DataAsciiConverter}.
	 * 
	 * @param in
	 *            from which to read the {@link Value} in ISO-8859-1 form
	 * @param len
	 *            the length in bytes of the valid part of "in"
	 * @throws IOException
	 *             if reading failed
	 */
	abstract void readFromAscii(final byte[] in, int len) throws IOException;

	abstract int writeToAscii(DataOutput out) throws IOException;

	@Override
	public abstract String toString();

	abstract boolean mustWriteAsciiAsHex();

	/**
	 * Must only be used from test cases.
	 * 
	 * @param content
	 *            the new content
	 * @return the {@link Value} itself
	 * @throws IOException
	 *             if the new content is invalid
	 */
	public abstract Value set(final String content) throws IOException;

	@Override
	public abstract boolean equals(final Object obj);

	@Override
	public abstract int hashCode();

	public void compact() {
		// do nothing
	}

}
