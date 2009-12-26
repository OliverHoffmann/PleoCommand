package pleocmd.pipe.data;

import java.util.ArrayList;
import java.util.List;

import pleocmd.pipe.val.Value;

public abstract class AbstractDataConverter {

	private final List<Value> values;

	private byte priority;

	private long time;

	public AbstractDataConverter() {
		values = new ArrayList<Value>(8);
		priority = Data.PRIO_DEFAULT;
		time = Data.TIME_NOTIME;
	}

	public AbstractDataConverter(final Data data) {
		values = data;
		priority = data.getPriority();
		time = data.getTime();
	}

	public final List<Value> getValues() {
		return values;
	}

	protected final void trimValues() {
		if (values instanceof ArrayList<?>)
			((ArrayList<?>) values).trimToSize();
	}

	public final byte getPriority() {
		return priority;
	}

	protected final void setPriority(final byte priority) {
		this.priority = priority;
	}

	public final long getTime() {
		return time;
	}

	protected final void setTime(final long time) {
		this.time = time;
	}

	public final Data createDataFromFields() {
		return new Data(values, null, priority, time);
	}

}
