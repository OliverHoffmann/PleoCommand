package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.exc.InternalException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Value;

public final class BCICodomainAdaption extends Converter {

	enum OutOfRangeBehavior {
		CutOff, FitToRange
	}

	enum Transformation {
		Linear
	}

	private final ConfigInt cfgSourceMin;
	private final ConfigInt cfgSourceMax;
	private final ConfigInt cfgTargetMin;
	private final ConfigInt cfgTargetMax;
	private final ConfigEnum<OutOfRangeBehavior> cfgOutOfRange;
	private final ConfigEnum<Transformation> cfgTransformation;

	private int dec;
	private int inc;
	private double fl; // factor for linear transformation

	public BCICodomainAdaption() {
		addConfig(cfgSourceMin = new ConfigInt("Source-Minimum", -1000));
		addConfig(cfgSourceMax = new ConfigInt("Source-Maximum", 1000));
		addConfig(cfgTargetMin = new ConfigInt("Target-Minimum", -64));
		addConfig(cfgTargetMax = new ConfigInt("Target-Maximum", 64));
		addConfig(cfgOutOfRange = new ConfigEnum<OutOfRangeBehavior>(
				"OutOfRange-Behavior", OutOfRangeBehavior.class));
		addConfig(cfgTransformation = new ConfigEnum<Transformation>(
				"Transformation-Type", Transformation.class));
		constructed();
	}

	@Override
	protected void configure0() {
		dec = cfgSourceMin.getContent();
		inc = cfgTargetMin.getContent();
		fl = (double) (cfgTargetMax.getContent() - cfgTargetMin.getContent())
				/ (double) (cfgSourceMax.getContent() - cfgSourceMin
						.getContent());
	}

	@Override
	protected void init0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null) ds.setLabel("Codomain adapted");
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public String getInputDescription() {
		return "BCIChannel";
	}

	@Override
	public String getOutputDescription() {
		return "BCIChannel";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!"BCIChannel".equals(data.getSafe(0).asString())) return null;
		final List<Data> res = new ArrayList<Data>(1);
		final List<Value> vals = new ArrayList<Value>(data);
		try {
			double val = data.get(2).asDouble();
			switch (cfgOutOfRange.getEnum()) {
			case CutOff:
				// return empty list, so "data" is just dropped
				if (val < cfgSourceMin.getContent()
						|| val > cfgSourceMax.getContent()) return res;
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
				vals.get(2).set(String.valueOf((val - dec) * fl + inc));
			}
		} catch (final IOException e) {
			throw new InternalException(e);
		}
		res.add(new Data(vals, data));
		if (isVisualize()) plot(0, vals.get(2).asDouble());
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "BCI Codomain Adaption";
		case Description:
			return "Reduces or enlarges the range of possible values for a "
					+ "single BCI channel";
		case Configuration:
			return "1: Minimum value of source range\n"
					+ "2: Maximum value of source range\n"
					+ "3: Minimum value of target range\n"
					+ "4: Maximum value of target range\n"
					+ "5: Whether to fit values out of the source range to min/max "
					+ "or just drop their data packets\n"
					+ "6: Kind of transformation from source range to target range";
		default:
			return "???";
		}
	}

	@Override
	public boolean isConfigurationSane() {
		if (cfgSourceMin.getContent() >= cfgSourceMax.getContent())
			return false;
		if (cfgTargetMin.getContent() >= cfgTargetMax.getContent())
			return false;
		return true;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 1;
	}
}
