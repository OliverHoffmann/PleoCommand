package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.MultiFloatData;
import pleocmd.pipe.data.SingleFloatData;

public final class DataBlockSplitter extends Converter {

	private static final int MAX_VIS = 8;

	private final ConfigInt cfgChannelNr;

	private final ConfigInt cfgDelay;

	public DataBlockSplitter() {
		addConfig(cfgChannelNr = new ConfigInt("Channel-Number", 1, 1, 32));
		addConfig(cfgDelay = new ConfigInt("Delay (in ms)", 0, 0,
				Integer.MAX_VALUE));
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
			ds.setLabel(String.format("Channel %d", cfgChannelNr.getContent()));
			ds.setPen(getVisualizationDialog().getDiagram().detectPen(
					cfgChannelNr.getContent()));
		}
	}

	@Override
	public String getInputDescription() {
		return MultiFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!MultiFloatData.isMultiFloatData(data)) return null;
		final int cnt = MultiFloatData.getValueCount(data);
		final List<Data> res = new ArrayList<Data>(cnt);
		long time = data.getTime();
		if (time == Data.TIME_NOTIME && cfgDelay.getContent() != 0)
			time = getPipe().getFeedback().getElapsed();
		for (int i = 0; i < cnt; ++i) {
			final double val = MultiFloatData.getValue(data, i);
			res.add(new SingleFloatData(val, cfgChannelNr.getContent(), data,
					Data.PRIO_DEFAULT, time));
			if (isVisualize() && i <= MAX_VIS) plot(i, val);
			time += cfgDelay.getContent();
		}
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Data Block Splitter";
		case Description:
			return "Splits a multi-value data packet into its "
					+ "single values, like 'Multi|2.5|7|0.01' to "
					+ "'Single|2.5|1', 'Single|7|1' and 'Single|0.01|1'";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return MAX_VIS;
	}

}
