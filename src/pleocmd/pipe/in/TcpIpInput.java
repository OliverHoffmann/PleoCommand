package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConfigurationException;
import pleocmd.pipe.data.Data;

public final class TcpIpInput extends Input {

	private final ConfigInt cfgTimeoutConn;

	private final ConfigInt cfgTimeoutRead;

	private final ConfigInt cfgPort;

	private Socket socket;

	private ServerSocket serverSocket;

	private DataInputStream in;

	public TcpIpInput() {
		addConfig(cfgPort = new ConfigInt("Port", 19876, 1, 65535));
		addConfig(cfgTimeoutConn = new ConfigInt("Connection-Timeout (sec)",
				60, 0, 3600));
		addConfig(cfgTimeoutRead = new ConfigInt("Read-Timeout (sec)", 10, 0,
				3600));
		constructed();
	}

	public TcpIpInput(final int port) {
		this();
		try {
			cfgPort.setContent(port);
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException("Cannot set port", e);
		}
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		serverSocket = new ServerSocket();
		serverSocket.setPerformancePreferences(0, 2, 1);
		serverSocket.setReuseAddress(true);
		serverSocket.setSoTimeout(cfgTimeoutConn.getContent() * 1000);
		serverSocket.bind(new InetSocketAddress(cfgPort.getContent()));
		socket = serverSocket.accept();
		socket.setSoTimeout(cfgTimeoutRead.getContent() * 1000);
		in = new DataInputStream(socket.getInputStream());
	}

	@Override
	protected void close0() throws IOException {
		in.close();
		socket.close();
		serverSocket.close();
	}

	@Override
	protected boolean canReadData0() throws IOException {
		return socket.isConnected() && !socket.isInputShutdown();
	}

	@Override
	protected Data readData0() throws IOException {
		return Data.createFromBinary(in);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "TCP/IP Input";
		case Description:
			return "Reads Data blocks from a TCP/IP connection";
		case Configuration:
			return "1: Port number of the Host\n"
					+ "2: Connection Timeout in seconds (0 means infinite)\n"
					+ "3: Timeout for reading in seconds (0 means infinite)";
		default:
			return "???";
		}
	}

}
