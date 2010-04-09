package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleValueData;

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
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) {
			ds.setLabel(String.format("Channel %d",
					cfgChannelNr.getContent() + 1));
			ds.setPen(getVisualizationDialog().getDiagram().detectPen(
					cfgChannelNr.getContent()));
		}
	}

	@Override
	public String getInputDescription() {
		return SingleValueData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleValueData.IDENT;
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleValueData.isSingleValueData(data)) return null;
		if (SingleValueData.getUser(data) != cfgChannelNr.getContent())
			return emptyList();
		if (isVisualize()) plot(0, SingleValueData.getValue(data));
		return asList(data);
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
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 1;
	}

}
