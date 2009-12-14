package pleocmd.pipe.out;

import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import pleocmd.Log;
import pleocmd.api.PleoCommunication;
import pleocmd.exc.OutputException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigEnum;
import pleocmd.pipe.Data;

public final class PleoRXTXOutput extends Output {

	private PleoCommunication pc;

	private String device;

	public PleoRXTXOutput() {
		super(new Config().addV(new ConfigEnum("Device", getAllDeviceNames())));
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
	protected void configured0() {
		device = getConfig().get(0).getContentAsString();
	}

	@Override
	protected void init0() throws IOException {
		Log.detail("Initializing PleoRXTXOutput for device " + device);
		pc = new PleoCommunication(PleoCommunication.getPort(device));
		pc.init();
	}

	@Override
	protected void close0() {
		Log.detail("Closing PleoRXTXOutput " + pc + " for device " + device);
		pc.close();
		pc = null;
	}

	@Override
	protected void write0(final Data data) throws OutputException, IOException {
		if ("PMC".equals(data.getSafe(0).asString())) {
			pc.send(data.getSafe(1).asString());
			try {
				pc.readAnswer();
			} catch (final TimeoutException e) {
				throw new OutputException(this, true, "Cannot read answer", e);
			}
		}
	}

}
