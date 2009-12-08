package pleocmd.pipe.cmd;

import pleocmd.pipe.Data;

public abstract class Command {

	private final Data data;

	/**
	 * Creates a new {@link Command} which belongs to a sequence of commands
	 */
	public Command() {
		data = null;
	}

	/**
	 * Creates a single {@link Command} or the first command of a sequence of
	 * commands.
	 * 
	 * @param data
	 *            the {@link Data} of which this command has been created.
	 */
	public Command(final Data data) {
		this.data = data;
	}

	/**
	 * @return the {@link Data} of which this command has been created or
	 *         <b>null</b> if this {@link Command} belongs to a sequence of
	 *         commands which are all associated with one {@link Data}.
	 */
	public final Data getData() {
		return data;
	}

	@Override
	public abstract String toString();

	public abstract boolean canBeSendToPleo();

	public abstract String asPleoMonitorCommand();

}
