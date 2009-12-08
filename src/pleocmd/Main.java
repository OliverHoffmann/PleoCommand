package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.GUIFrame;

/**
 * TODO: <br>
 * implement DataFilter and CommandFilter<br>
 * check that state of Input/Output/Converter is consistent (new base-class?)<br>
 * check terminology (Value, Data, Block, Command, Sequence)<br>
 * all output through class Log
 */

public final class Main {

	private Main() {
		// utility class => hidden
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) throws Exception {
		/*
		 * CommPortIdentifier.addPortName("/dev/ttyACM0",
		 * CommPortIdentifier.PORT_SERIAL, new RXTXCommDriver()); final
		 * PleoCommunication pc = new PleoCommunication(PleoCommunication
		 * .getFirstPort()); pc.init(); pc.send("APP UNLOAD");
		 * System.out.println(pc.readAnswer()); pc.send("JOINT NEUTRAL");
		 * System.out.println(pc.readAnswer());
		 * pc.send("JOINT RANGE 12 2 10 -10"); // nod
		 * System.out.println(pc.readAnswer());
		 * pc.send("JOINT RANGE 11 2 30 -30"); // shake
		 * System.out.println(pc.readAnswer()); pc.close(); System.exit(0);
		 */
		if (args.length > 0)
			CommandLine.the().parse(args);
		else
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GUIFrame.the();
				}
			});
	}

}
