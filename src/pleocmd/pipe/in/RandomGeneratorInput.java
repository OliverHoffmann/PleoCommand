package pleocmd.pipe.in;

import java.io.IOException;

import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigInt;
import pleocmd.exc.InputException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class RandomGeneratorInput extends Input {

	private final ConfigInt cfgUserData;
	private final ConfigInt cfgSamplerate;
	private final ConfigDouble cfgPeakPropbability;
	private final ConfigDouble cfgMaxAmplitude;
	private final ConfigDouble cfgMaxPeakLength;
	private final ConfigDouble cfgMaxGrad0Length;
	private final ConfigDouble cfgMaxGrad1Length;
	private final ConfigDouble cfgMaxNoise;

	private enum PeakPos {
		NoPeak, Grad0, OnPeak, Grad1
	}

	private long last;
	private PeakPos peakPos;
	private double amp;
	private int peakLen; // in loops
	private int grad0Len; // in loops
	private int grad1Len; // in loops
	private double grad0Inc;
	private double grad1Inc;
	private double value;
	private int step;

	public RandomGeneratorInput() {
		addConfig(cfgUserData = new ConfigInt("User-Data", 0));
		addConfig(cfgSamplerate = new ConfigInt("Samplerate (in Hz)", 10, 1,
				10000));
		addConfig(cfgPeakPropbability = new ConfigDouble("Peak Probability", 0,
				0, 1, 0.01));
		addConfig(cfgMaxAmplitude = new ConfigDouble("Max Amplitude", 1000, 0,
				Double.MAX_VALUE));
		addConfig(cfgMaxPeakLength = new ConfigDouble(
				"Max Length of Peak (in ms)", 2000, 0, Double.MAX_VALUE));
		addConfig(cfgMaxGrad0Length = new ConfigDouble(
				"Max Length of First Gradient (in ms)", 2000, 0,
				Double.MAX_VALUE));
		addConfig(cfgMaxGrad1Length = new ConfigDouble(
				"Max Length of Second Gradient (in ms)", 2000, 0,
				Double.MAX_VALUE));
		addConfig(cfgMaxNoise = new ConfigDouble("Max Noise", 300, 0,
				Double.MAX_VALUE));
		constructed();
	}

	@Override
	protected void init0() throws IOException {
		last = 0;
		peakPos = PeakPos.NoPeak;
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null)
			ds.setLabel(String.format("Random [0-%s]", cfgMaxAmplitude
					.getContent()));
	}

	@Override
	public String getOutputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("%s@p=%s ~%s", cfgMaxAmplitude.asString(),
				cfgPeakPropbability.asString(), cfgMaxNoise.asString());
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		try {
			final long next = last + 1000 / cfgSamplerate.getContent();
			final long now = System.currentTimeMillis();
			if (next > now) Thread.sleep(next - now);
		} catch (final InterruptedException e) {
			return null;
		}
		last = System.currentTimeMillis();
		final double d;
		switch (peakPos) {
		case NoPeak:
			d = .0;
			if (Math.random() < cfgPeakPropbability.getContent()) {
				peakPos = PeakPos.Grad0;
				amp = rand11() * cfgMaxAmplitude.getContent();
				final double samplesPerMS = cfgSamplerate.getContent() / 1000.0;
				peakLen = Math.max(1, rand0N(cfgMaxPeakLength.getContent()
						* samplesPerMS));
				grad0Len = Math.max(1, rand0N(cfgMaxGrad0Length.getContent()
						* samplesPerMS));
				grad1Len = Math.max(1, rand0N(cfgMaxGrad1Length.getContent()
						* samplesPerMS));
				grad0Inc = amp / grad0Len;
				grad1Inc = amp / grad1Len;
				value = 0;
				step = 0;
			}
			break;
		case Grad0:
			d = value += grad0Inc;
			if (++step >= grad0Len) {
				step = 0;
				peakPos = PeakPos.OnPeak;
			}
			break;
		case OnPeak:
			d = amp; // assert(value == amp)
			if (++step >= peakLen) {
				step = 0;
				peakPos = PeakPos.Grad1;
			}
			break;
		case Grad1:
			d = value -= grad1Inc;
			if (++step >= grad1Len) peakPos = PeakPos.NoPeak;
			break;
		default:
			return null;
		}

		final double val = d + rand11() * cfgMaxNoise.getContent();
		if (isVisualize()) plot(0, val);
		return new SingleFloatData(val, cfgUserData.getContent(), null);
	}

	private static double rand11() {
		return 1 - 2 * Math.random();
	}

	private static int rand0N(final double max) {
		return (int) (Math.random() * max);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Random Generator";
		case Description:
			return "Generates noise with some random peaks";
		case Config1:
			return "Some integer which is sent as an additional argument in "
					+ "the Data packets (mostly used for channel number)";
		case Config2:
			return "Number of Data packets created per second";
		case Config3:
			return "The probability to start a new peak on a Data packet "
					+ "(if 0 there are no peaks at all, if 1 a peak is started "
					+ "immediately after the last one)";
		case Config4:
			return "The maximum possible (absolute) value to send during a peak";
		case Config5:
			return "The maximum possible length of one peak in ms (rounded "
					+ "down to the next value possible with the current samplerate)";
		case Config6:
			return "The maximum possible length from the start to the highest point "
					+ "of a peak in ms (rounded  down to the next value possible "
					+ "with the current samplerate)";
		case Config7:
			return "The maximum possible length from the highest point to the end "
					+ "of a peak in ms (rounded  down to the next value possible "
					+ "with the current samplerate)";
		case Config8:
			return "The maximum possible random noise added to / subtracted from "
					+ "each generated Data packet.";
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
