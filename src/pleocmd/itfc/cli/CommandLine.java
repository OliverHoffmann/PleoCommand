package pleocmd.itfc.cli;

/**
 * @author oliver
 */
public final class CommandLine {

	private static CommandLine commandLine;

	public static CommandLine the() {
		if (commandLine == null) commandLine = new CommandLine();
		return commandLine;
	}

	public void parse(final String[] args) {
		// TODO
	}

}
