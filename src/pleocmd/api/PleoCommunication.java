package pleocmd.api;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;

import pleocmd.Log;

public final class PleoCommunication implements SerialPortEventListener {

	private static final int OPEN_TIMEOUT = 5000;

	private static final int ANSWER_TIMEOUT_1 = 6000;

	private static final int ANSWER_TIMEOUT_N = 3000;

	private static final int BAUDRATE = 115200;

	private final CommPortIdentifier portID;

	private SerialPort port;

	private InputStream in;

	private OutputStream out;

	private final StringBuffer inBuffer = new StringBuffer();

	private long inBufferLastRead;

	public PleoCommunication(final CommPortIdentifier portID) {
		this.portID = portID;
		Log.detail("Bound to port '%s' owned by '%s'", portID.getName(), portID
				.getCurrentOwner());
	}

	public void close() {
		Log.detail("Closing");
		try {
			if (in != null) in.close();
			if (out != null) out.close();
			if (port != null) port.close();
		} catch (final IOException e) {
			// ignore
		}
		in = null;
		out = null;
		port = null;
	}

	public void init() throws IOException {
		close();
		Log.detail("Connecting");
		try {
			port = (SerialPort) portID.open("PleoCommand", OPEN_TIMEOUT);
		} catch (final PortInUseException e) {
			throw new IOException("Port already in use");
		}
		Log.info("Connected to port '%s'", port);
		in = port.getInputStream();
		out = port.getOutputStream();
		try {
			port.addEventListener(this);
		} catch (final TooManyListenersException e) {
			throw new IOException("Internal error: Too many listeners");
		}
		port.notifyOnDataAvailable(true);
		try {
			port.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			Log.detail(String.format("Done initializing %s", toString()));
		} catch (final UnsupportedCommOperationException e) {
			throw new IOException("Cannot set serial-port parameters");
		}
	}

	@Override
	public void serialEvent(final SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
			try {
				final int avail = in.available();
				Log.detail("Reading %d available bytes", avail);
				if (avail <= 0)
					throw new RuntimeException(
							"DATA_AVAILABLE Event but no data in input-stream!");
				final byte[] buf = new byte[avail];
				final int read = in.read(buf);
				inBuffer.append(new String(buf, 0, read, "ISO-8859-1"));
				inBufferLastRead = System.currentTimeMillis();
				Log.detail("Received %d bytes", read);
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException("Default code-table missing!");
			} catch (final IOException e) {
				throw new RuntimeException(
						"Cannot read available data from USB!");
			}
	}

	public void send(final String command) throws IOException {
		inBuffer.delete(0, Integer.MAX_VALUE);
		inBuffer.trimToSize();
		// add additional seconds to wait-time in readAnswer() before the
		// first packet of data has been received
		inBufferLastRead = System.currentTimeMillis() + ANSWER_TIMEOUT_1
				- ANSWER_TIMEOUT_N;
		out.write((command + "\n").getBytes("ISO-8859-1"));
		out.flush();
		Log.detail("Sent '%s'", command);
	}

	public String readAnswer() throws TimeoutException {
		// wait until "> " has been received which marks the end of the answer
		Log.detail("Reading answer");
		int cnt = 0;
		while (inBuffer.length() < 2
				|| inBuffer.charAt(inBuffer.length() - 2) != '>'
				|| inBuffer.charAt(inBuffer.length() - 1) != ' ') {
			final long wait = System.currentTimeMillis() - inBufferLastRead;
			if (++cnt % 4 == 0)
				Log.detail("Waiting for answer since %d ms - got %d chars yet",
						wait, inBuffer.length());
			if (wait > ANSWER_TIMEOUT_N) //
				// no response within a few seconds =>
				// handle as unexpected end of received data
				throw new TimeoutException(String.format(
						"Timeout in reading answer from Pleo via port '%s'",
						portID.getName()));
			try {
				Thread.sleep(50);
			} catch (final InterruptedException e) {
				Log.warn("Interrupted while waiting for answer");
				return null;
			}
		}
		Log.detail("Received answer with a length of %d chars", inBuffer
				.length());
		return inBuffer.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<CommPortIdentifier> getAvailableSerialPorts() {
		final List<CommPortIdentifier> h = new ArrayList<CommPortIdentifier>();
		final Enumeration<CommPortIdentifier> ports = CommPortIdentifier
				.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			final CommPortIdentifier port = ports.nextElement();
			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				Log.detail("Found port '%s'", port.getName());
				h.add(port);
			}
		}
		return h;
	}

	@SuppressWarnings("unchecked")
	public static CommPortIdentifier getPort(final String name)
			throws IOException {
		final Enumeration<CommPortIdentifier> ports = CommPortIdentifier
				.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			final CommPortIdentifier port = ports.nextElement();
			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& port.getName().equals(name)) return port;
		}
		throw new IOException("Port not found");
	}

	public static CommPortIdentifier getHighestPort() throws IOException {
		final List<CommPortIdentifier> ports = getAvailableSerialPorts();
		if (ports.isEmpty()) throw new IOException("No port available");
		Collections.sort(ports, new Comparator<CommPortIdentifier>() {

			@Override
			public int compare(final CommPortIdentifier cpi1,
					final CommPortIdentifier cpi2) {
				return -cpi1.getName().compareTo(cpi2.getName());
			}
		});
		return ports.get(0);
	}

	@Override
	public String toString() {
		try {
			return String.format("'%s' with %d %d %d %d @ "
					+ "EOI %d flowctrl %d in_size %d out_size %d "
					+ "parity_err %d rcv_framing %d rcv_trshld %d "
					+ "rcv_timeout %d", port, port.getBaudRate(), port
					.getDataBits(), port.getStopBits(), port.getParity(), port
					.getEndOfInputChar(), port.getFlowControlMode(), port
					.getInputBufferSize(), port.getOutputBufferSize(), port
					.getParityErrorChar(), port.getReceiveFramingByte(), port
					.getReceiveThreshold(), port.getReceiveTimeout());
		} catch (final UnsupportedCommOperationException e) {
			Log.error(e);
			return "[Exception while fetching port information]";
		}
	}

}
