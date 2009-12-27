package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import pleocmd.pipe.data.Data;

/**
 * A placeholder for a {@link Value} on a position in a {@link Data} which does
 * not exist. Just returns some safe default value for every conversion
 * operation and never fails during conversion.
 * 
 * @see Data#getSafe(int)
 * @author oliver
 */
public final class DummyValue extends Value {

	public DummyValue() {
		super(ValueType.Data);
	}

	@Override
	public void readFromBinary(final DataInput in) throws IOException {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public void writeToBinary(final DataOutput out) throws IOException {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public void readFromAscii(final byte[] in, final int len)
			throws IOException {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public void writeToAscii(final DataOutput out) throws IOException {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public boolean mustWriteAsciiAsHex() {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public String toString() {
		return null;
	}

	@Override
	public long asLong() {
		return 0;
	}

	@Override
	public double asDouble() {
		return .0;
	}

	@Override
	public String asString() {
		return "";
	}

	@Override
	public byte[] asByteArray() {
		return new byte[0];
	}

	@Override
	public Value set(final String content) {
		throw new UnsupportedOperationException("This is a dummy value");
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof DummyValue;
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
