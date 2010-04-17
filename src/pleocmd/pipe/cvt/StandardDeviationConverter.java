package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class StandardDeviationConverter extends Converter {

	private final ConfigInt cfgTimeFrameLength;

	private double sum; // sum of all x_i, where i = 0..m_timeFrame
	private double sum2; // sum of all (x_i - avg), where i = 0..m_timeFrame
	private int valPos; // position in ring buffer m_val
	private double[] values; // ring buffer for m_sum
	private double[] values2; // ring buffer for m_sum2
	private boolean feeded; // first round or enough data?

	public StandardDeviationConverter() {
		addConfig(cfgTimeFrameLength = new ConfigInt("Length of Time-Frame",
				100, 1, 1024 * 1024));
		constructed();
	}

	@Override
	protected void init0() {
		sum = .0;
		sum2 = .0;
		valPos = 0;
		values = new double[cfgTimeFrameLength.getContent()];
		values2 = new double[cfgTimeFrameLength.getContent()];
		feeded = false;
	}

	@Override
	protected void close0() {
		values = null; // make garbage collector happy
		values2 = null; // make garbage collector happy
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null)
			ds.setLabel(String.format("Deviation of Data [%d]",
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
	protected String getShortConfigDescr0() {
		return String.format("...%d...", cfgTimeFrameLength.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		double val = SingleFloatData.getValue(data);
		final double avg = sum / values.length;
		if (feeded) {
			sum -= values[valPos];
			sum2 -= values2[valPos];
		}
		sum += val;
		sum2 += val - avg;
		values[valPos] = val;
		values2[valPos] = val - avg;
		valPos = (valPos + 1) % values.length;
		if (valPos == 0) feeded = true; // at least one loop now
		val = feeded ? sum2 / values.length : 0;
		if (isVisualize()) plot(0, val);
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Standard-Deviation Converter";
		case Description:
			return "Calculates the standard deviation of the Data based "
					+ "on the most recent data packets";
		case Config1:
			return "Number of Data blocks to use for deviation calculation";
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
