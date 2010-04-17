package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.cfg.ConfigDouble;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class ThresholdConverter extends Converter {

	private final ConfigDouble cfgTheshold;

	private final ConfigDouble cfgMarginalArea;

	private final ConfigDouble cfgValueBelow;

	private final ConfigDouble cfgValueAbove;

	private enum Hit {
		Undef, Lower, Upper
	}

	private enum Val {
		Undef, Below, Above
	}

	private double upper;
	private double lower;
	private Hit lastHit;
	private Val value;
	private double lastVal;

	public ThresholdConverter() {
		addConfig(cfgTheshold = new ConfigDouble("Threshold", 10, 0,
				Double.MAX_VALUE));
		addConfig(cfgMarginalArea = new ConfigDouble("Marginal Area", 3, 0,
				Double.MAX_VALUE));
		addConfig(cfgValueBelow = new ConfigDouble("Value For Below", 0, 0,
				Double.MAX_VALUE));
		addConfig(cfgValueAbove = new ConfigDouble(
				"Value For Above (0 == current)", 0, 0, Double.MAX_VALUE));
		constructed();
	}

	@Override
	protected void init0() {
		upper = cfgTheshold.getContent() + cfgMarginalArea.getContent() / 2;
		lower = cfgTheshold.getContent() - cfgMarginalArea.getContent() / 2;
		lastHit = Hit.Undef;
		value = Val.Undef;
		lastVal = .0;
	}

	@Override
	protected void initVisualize0() {
		DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Resulting Data");
		ds = getVisualizeDataSet(1);
		if (ds != null) ds.setLabel("Lower Margin");
		ds = getVisualizeDataSet(2);
		if (ds != null) ds.setLabel("Upper Margin");
		ds = getVisualizeDataSet(3);
		if (ds != null) ds.setLabel("Threshold");
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
		return String.format("|>%s±%s| => %s : %s", cfgTheshold.asString(),
				String.valueOf(cfgMarginalArea.getContent() / 2), cfgValueBelow
						.asString(),
				cfgValueAbove.getContent() > 0 ? cfgValueAbove.asString()
						: "<cur>");
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		double val = SingleFloatData.getValue(data);

		if (val < 0) val = -val; // consider values as absolute

		// check if we just broke through the marginal area's bounds
		if (lastVal < lower && val >= lower) lastHit = Hit.Lower;
		if (lastVal > upper && val <= upper) lastHit = Hit.Upper;

		// check if we just hit the threshold
		final double thrs = cfgTheshold.getContent();
		if (lastVal < thrs && val >= thrs || lastVal > thrs && val <= thrs)
			switch (lastHit) {
			case Lower:
				value = Val.Above;
				break;
			case Upper:
				value = Val.Below;
				break;
			default:
				break;
			}

		// check if we just exceeded the bounds
		if (lastVal > lower && val <= lower) value = Val.Below;
		if (lastVal < upper && val >= upper) value = Val.Above;
		lastVal = val;

		// set correct output depending on our current state
		switch (value) {
		case Below:
			val = cfgValueBelow.getContent();
			break;
		case Above:
			val = cfgValueAbove.getContent() > 0 ? cfgValueAbove.getContent()
					: val;
			break;
		default:
			val = .0;
			break;
		}

		if (isVisualize()) {
			plot(0, val);
			plot(1, lower);
			plot(2, upper);
			plot(3, thrs);
		}
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Threshold Converter";
		case Description:
			return "Sends one of two different values depending on whether the "
					+ "currently received value is below or above a specified "
					+ "threshold, while a passing of the threshold-value is "
					+ "ignored unless a specifiable margin has been exceeded.";
		case Config1:
			return "The threshold-value (is treated as an absolute value)";
		case Config2:
			return "The size of the marginal area around the threshold-value";
		case Config3:
			return "The value which will be send if the current one is "
					+ "below the threshold";
		case Config4:
			return "The value which will be send if the current one is "
					+ "above the threshold - if this is 0, the current "
					+ "one itself will be sent";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		if (2 * cfgMarginalArea.getContent() > cfgTheshold.getContent())
			return "Marginal Area must be at most half of threshold";
		if (cfgValueBelow.getContent() > cfgTheshold.getContent()
				- cfgMarginalArea.getContent() / 2)
			return "Value for Below must not be larger than the "
					+ "lower marginal area bound";
		if (cfgValueAbove.getContent() != 0) {
			if (cfgValueAbove.getContent() < cfgTheshold.getContent()
					+ cfgMarginalArea.getContent() / 2)
				return "Value for Above must not be smaller than the "
						+ "higher marginal area bound";
			if (cfgValueAbove.getContent() <= cfgValueBelow.getContent())
				return "Value for Above must be larger than Value for Below";
		}
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 4;
	}

}
