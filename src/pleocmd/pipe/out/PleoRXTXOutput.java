package pleocmd.pipe.out;

import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import pleocmd.Log;
import pleocmd.api.PleoCommunication;
import pleocmd.exc.OutputException;
import pleocmd.pipe.cfg.ConfigList;
import pleocmd.pipe.data.Data;

public final class PleoRXTXOutput extends Output {

	private final ConfigList cfg0;

	private PleoCommunication pc;

	private String device;

	public PleoRXTXOutput() {
		getConfig().add(
				cfg0 = new ConfigList("Device", true, getAllDeviceNames()));
		constructed();
	}

	private static List<String> getAllDeviceNames() {
		final List<String> names = new ArrayList<String>();
		final List<CommPortIdentifier> ports = PleoCommunication
				.getAvailableSerialPorts();
		for (final CommPortIdentifier port : ports)
			names.add(port.getName());
		return names;
	}

	@Override
	protected void configure0() {
		device = cfg0.getContent();
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Initializing PleoRXTXOutput for device '%s'", device);
		pc = new PleoCommunication(PleoCommunication.getPort(device));
		pc.init();
	}

	@Override
	protected void close0() {
		Log.detail("Closing PleoRXTXOutput '%s' for device '%s'", pc, device);
		pc.close();
		pc = null;
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		if ("PMC".equals(data.getSafe(0).asString())) {
			pc.send(data.get(1).asString());
			try {
				pc.readAnswer();
			} catch (final TimeoutException e) {
				throw new OutputException(this, true, e,
						"Cannot read answer for command '%s'", data);
			}
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Pleo RXTX Output";
		case Description:
			return "Processes commands like 'PMC|foo' by sending 'foo' "
					+ "without any modifications to the Pleo via an USB "
					+ "connection by using the RXTX library";
		case Configuration:
			return "1: Path to the device on which the Pleo is connected to"
					+ "this computer";
		default:
			return "???";
		}
	}

}
