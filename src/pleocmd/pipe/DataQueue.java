package pleocmd.pipe;

import java.io.IOException;
import java.util.List;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * Provides a FiFo implemented as a ring-buffer which passes {@link Data} from
 * {@link Input} / {@link Converter} thread to the {@link Output} thread in a
 * thread-safe manner with priority support.
 * 
 * @author oliver
 * @see StandardInput
 */
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
	 * The priority of the {@link Data} most recently read from {@link #buffer}.
	 */
	private byte lastPriority;

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
		Log.detail("Sending close to ring-buffer '%s'", this);
		closed = true;
	}

	/**
	 * Clears and (if currently closed) reopens the queue.<br>
	 * All data in the ring buffer not yet read will be lost.
	 */
	public void resetCache() {
		synchronized (this) {
			Log.detail("Resetting ring-buffer '%s'", this);
			buffer = new Data[1];// TODO bigger default
			readPos = 0;
			writePos = 0;
			closed = false;
			lastPriority = Data.PRIO_LOWEST;
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
			lastPriority = res.getPriority();
			Log.detail("Read from %03d '%s' in '%s'", readPos, res, this);
			readPos = (readPos + 1) % buffer.length;
			return res;
		}
	}

	/**
	 * Puts one {@link Data} into the ringbuffer, so it can be read by
	 * {@link #get()}.<br>
	 * If {@link Data}'s priority is lower than the one of the current elements
	 * in the queue, the new {@link Data} will silently be dropped. <br>
	 * If {@link Data}'s priority is higher than the one of the current elements
	 * in the queue, the queue is cleared before inserting the new {@link Data}. <br>
	 * Should only be called from the Input/Converter thread.
	 * 
	 * @param data
	 *            data to put into the ring buffer
	 * @return true if the queue has been cleared because the new {@link Data}
	 *         has a higher priority as the current {@link Data}s in the queue
	 * @throws IOException
	 *             if the {@link DataQueue} has been {@link #close()}d.
	 */
	public synchronized boolean put(final Data data) throws IOException {
		if (closed) throw new IOException("DataQueue is closed");

		if (data.getPriority() < lastPriority) // silently drop the new Data
			return false;
		if (data.getPriority() > lastPriority) // quick clearing of the queue
			readPos = writePos;

		buffer[writePos] = data;
		Log.detail("Put at %03d '%s' in '%s'", writePos, data, this);
		writePos = (writePos + 1) % buffer.length;
		if (writePos == readPos) {
			// we need to increase our ring buffer:
			// we "insert space" between the current write and
			// read position so writePos stays the same while
			// readPos moves.
			final Data[] newbuf = new Data[buffer.length * 2];
			readPos += newbuf.length - buffer.length;
			Log.detail("Increased from %d to %d in '%s'", buffer.length,
					newbuf.length, this);
			System.arraycopy(buffer, 0, newbuf, 0, writePos);
			System.arraycopy(buffer, writePos, newbuf, readPos, buffer.length
					- writePos);
			buffer = newbuf;
		}

		return data.getPriority() > lastPriority;
	}

	/**
	 * Puts a series of {@link Data} into the ring buffer as an atomic operation
	 * (i.e. completely synchronized).
	 * 
	 * @param list
	 *            data to put into the ring buffer
	 * @return true if the queue has been cleared because one of the new
	 *         {@link Data} has a higher priority as the current {@link Data}s
	 *         in the queue
	 * @throws IOException
	 *             if the queue has been closed
	 * @see #put(Data)
	 */
	public synchronized boolean put(final List<Data> list) throws IOException {
		boolean cleared = false;
		for (final Data data : list)
			cleared |= put(data);
		return cleared;
	}

	@Override
	public String toString() {
		return String.format("cap: %d, read: %d, write: %d",
				buffer == null ? -1 : buffer.length, readPos, writePos);
	}

}
