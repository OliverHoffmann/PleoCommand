package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.MultiValueData;
import pleocmd.pipe.data.SingleValueData;

public final class BCIChannelSplitter extends Converter {

	private static final int MAX_VIS = 8;

	public BCIChannelSplitter() {
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
		for (int i = 0; i < MAX_VIS; ++i) {
			final DiagramDataSet ds = getVisualizeDataSet(i);
			if (ds != null)
				ds.setLabel(String.format("BCI Channel %d", i + 1));
		}
	}

	@Override
	public String getInputDescription() {
		return MultiValueData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleValueData.IDENT;
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!MultiValueData.isMultiValueData(data)) return null;
		final int cnt = MultiValueData.getValueCount(data);
		final List<Data> res = new ArrayList<Data>(cnt);
		for (int i = 0; i < cnt; ++i) {
			final double val = MultiValueData.getValue(data, i);
			res.add(new SingleValueData(val, i + 1, data));
			if (isVisualize() && i <= MAX_VIS) plot(i, val);
		}
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Channel Splitter";
		case Description:
			return "Splits a multi-channel data packet into its "
					+ "single channels, like 'Multi|2.5|7|0.01' to "
					+ "'Single|2.5|1', 'Single|7|2' and 'Single|0.01|3'";
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
