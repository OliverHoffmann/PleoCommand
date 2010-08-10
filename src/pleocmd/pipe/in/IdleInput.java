package pleocmd.pipe.in;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.Log;
import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InputException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleBoolData;

public final class IdleInput extends Input { // NO_UCD

	private final ConfigDouble cfgProbability;

	private final ConfigInt cfgDelayIdle;

	private final ConfigInt cfgDelayRepeat;

	private final ConfigInt cfgPriority;

	private final ConfigInt cfgUserData;

	private boolean first;

	private long last;

	public IdleInput() {
		addConfig(cfgProbability = new ConfigDouble("Probability", 0.75, 0, 1));
		addConfig(cfgDelayIdle = new ConfigInt("Idle-Delay", 4000, 0,
				Integer.MAX_VALUE));
		addConfig(cfgDelayRepeat = new ConfigInt("Repeat-Delay", 1000, 10,
				Integer.MAX_VALUE));
		addConfig(cfgPriority = new ConfigInt("Priority", Data.PRIO_LOWEST
				+ (Data.PRIO_DEFAULT - Data.PRIO_LOWEST) / 2, Data.PRIO_LOWEST,
				Data.PRIO_DEFAULT - 1));
		addConfig(cfgUserData = new ConfigInt("UserData", 0));
		constructed();
	}

	public IdleInput(final double probability, final int delayIdle,
			final int delayRepeat, final int priority, final int userData)
			throws ConfigurationException {
		this();
		cfgProbability.setContent(probability);
		cfgDelayIdle.setContent(delayIdle);
		cfgDelayRepeat.setContent(delayRepeat);
		cfgPriority.setContent(priority);
		cfgUserData.setContent(userData);
	}

	@Override
	public String getOutputDescription() {
		return "bool";
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("[P%02d] %dms + %dms * %.2f", cfgPriority
				.getContent(), cfgDelayIdle.getContent(), cfgDelayRepeat
				.getContent(), cfgProbability.getContent());
	}

	@Override
	protected void init0() throws InputException, IOException {
		first = true;
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (first) {
			// we've been called from the main input thread
			first = false;
			// create a second input thread with only this instance as an input
			final List<Input> l = new ArrayList<Input>();
			l.add(this);
			getPipe().createNewInputThread(l).start();
			// don't do anything in this thread - just signal "finished"
			return null;
		}

		// as we only create background data, we should exit
		// ourself once the main input thread has been closed.
		if (getPipe().isMainInputThreadFinished()) {
			Log.info("Ending Idle-Input as Main-Input-Thread closed");
			return null;
		}

		try {
			final long now = System.currentTimeMillis();
			final long next = last + cfgDelayRepeat.getContent();
			if (next > now) {
				Log.detail("Artificially slow down by %d ms", next - now);
				Thread.sleep(next - now);
			}
		} catch (final InterruptedException e) {
			Log.detail("Slow down interrupted");
			return null;
		}
		last = System.currentTimeMillis();
		final long idle = last
				- getPipe().getFeedback().getLastNormalDataOutput();
		return new SingleBoolData(idle > cfgDelayIdle.getContent()
				&& Math.random() < cfgProbability.getContent(), cfgUserData
				.getContent(), null, cfgPriority.getContent().byteValue(),
				Data.TIME_NOTIME);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Idle Input";
		case Description:
			return "Creates boolean Data blocks while the Pipe is idle";
		case Config1:
			return "Probability to send 'true' instead of 'false' in the Data blocks";
		case Config2:
			return "Number of ms the pipe must be idle before sending "
					+ "the first Data block";
		case Config3:
			return "Number of ms between two Data blocks while the Pipe is idle";
		case Config4:
			return "Priority of all sent Data Blocks";
		case Config5:
			return "Some Integer which is sent as an additional argument in "
					+ "the Data blocks (mostly used for channel number)";
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
		return 0;
	}

}
