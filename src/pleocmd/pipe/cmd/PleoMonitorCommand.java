package pleocmd.pipe.cmd;

import pleocmd.Log;
import pleocmd.pipe.Data;

public final class PleoMonitorCommand extends Command {

	private final String command;

	public PleoMonitorCommand(final Data data, final String command) {
		super(data);
		Log.detail("New command " + command);
		this.command = command;
	}

	@Override
	public boolean canBeSendToPleo() {
		return true;
	}

	@Override
	public String asPleoMonitorCommand() {
		return command + "\n";
	}

	@Override
	public String toString() {
		return "\"" + command + "\"";
	}

}
