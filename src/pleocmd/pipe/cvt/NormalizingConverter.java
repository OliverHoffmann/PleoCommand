package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class NormalizingConverter extends Converter {

	private final ConfigInt cfgTimeFrameLength;

	private double sum; // sum of all x_i, where i = 0..m_timeFrame
	private int valPos; // position in ring buffer m_val
	private double[] values; // ring buffer for m_sum
	private boolean feeded; // first round or enough data?

	public NormalizingConverter() {
		addConfig(cfgTimeFrameLength = new ConfigInt("Length of Time-Frame",
				100, 1, 1024 * 1024));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() {
		sum = .0;
		valPos = 0;
		values = new double[cfgTimeFrameLength.getContent()];
		feeded = false;
	}

	@Override
	protected void close0() {
		values = null; // make garbage collector happy
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null)
			ds.setLabel(String.format("Normalized Data [%d]",
					cfgTimeFrameLength.getContent()));
	}

	@Override
	public String getInputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		double val = SingleFloatData.getValue(data);
		if (feeded) sum -= values[valPos];
		sum += val;
		values[valPos] = val;
		valPos = (valPos + 1) % values.length;
		if (valPos == 0) feeded = true; // at least one loop now
		if (!feeded) return emptyList();

		double realsum = .0;
		for (final double d : values)
			realsum += d;
		System.out.println(sum + " " + realsum + " " + Math.abs(sum - realsum)
				+ " " + val);

		val -= sum / values.length;
		if (isVisualize()) plot(0, val);
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Normalizing Converter";
		case Description:
			return "Normalizes the Data to the baseline by reducing it "
					+ "by the average over the most recent Data blocks";
		case Config1:
			return "Number of Data blocks to use for average calculation";
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
		return 1;
	}

}
