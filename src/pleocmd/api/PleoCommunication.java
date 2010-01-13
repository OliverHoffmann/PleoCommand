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
import pleocmd.cfg.ConfigInt;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;

/**
 * This is the central communication class with the Pleo.<br>
 * Handles opening and closing connections, sending and receiving data to/from
 * the Pleo.
 * 
 * @author oliver
 */
public final class PleoCommunication implements SerialPortEventListener,
		ConfigurationInterface {

	private final ConfigInt cfgOpenTimeout = new ConfigInt("Open Timeout",
			5000, 100, 60000);

	private final ConfigInt cfgAnswerTimeout = new ConfigInt("Answer Timeout",
			60000, 1000, 60 * 60 * 1000);

	private final ConfigInt cfgBaudrate = new ConfigInt("Baudrate", 115200,
			110, 256000);

	private final CommPortIdentifier portID;

	private SerialPort port;

	private InputStream in;

	private OutputStream out;

	private final StringBuffer inBuffer = new StringBuffer();

	private long inBufferLastRead;

	/**
	 * Creates a new instance of {@link PleoCommunication} which is bound to one
	 * port.
	 * 
	 * @param portID
	 *            the port under which the Pleo should be found
	 * @see #getAvailableSerialPorts()
	 * @see #getHighestPort()
	 */
	public PleoCommunication(final CommPortIdentifier portID) {
		this.portID = portID;
		Log.detail("Bound to port '%s' owned by '%s'", portID.getName(), portID
				.getCurrentOwner());
		try {
			Configuration.the().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	/**
	 * Closes the connection to the port. Does nothing if no connection is
	 * currently open.
	 */
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

	/**
	 * Opens a new connection to the port specified in
	 * {@link #PleoCommunication(CommPortIdentifier)}. If a connection is
	 * already open, it will be closed first.<br>
	 * Blocks until the connection has been opened or {@link #cfgOpenTimeout}
	 * milliseconds have been elapsed.
	 * 
	 * @throws IOException
	 *             if the port is already in use or the serial parameters could
	 *             not be set.
	 */
	public void init() throws IOException {
		close();
		Log.detail("Connecting");
		try {
			port = (SerialPort) portID.open("PleoCommand", cfgOpenTimeout
					.getContent());
		} catch (final PortInUseException e) {
			throw new IOException("Port already in use");
		}
		Log.info("Connected to port '%s'", port);
		in = port.getInputStream();
		out = port.getOutputStream();
		try {
			port.addEventListener(this);
		} catch (final TooManyListenersException e) {
			throw new InternalException("Only one, but too many listeners");
		}
		port.notifyOnDataAvailable(true);
		try {
			port.setSerialPortParams(cfgBaudrate.getContent(),
					SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			Log.detail("Done initializing '%s'", toString());
		} catch (final UnsupportedCommOperationException e) {
			throw new IOException("Cannot set serial-port parameters");
		}
	}

	/**
	 * Reads new available data from the port and puts it into a buffer.
	 * <p>
	 * Is only <b>public</b> because of bad defaults in the super class.<br>
	 * Must never be called.
	 */
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

	/**
	 * Sends one command by writing all the bytes in the given {@link String}
	 * with ISO-8859-1 encoding plus an additional newline character. <br>
	 * Must only be called if a connection has been opened via {@link #init()}.
	 * 
	 * @param command
	 *            the command to send thru the connection (must not have a
	 *            newline character)
	 * @throws IOException
	 *             if an error during converting or sending the bytes occurred
	 */
	public void send(final String command) throws IOException {
		inBuffer.delete(0, Integer.MAX_VALUE);
		inBuffer.trimToSize();
		// add additional seconds to wait-time in readAnswer() before the
		// first packet of data has been received
		inBufferLastRead = System.currentTimeMillis();
		out.write((command + "\n").getBytes("ISO-8859-1"));
		out.flush();
		Log.detail("Sent '%s'", command);
	}

	/**
	 * Tries to read the answer of a {@link #send(String)} from the connection.
	 * Should be called after every {@link #send(String)} before sending again
	 * (even if the answer is not processed any further) to clear the output
	 * stream and make sure Pleo is able to receive new input.
	 * 
	 * @return the answer read from the connection converted via ISO-8859-1
	 * @throws TimeoutException
	 *             if the answer could not be read (completely) within a given
	 *             time ({@link #cfgAnswerTimeout}).
	 */
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
			if (wait > cfgAnswerTimeout.getContent()) //
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

	/**
	 * Collects all serial ports available under the current operating system.
	 * 
	 * @return an unsorted list of all available ports
	 */
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

	/**
	 * Returns a {@link CommPortIdentifier} fitting to the given name.
	 * 
	 * @param name
	 *            the name of a port (under Linux something like
	 *            <i>/dev/ttyS0</i>)
	 * @return the fitting {@link CommPortIdentifier}
	 * @throws IOException
	 *             if a port with the given name could not be found
	 */
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

	/**
	 * Returns the "highest" available port, i.e. the port with the highest
	 * decimal number.
	 * 
	 * @return "highest" available port
	 * @throws IOException
	 *             if not even one port is available
	 */
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
		Log.detail("Found as highest port: %s", ports.get(0).getName());
		return ports.get(0);
	}

	@Override
	public String toString() {
		if (port == null) return "<No port assigned>";
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

	@Override
	public Group getSkeleton(final String groupName)
			throws ConfigurationException {
		return new Group(groupName).add(cfgOpenTimeout).add(cfgAnswerTimeout)
				.add(cfgBaudrate);
	}

	@Override
	public void configurationAboutToBeChanged() throws ConfigurationException {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group)
			throws ConfigurationException {
		// nothing to do
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

}
