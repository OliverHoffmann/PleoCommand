package pleocmd.itfc.gui.dse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;

public final class RandomAccessFile extends java.io.RandomAccessFile implements
		RandomAccess {

	public RandomAccessFile(final File file) throws FileNotFoundException {
		super(file, "r");
	}

	@Override
	public DataInput getDataInput() {
		return this;
	}

	@Override
	public DataOutput getDataOutput() {
		return this;
	}

}
