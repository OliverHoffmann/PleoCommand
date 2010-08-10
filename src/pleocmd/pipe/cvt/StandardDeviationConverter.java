package pleocmd.pipe.cvt;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import pleocmd.Log;
import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class StandardDeviationConverter extends Converter { // NO_UCD

	private final ConfigInt cfgTimeFrameLength;

	// CS_IGNORE_BEGIN class meant as struct

	public static class Params {
		public double sum; // sum of all x_i, where i = 0..m_timeFrame
		public double sum2; // sum of all (x_i - avg), where i = 0..m_timeFrame
		public int valPos; // position in ring buffer m_val
		public double[] values; // ring buffer for m_sum
		public double[] values2; // ring buffer for m_sum2
		public boolean feeded; // first round or enough data?
	}

	// CS_IGNORE_END

	private final Map<Integer, Params> map = new Hashtable<Integer, Params>();

	public StandardDeviationConverter() {
		addConfig(cfgTimeFrameLength = new ConfigInt("Length of Time-Frame",
				100, 1, 1024 * 1024));
		constructed();
	}

	@Override
	protected void init0() {
		map.clear();
	}

	@Override
	protected void close0() {
		map.clear(); // make garbage collector happy
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
		Params p = map.get(SingleFloatData.getUser(data));
		if (p == null) {
			p = new Params();
			p.values = new double[cfgTimeFrameLength.getContent()];
			p.values2 = new double[cfgTimeFrameLength.getContent()];
			map.put((int) SingleFloatData.getUser(data), p);
		}
		final double avg = p.sum / p.values.length;
		if (p.feeded) {
			p.sum -= p.values[p.valPos];
			p.sum2 -= p.values2[p.valPos];
		}
		p.sum += val;
		p.sum2 += val - avg;
		p.values[p.valPos] = val;
		p.values2[p.valPos] = val - avg;
		p.valPos = (p.valPos + 1) % p.values.length;
		if (p.valPos == 0) p.feeded = true; // at least one loop now

		if (Double.isInfinite(p.sum) || Double.isNaN(p.sum)
				|| Double.isInfinite(p.sum2) || Double.isNaN(p.sum2)) {
			Log.warn("Average (square) sum exceeded range of "
					+ "Double data type => Resetting");
			p.sum = .0;
			p.sum2 = .0;
			p.valPos = 0;
			p.feeded = false;
		}

		if (!p.feeded) return emptyList();

		val = p.sum2 / p.values.length;
		if (isVisualize()) plot(0, val);
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Standard-Deviation Converter";
		case Description:
			return "Calculates the standard deviation of the Data based "
					+ "on the most recent data blocks";
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
