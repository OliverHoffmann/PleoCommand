package pleocmd.pipe.cvt;

import java.util.ArrayList;
import java.util.List;

import pleocmd.RunnableWithArgument;
import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class BCICodomainAdaption extends Converter { // NO_UCD

	private enum OutOfRangeBehavior {
		CutOff, FitToRange
	}

	private enum Transformation {
		Linear
	}

	private final ConfigDouble cfgSourceMin;
	private final ConfigDouble cfgSourceMax;
	private final ConfigDouble cfgTargetMin;
	private final ConfigDouble cfgTargetMax;
	private final ConfigEnum<OutOfRangeBehavior> cfgOutOfRange;
	private final ConfigEnum<Transformation> cfgTransformation;

	private double dec;
	private double inc;
	private double fl; // factor for linear transformation

	public BCICodomainAdaption() {
		addConfig(cfgSourceMin = new ConfigDouble("Source-Minimum", -1000));
		addConfig(cfgSourceMax = new ConfigDouble("Source-Maximum", 1000));
		addConfig(cfgTargetMin = new ConfigDouble("Target-Minimum", -64));
		addConfig(cfgTargetMax = new ConfigDouble("Target-Maximum", 64));
		addConfig(cfgOutOfRange = new ConfigEnum<OutOfRangeBehavior>(
				"OutOfRange-Behavior", OutOfRangeBehavior.class));
		addConfig(cfgTransformation = new ConfigEnum<Transformation>(
				"Transformation-Type", Transformation.class));
		cfgSourceMin.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final double min = (Double) args[0];
				final double max = getCfgSourceMax().getContentGUI();
				if (min >= max) getCfgSourceMax().setContentGUI(min + 1);
				return null;
			}
		});
		cfgSourceMax.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final double min = getCfgSourceMin().getContentGUI();
				final double max = (Double) args[0];
				if (min >= max) getCfgSourceMin().setContentGUI(max - 1);
				return null;
			}
		});
		cfgTargetMin.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final double min = (Double) args[0];
				final double max = getCfgTargetMax().getContentGUI();
				if (min >= max) getCfgTargetMax().setContentGUI(min + 1);
				return null;
			}
		});
		cfgTargetMax.setChangingContent(new RunnableWithArgument() {
			@Override
			public Object run(final Object... args) {
				final double min = getCfgTargetMin().getContentGUI();
				final double max = (Double) args[0];
				if (min >= max) getCfgTargetMin().setContentGUI(max - 1);
				return null;
			}
		});
		constructed();
	}

	@Override
	protected void configure1() {
		dec = cfgSourceMin.getContent();
		inc = cfgTargetMin.getContent();
		fl = (cfgTargetMax.getContent() - cfgTargetMin.getContent())
				/ (cfgSourceMax.getContent() - cfgSourceMin.getContent());
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Codomain adapted");
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
		return String.format("[%s-%s]=>[%s-%s]", cfgSourceMin.asString(),
				cfgSourceMax.asString(), cfgTargetMin.asString(), cfgTargetMax
						.asString());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		double val = SingleFloatData.getValue(data);
		switch (cfgOutOfRange.getEnum()) {
		case CutOff:
			// return empty list, so "data" is just dropped
			if (val < cfgSourceMin.getContent()
					|| val > cfgSourceMax.getContent())
				return new ArrayList<Data>(0);
			break;
		case FitToRange:
			if (val < cfgSourceMin.getContent())
				val = cfgSourceMin.getContent();
			if (val > cfgSourceMax.getContent())
				val = cfgSourceMax.getContent();
			break;
		}
		switch (cfgTransformation.getEnum()) {
		case Linear:
			val = (val - dec) * fl + inc;
			break;
		}
		if (isVisualize()) plot(0, val);
		return asList(SingleFloatData.create(val, data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Codomain Adaption";
		case Description:
			return "Reduces or enlarges the range of possible values for a "
					+ "single channel";
		case Config1:
			return "Minimum value of source range";
		case Config2:
			return "Maximum value of source range";
		case Config3:
			return "Minimum value of target range";
		case Config4:
			return "Maximum value of target range";
		case Config5:
			return "Whether to fit values out of the source range to min/max "
					+ "or just drop the data packet";
		case Config6:
			return "Kind of transformation from source range to target range";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		if (cfgSourceMin.getContent() >= cfgSourceMax.getContent())
			return String.format("%d must be less than %d in source range",
					cfgSourceMin.getContent(), cfgSourceMax.getContent());
		if (cfgTargetMin.getContent() >= cfgTargetMax.getContent())
			return String.format("%d must be less than %d in target range",
					cfgTargetMin.getContent(), cfgTargetMax.getContent());
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 1;
	}

	protected ConfigDouble getCfgSourceMin() {
		return cfgSourceMin;
	}

	protected ConfigDouble getCfgSourceMax() {
		return cfgSourceMax;
	}

	protected ConfigDouble getCfgTargetMin() {
		return cfgTargetMin;
	}

	protected ConfigDouble getCfgTargetMax() {
		return cfgTargetMax;
	}

}
