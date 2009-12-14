package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

/**
 * TODO: <br>
 * implement DataFilter and CommandFilter<br>
 * check terminology (Value, Data, Block, Command, Sequence)<br>
 * dblclick on pipepart should call "Modify"<br>
 * disable "Modify" if nothing to configure<br>
 * set filetypes for open/save dialogs<br>
 * dbl-click on history entry to put it into input field<br>
 * term: loadFrom / readFrom<br>
 * checkstyle: no exception handling in pipe's, only pass them thru<br>
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
					MainFrame.the();
				}
			});
	}

}
