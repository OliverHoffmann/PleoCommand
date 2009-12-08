package pleocmd.pipe.val;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
	public String toString() {
		return null;
	}

}
