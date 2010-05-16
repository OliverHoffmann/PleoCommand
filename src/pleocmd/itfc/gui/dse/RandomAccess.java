package pleocmd.itfc.gui.dse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface RandomAccess {

	long length() throws IOException;

	long getFilePointer() throws IOException;

	void seek(long pos) throws IOException;

	void close() throws IOException;

	int read() throws IOException;

	void write(int b) throws IOException;

	DataInput getDataInput();

	DataOutput getDataOutput();

}
