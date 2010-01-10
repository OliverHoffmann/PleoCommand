package pleocmd.pipe.out;

import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import pleocmd.Log;
import pleocmd.api.PleoCommunication;
import pleocmd.cfg.ConfigItem;
import pleocmd.cfg.ConfigurationException;
import pleocmd.exc.OutputException;
import pleocmd.pipe.data.Data;

public final class PleoRXTXOutput extends Output {

	private final ConfigItem cfgDevice;

	private PleoCommunication pc;

	public PleoRXTXOutput() {
		addConfig(cfgDevice = new ConfigItem("Device", true,
				getAllDeviceNames()));
		constructed();
	}

	public PleoRXTXOutput(final String device) throws ConfigurationException {
		this();
		cfgDevice.setContent(device);
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
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Initializing PleoRXTXOutput for device '%s'", cfgDevice
				.getContent());
		pc = new PleoCommunication(PleoCommunication.getPort(cfgDevice
				.getContent()));
		pc.init();
	}

	@Override
	protected void close0() {
		Log.detail("Closing PleoRXTXOutput '%s' for device '%s'", pc, cfgDevice
				.getContent());
		pc.close();
		pc = null;
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		if ("PMC".equals(data.getSafe(0).asString())) {
			pc.send(data.get(1).asString());
			try {
				Log.consoleOut(pc.readAnswer());
			} catch (final TimeoutException e) {
				throw new OutputException(this, true, e,
						"Cannot read answer for command '%s'", data);
			}
			return true;
		}
		return false;
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
