package pleocmd;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;

import pleocmd.itfc.gui.MainFrame;

/**
 * In console mode, the standard input gets simply wrapped by this class.<br>
 * In GUI mode, data coming from the GUI's {@link javax.swing.JTextField} will
 * be cached inside a ring buffer of this {@link StandardInput} to later be read
 * by {@link #read()}.
 * 
 * @author oliver
 */
public final class StandardInput extends InputStream {

	private static StandardInput stdin;

	/**
	 * This array represents a ring buffer.
	 */
	private byte[] cache;

	/**
	 * The position of the next byte to read from {@link #cache}.
	 */
	private int cachePosRead;

	/**
	 * The position of the next byte to write to {@link #cache}.
	 */
	private int cachePosWrite;

	/**
	 * Only true if the cache has been closed, i.e. the remaining data in
	 * {@link #cache} can still be read, but no new data can be put into the
	 * {@link #cache} and if {@link #cachePosRead} catches up
	 * {@link #cachePosWrite} {@link #available()} will return 0.
	 */
	private boolean cacheClosed;

	private StandardInput() {
		stdin = this;
		resetCache();
	}

	/**
	 * Returns or creates the singleton instance.
	 * 
	 * @return instance of {@link StandardInput}
	 */
	public static StandardInput the() {
		if (stdin == null) new StandardInput();
		return stdin;
	}

	/**
	 * True if the input stream has been closed.
	 * 
	 * @return true if closed
	 * @see #close()
	 */
	public synchronized boolean isClosed() {
		return cacheClosed;
	}

	/**
	 * Appends a "close" to the ring buffer in GUI mode and has no effect in
	 * console mode.<br>
	 * The remaining {@link #available()} data in the ring buffer can still be
	 * {@link #read()} but no new data can be {@link #put(byte)} into it. After
	 * no more data is {@link #available()} {@link #read()} throws an
	 * {@link IOException}.<br>
	 * Has no effect if the {@link StandardInput} is already closed.
	 */
	@Override
	public void close() throws IOException {
		if (MainFrame.hasGUI()) synchronized (this) {
			cacheClosed = true;
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (MainFrame.hasGUI()) MainFrame.the().updateState();
			}
		});
	}

	/**
	 * Clears and (if currently closed) reopens the input stream.<br>
	 * All data in the ring buffer not yet read will be lost.<br>
	 * Has no effect in console mode.
	 */
	public void resetCache() {
		synchronized (this) {
			Log.detail("Resetting cache");
			cache = new byte[8];
			cachePosRead = 0;
			cachePosWrite = 0;
			cacheClosed = false;
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (MainFrame.hasGUI()) MainFrame.the().updateState();
			}
		});
	}

	/**
	 * Returns the number of bytes ready to read via {@link #read()}.<br>
	 * Blocks until data is available or (in GUI mode) {@link #close()} or
	 * {@link #resetCache()} has been called.<br>
	 * In GUI mode, it should <b>not</b> be called from the GUI thread.
	 * 
	 * @return available bytes
	 */
	@Override
	public int available() throws IOException {
		if (MainFrame.hasGUI()) while (true) {
			// check if read catches up write?
			int avail;
			synchronized (this) {
				avail = (cachePosWrite - cachePosRead) % cache.length;
			}
			if (avail < 0) avail += cache.length; // Java's mod is not a mod :(
			if (avail > 0) {
				Log.detail("%d bytes available", avail);
				return avail;
			}
			synchronized (this) {
				if (cacheClosed)
				// no need to wait any longer - there can never be any new data
					return 0;
			}
			// block until data available
			try {
				Thread.sleep(30);
			} catch (final InterruptedException e) {
				throw new IOException("Interrupted while waiting for input", e);
			}
		}
		return System.in.available(); // CS_IGNORE
	}

	/**
	 * Reads one byte from the input stream or (in GUI mode) the ring buffer.<br>
	 * Blocks until the byte is available.<br>
	 * In GUI mode, it should <b>not</b> be called from the GUI thread.
	 */
	@Override
	public int read() throws IOException {
		if (MainFrame.hasGUI()) {
			while (true) {
				// check if read catches up write?
				synchronized (this) {
					if (cachePosRead != cachePosWrite) break;
					if (cacheClosed)
						throw new IOException("StandardInput is closed");
				}
				// block until data available
				try {
					Thread.sleep(30);
				} catch (final InterruptedException e) {
					throw new IOException(
							"Interrupted while waiting for input", e);
				}
			}
			synchronized (this) {
				final int b = cache[cachePosRead];
				Log.detail(String.format("Read from %03d %d", cachePosRead, b));
				cachePosRead = (cachePosRead + 1) % cache.length;
				return b;
			}
		}
		return System.in.read(); // CS_IGNORE
	}

	/**
	 * Puts one byte into the ringbuffer in GUI mode, so it can be read by
	 * {@link #read()}.<br>
	 * Should only be called in GUI mode and from the GUI thread.
	 * 
	 * @param b
	 *            byte to put into the ring buffer
	 * @throws IOException
	 *             if the stream has been closed
	 */
	public void put(final byte b) throws IOException {
		assert MainFrame.hasGUI();
		if (cacheClosed) throw new IOException("StandardInput is closed");
		synchronized (this) {
			cache[cachePosWrite] = b;
			Log.detail(String.format("Put at %03d %d", cachePosWrite, b));
			cachePosWrite = (cachePosWrite + 1) % cache.length;
			// check if write catches up read?
			if (cachePosRead == cachePosWrite) {
				// we need to increase our ring buffer:
				// we "insert space" between the current write and
				// read position so cachePosWrite stays the same as
				// cachePosRead moves.
				final byte[] newcache = new byte[cache.length * 2];
				cachePosRead += newcache.length - cache.length;
				Log.detail(String.format("Increased from %d to %d",
						cache.length, newcache.length));
				System.arraycopy(cache, 0, newcache, 0, cachePosWrite);
				System.arraycopy(cache, cachePosWrite, newcache, cachePosRead,
						cache.length - cachePosWrite);
				cache = newcache;
			}
		}
	}

	/**
	 * Puts a series of bytes into the ring buffer as an atomic operation (i.e.
	 * completely synchronized).
	 * 
	 * @param bytes
	 *            data to put into the ring buffer
	 * @throws IOException
	 *             if the stream has been closed
	 * @see #put(byte)
	 */
	public void put(final byte[] bytes) throws IOException {
		synchronized (this) {
			for (final byte b : bytes)
				put(b);
		}
	}

}
