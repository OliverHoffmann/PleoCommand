package pleocmd.pipe.in;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import pleocmd.pipe.cfg.ConfigInt;
import pleocmd.pipe.cfg.ConfigString;
import pleocmd.pipe.data.Data;

public final class TcpIpInput extends Input {

	private final ConfigString cfg0;

	private final ConfigInt cfg1;

	private String host;

	private int port;

	private Socket socket;

	private DataInputStream in;

	public TcpIpInput() {
		getConfig().add(cfg0 = new ConfigString("Host", false));
		getConfig().add(cfg1 = new ConfigInt("Port", 1, 65535));
		cfg1.setContent(19876);
		constructed();
	}

	public TcpIpInput(final String host, final int port) {
		this();
		cfg0.setContent(host);
		cfg1.setContent(port);
	}

	@Override
	protected void configure0() {
		host = cfg0.getContent();
		port = (int) cfg1.getContent();
	}

	@Override
	protected void init0() throws IOException {
		socket = new Socket(host, port);
		in = new DataInputStream(socket.getInputStream());
	}

	@Override
	protected void close0() throws IOException {
		socket.close(); // closes "in", too
	}

	@Override
	protected boolean canReadData0() throws IOException {
		// TODO use a put-back stream and just read one byte?
		return true; // impossible to detect beforehand on socket stream
	}

	@Override
	protected Data readData0() throws IOException {
		return Data.createFromBinary(in);
	}

	public static String help(final HelpKind kind) {
		// TODO
		switch (kind) {
		case Name:
			return "TCP/IP Input";
		case Description:
			return "Reads Data blocks from a TCP/IP connection";
		case Configuration:
			return "1: Name of the Host (machine name or IP address), "
					+ "empty for loopback device\n"
					+ "2: Port number of the Host\n";
		default:
			return "???";
		}
	}

}
