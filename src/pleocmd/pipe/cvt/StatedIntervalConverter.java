package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConverterException;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.itfc.gui.dgr.DiagramDataSet.DiagramType;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleValueData;

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
	protected void initVisualize0() {
		DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Sum");
		ds = getVisualizeDataSet(0);
		if (ds != null) {
			ds.setLabel("Command sent");
			ds.setType(DiagramType.IntersectionDiagram);
		}
	}

	@Override
	public String getInputDescription() {
		return SingleValueData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleValueData.isSingleValueData(data)) return null;
		sum += 1.0 / SingleValueData.getValue(data);
		if (isVisualize()) plot(0, sum);
		if (sum < 1) return emptyList();
		sum = .0;
		final List<Data> res;
		switch (nextCommand) {
		case 1:
			res = asList(createCommand(cfgCommand1, data));
			break;
		case 2:
			res = asList(createCommand(cfgCommand2, data));
			break;
		default:
			throw new IllegalStateException("nextCommand is invalid");
		}
		if (isVisualize()) plot(1, nextCommand);
		nextCommand = cfgCommand2.getContent().isEmpty() ? 1 : 3 - nextCommand;
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
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 2;
	}

}
