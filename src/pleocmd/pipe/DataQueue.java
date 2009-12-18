package pleocmd.pipe;

import java.io.IOException;
import java.util.List;

import pleocmd.Log;

public final class DataQueue {

	/**
	 * This array represents a ring buffer.
	 */
	private Data[] buffer;

	/**
	 * The position of the next byte to read from {@link #buffer}.
	 */
	private int readPos;

	/**
	 * The position of the next byte to write to {@link #buffer}.
	 */
	private int writePos;

	/**
	 * Only true if the cache has been closed, i.e. the remaining data in
	 * {@link #buffer} can still be read, but no new data can be put into the
	 * {@link #buffer} and if {@link #readPos} catches up {@link #writePos}
	 * {@link #get()} throws an {@link IOException}.
	 */
	private boolean closed;

	public DataQueue() {
		resetCache();
	}

	/**
	 * Appends a "close" to the ring buffer.<br>
	 * The remaining Data in the ring buffer can still be {@link #get()} but no
	 * new data can be {@link #put(Data)} into it. After no more data is
	 * available {@link #get()} throws an {@link IOException}.<br>
	 * Has no effect if the {@link DataQueue} is already closed.
	 */
	public synchronized void close() {
		closed = true;
	}

	/**
	 * Clears and (if currently closed) reopens the queue.<br>
	 * All data in the ring buffer not yet read will be lost.
	 */
	public void resetCache() {
		synchronized (this) {
			Log.detail("Resetting cache");
			buffer = new Data[1];// TODO bigger default
			readPos = 0;
			writePos = 0;
			closed = false;
		}
	}

	/**
	 * Reads one {@link Data} from the ring buffer.<br>
	 * Blocks until the {@link Data} is available.<br>
	 * Should only be called from the Output thread.
	 * 
	 * @return the next {@link Data} or <b>null</b> if no more {@link Data} is
	 *         available and the {@link DataQueue} has been {@link #close()}d
	 * @throws InterruptedException
	 *             if waiting for the next data block has been interrupted
	 */
	public Data get() throws InterruptedException {
		while (true) {
			// check if read catches up write?
			synchronized (this) {
				if (readPos != writePos) break;
				if (closed) return null;
			}
			// block until data available
			Thread.sleep(30);
		}
		synchronized (this) {
			final Data res = buffer[readPos];
			Log.detail(String.format("Read from %03d '%s'", readPos, res));
			readPos = (readPos + 1) % buffer.length;
			return res;
		}
	}

	/**
	 * Puts one {@link Data} into the ringbuffer, so it can be read by
	 * {@link #get()}.<br>
	 * Should only be called from the Input/Converter thread.
	 * 
	 * @param data
	 *            data to put into the ring buffer
	 * @throws IOException
	 *             if the {@link DataQueue} has been {@link #close()}d.
	 */
	public synchronized void put(final Data data) throws IOException {
		if (closed) throw new IOException("DataQueue is closed");
		buffer[writePos] = data;
		Log.detail(String.format("Put at %03d '%s'", writePos, data));
		writePos = (writePos + 1) % buffer.length;
		if (writePos == readPos) {
			// we need to increase our ring buffer:
			// we "insert space" between the current write and
			// read position so writePos stays the same while
			// readPos moves.
			final Data[] newbuf = new Data[buffer.length * 2];
			readPos += newbuf.length - buffer.length;
			Log.detail(String.format("Increased from %d to %d", buffer.length,
					newbuf.length));
			System.arraycopy(buffer, 0, newbuf, 0, writePos);
			System.arraycopy(buffer, writePos, newbuf, readPos, buffer.length
					- writePos);
			buffer = newbuf;
		}
	}

	/**
	 * Puts a series of {@link Data} into the ring buffer as an atomic operation
	 * (i.e. completely synchronized).
	 * 
	 * @param list
	 *            data to put into the ring buffer
	 * @throws IOException
	 *             if the queue has been closed
	 * @see #put(Data)
	 */
	public synchronized void put(final List<Data> list) throws IOException {
		for (final Data data : list)
			put(data);
	}

}
