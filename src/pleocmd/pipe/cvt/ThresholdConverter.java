package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.RunnableWithArgument;
import pleocmd.cfg.ConfigBoolean;
import pleocmd.cfg.ConfigDouble;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleBoolData;
import pleocmd.pipe.data.SingleFloatData;

public final class ThresholdConverter extends Converter { // NO_UCD

	private final ConfigDouble cfgThreshold;

	private final ConfigDouble cfgMarginalArea;

	private final ConfigDouble cfgValueBelow;

	private final ConfigDouble cfgValueAbove;

	private final ConfigBoolean cfgReturnBool;

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
		addConfig(cfgThreshold = new ConfigDouble("Threshold", 10, 0,
				Double.MAX_VALUE));
		addConfig(cfgMarginalArea = new ConfigDouble("Marginal Area", 3, 0,
				Double.MAX_VALUE));
		addConfig(cfgValueBelow = new ConfigDouble("Value For Below", 0, 0,
				Double.MAX_VALUE));
		addConfig(cfgValueAbove = new ConfigDouble(
				"Value For Above (0 == current)", 0, 0, Double.MAX_VALUE));
		addConfig(cfgReturnBool = new ConfigBoolean("Return Boolean As Value",
				false));
		cfgThreshold.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final double thres = (Double) args[0];
				double margin = getCfgMarginalArea().getContentGUI();
				final double below = getCfgValueBelow().getContentGUI();
				final double above = getCfgValueAbove().getContentGUI();
				if (2 * margin > thres) {
					getCfgMarginalArea().setContentGUI(thres / 2);
					margin = thres / 2;
				}
				if (below > thres - margin / 2)
					getCfgValueBelow().setContentGUI(
							Math.max(0, thres - margin / 2));
				if (above != 0 && above < thres + margin / 2)
					getCfgValueAbove().setContentGUI(thres + margin / 2);
				return null;
			}
		});
		cfgMarginalArea.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				double thres = getCfgThreshold().getContentGUI();
				final double margin = (Double) args[0];
				final double below = getCfgValueBelow().getContentGUI();
				final double above = getCfgValueAbove().getContentGUI();
				if (2 * margin > thres) {
					getCfgThreshold().setContentGUI(margin * 2);
					thres = margin * 2;
				}
				if (below > thres - margin / 2)
					getCfgValueBelow().setContentGUI(
							Math.max(0, thres - margin / 2));
				if (above != 0 && above < thres + margin / 2)
					getCfgValueAbove().setContentGUI(thres + margin / 2);
				return null;
			}
		});
		cfgValueBelow.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				double thres = getCfgThreshold().getContentGUI();
				double margin = getCfgMarginalArea().getContentGUI();
				final double below = (Double) args[0];
				final double above = getCfgValueAbove().getContentGUI();
				if (below > thres - margin / 2) {
					thres = below + margin / 2;
					getCfgThreshold().setContentGUI(thres);
					if (2 * margin > thres) {
						getCfgMarginalArea().setContentGUI(thres / 2);
						margin = thres / 2;
					}
				}
				if (above != 0
						&& (above <= below || above < thres + margin / 2))
					getCfgValueAbove().setContentGUI(thres + margin / 2);
				return null;
			}
		});
		cfgValueAbove.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				double thres = getCfgThreshold().getContentGUI();
				double margin = getCfgMarginalArea().getContentGUI();
				final double below = getCfgValueBelow().getContentGUI();
				final double above = (Double) args[0];
				if (above != 0 && above < thres + margin / 2) {
					thres = Math.max(0, above - margin / 2);
					getCfgThreshold().setContentGUI(thres);
					if (2 * margin > thres) {
						getCfgMarginalArea().setContentGUI(thres / 2);
						margin = thres / 2;
					}
				}
				if (above != 0 && above <= below || below > thres - margin / 2)
					getCfgValueBelow().setContentGUI(
							Math.max(0, thres - margin / 2));
				return null;
			}
		});
		cfgReturnBool.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final boolean retBool = (Boolean) args[0];
				getCfgValueBelow().setGUIEnabled(!retBool);
				getCfgValueAbove().setGUIEnabled(!retBool);
				return null;
			}
		});
		constructed();
	}

	@Override
	protected void init0() {
		upper = cfgThreshold.getContent() + cfgMarginalArea.getContent() / 2;
		lower = cfgThreshold.getContent() - cfgMarginalArea.getContent() / 2;
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
		return cfgReturnBool.getContent() ? SingleBoolData.IDENT
				: SingleFloatData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("|>%s±%s| => %s : %s", cfgThreshold.asString(),
				String.valueOf(cfgMarginalArea.getContent() / 2), cfgReturnBool
						.getContent() ? "false" : cfgValueBelow.asString(),
				cfgReturnBool.getContent() ? "true" : cfgValueAbove
						.getContent() > 0 ? cfgValueAbove.asString() : "<cur>");
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
		final double thrs = cfgThreshold.getContent();
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
		if (cfgReturnBool.getContent()) {
			final boolean out = value == Val.Above;
			if (isVisualize()) {
				plot(0, out ? 1 : 0);
				plot(1, lower);
				plot(2, upper);
				plot(3, thrs);
			}
			return asList(SingleBoolData.create(out, data));
		}
		final double out;
		switch (value) {
		case Below:
			out = cfgValueBelow.getContent();
			break;
		case Above:
			out = cfgValueAbove.getContent() > 0 ? cfgValueAbove.getContent()
					: val;
			break;
		default:
			out = .0;
			break;
		}

		if (isVisualize()) {
			plot(0, out);
			plot(1, lower);
			plot(2, upper);
			plot(3, thrs);
		}
		return asList(SingleFloatData.create(out, data));
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
		case Config5:
			return "If true, a boolean value (true for above, false for below) "
					+ "will be returned instead of the float from the fields above";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		final double thres = cfgThreshold.getContent();
		final double margin = cfgMarginalArea.getContent();
		final double below = cfgValueBelow.getContent();
		final double above = cfgValueAbove.getContent();
		if (2 * margin > thres)
			return "Marginal Area must be at most half of threshold";
		if (below > thres - margin / 2)
			return "Value for Below must not be larger than the "
					+ "lower marginal area bound";
		if (above != 0) {
			if (above < thres + margin / 2)
				return "Value for Above must not be smaller than the "
						+ "higher marginal area bound";
			if (above <= below)
				return "Value for Above must be larger than Value for Below";
		}
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 4;
	}

	protected ConfigDouble getCfgThreshold() {
		return cfgThreshold;
	}

	protected ConfigDouble getCfgMarginalArea() {
		return cfgMarginalArea;
	}

	protected ConfigDouble getCfgValueBelow() {
		return cfgValueBelow;
	}

	protected ConfigDouble getCfgValueAbove() {
		return cfgValueAbove;
	}

	public ConfigBoolean getCfgReturnBool() {
		return cfgReturnBool;
	}

}
