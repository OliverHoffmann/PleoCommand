package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.exc.FormatException;
import pleocmd.pipe.data.Data;

public final class StatedIntervalConverter extends Converter {

	private final ConfigString cfgCommand1;
	private final ConfigString cfgCommand2;

	private double sum;
	private int nextCommand;

	public StatedIntervalConverter() {
		addConfig(cfgCommand1 = new ConfigString("Command 1",
				"PMC|JOINT MOVE 0 0"));
		addConfig(cfgCommand2 = new ConfigString(
				"Command 2 (alternating, optional)", ""));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() {
		sum = .0;
		nextCommand = 2;
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
		return "";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!"BCIChannel".equals(data.getSafe(0).asString())) return null;
		final List<Data> res = new ArrayList<Data>(1);
		sum += 1.0 / data.get(2).asDouble();
		if (sum < 1) return res;
		sum = .0;
		switch (nextCommand) {
		case 1:
			res.add(createCommand(cfgCommand1, data));
			break;
		case 2:
			res.add(createCommand(
					cfgCommand2.getContent().isEmpty() ? cfgCommand1
							: cfgCommand2, data));
		}
		nextCommand = 3 - nextCommand;
		return res;
	}

	private Data createCommand(final ConfigString cfg, final Data parent)
			throws ConverterException {
		try {
			return new Data(Data.createFromAscii(cfg.getContent()), parent);
		} catch (final IOException e) {
			throw new ConverterException(this, true, "Invalid "
					+ cfg.getLabel(), e);
		} catch (final FormatException e) {
			throw new ConverterException(this, true, "Invalid "
					+ cfg.getLabel(), e);
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Stated Interval Converter";
		case Description:
			return "Sends one or two commands at a interval specified by "
					+ "the source values, i.e. if input is [2, 2, 2, 2, 1, 1]"
					+ "output would be [-, C, -, C, C, C] where C is command "
					+ "and - marks a dropped data packet";
		case Configuration:
			return "1: First command to send (alternating with the second one)\n"
					+ "2: Second command to send (if empty, "
					+ "only first one will be used)";
		default:
			return "???";
		}
	}

	@Override
	public boolean isConfigurationSane() {
		return true;
	}

}
