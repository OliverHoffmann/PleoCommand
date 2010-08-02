package pleocmd.pipe;

import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * Fully synchronized class that provides feedback about a {@link Pipe} that is
 * currently running or has recently run.
 * 
 * @author oliver
 */
public final class PipeFeedback extends Feedback {

	private int dataInputCount;

	private int dataConvertedCount;

	private int dataOutputCount;

	private long lastNormalDataOutput;

	PipeFeedback() {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;
	}

	/**
	 * @return number of {@link Data} read from an {@link Input}
	 */
	public synchronized int getDataInputCount() {
		return dataInputCount;
	}

	/**
	 * @return number of {@link Data} passed to a {@link Converter}
	 */
	public synchronized int getDataConvertedCount() {
		return dataConvertedCount;
	}

	/**
	 * @return number of {@link Data} written to an {@link Output}
	 */
	public synchronized int getDataOutputCount() {
		return dataOutputCount;
	}

	synchronized void incDataInputCount() {
		++dataInputCount;
	}

	synchronized void incDataConvertedCount() {
		++dataConvertedCount;
	}

	synchronized void incDataOutputCount(final boolean isNormalData) {
		++dataOutputCount;
		if (isNormalData) lastNormalDataOutput = System.currentTimeMillis();
	}

	public synchronized long getLastNormalDataOutput() {
		return lastNormalDataOutput;
	}

	@Override
	protected String getAdditionalString1() {
		return String.format(
				" has read %d, converted %d and written %d data(s), ",
				getDataInputCount(), getDataConvertedCount(),
				getDataOutputCount());
	}

	@Override
	protected String getAdditionalString2() {
		return "";
	}

	@Override
	protected void addAdditionalHTMLTable1(final StringBuilder sb) {
		appendToHTMLTable(sb, "Data read from Inputs", getDataInputCount());
		appendToHTMLTable(sb, "Data converted in Converters",
				getDataConvertedCount());
		appendToHTMLTable(sb, "Data written from Outputs", getDataOutputCount());
	}

	@Override
	protected void addAdditionalHTMLTable2(final StringBuilder sb) {
		// nothing to do
	}

}
