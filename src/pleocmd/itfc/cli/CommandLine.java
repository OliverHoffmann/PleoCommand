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
		/*
		 * final Pipe p = new Pipe(); // p.addInput(new FileInput(new
		 * File("in-bin"), ReadType.Binary)); final Input in = new FileInput();
		 * ((ConfigPath) in.getConfig().get(0)).setContent("in-ascii");
		 * ((ConfigEnum) in.getConfig().get(1)).setContent(ReadType.Ascii
		 * .ordinal()); p.addInput(in); p.addConverter(new
		 * PleoMonitorCMDConverter()); p.addConverter(new SimpleConverter());
		 * p.addConverter(new EmotionConverter()); p.addOutput(new
		 * FileOutput(new File("out-bin"), PrintType.DataBinary));
		 * p.addOutput(new FileOutput(new File("out-ascii"),
		 * PrintType.DataAscii)); p.addOutput(new
		 * ConsoleOutput(PrintType.CommandHumanReadable)); p.addOutput(new
		 * PleoRXTXOutput()); try { p.configuredAll(); p.initializeAll(); }
		 * catch (final PipeException e) { e.printStackTrace(); return; }
		 * p.pipeAllData(); p.closeAll();
		 */
	}

}
