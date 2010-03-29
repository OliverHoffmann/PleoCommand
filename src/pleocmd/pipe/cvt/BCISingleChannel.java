package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.pipe.data.Data;

public final class BCISingleChannel extends Converter {

	private final ConfigInt cfgChannelNr;

	public BCISingleChannel() {
		addConfig(cfgChannelNr = new ConfigInt("Channel-Number", 0, 0, 31));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public String getInputDescription() {
		return "BCIChannel";
	}

	@Override
	public String getOutputDescription() {
		return "BCIChannel";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!"BCIChannel".equals(data.getSafe(0).asString())) return null;
		final List<Data> res = new ArrayList<Data>(1);
		if (data.get(1).asLong() == cfgChannelNr.getContent()) res.add(data);
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "BCI Single Channel";
		case Description:
			return "Lets only one single channel pass and "
					+ "blocks all other ones";
		case Configuration:
			return "1: Number of channel that is allowed to pass";
		default:
			return "???";
		}
	}

	@Override
	public boolean isConfigurationSane() {
		return true;
	}

}
