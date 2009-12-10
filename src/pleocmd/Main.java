package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.GUIFrame;

/**
 * TODO: <br>
 * implement DataFilter and CommandFilter<br>
 * check terminology (Value, Data, Block, Command, Sequence)<br>
 */

public final class Main {

	private Main() {
		// utility class => hidden
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) throws Exception {
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
