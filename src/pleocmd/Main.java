package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

/**
 * TODO: <br>
 * implement DataFilter<br>
 * check terminology (Value, Data, Block, Sequence)<br>
 * set filetypes for open/save dialogs<br>
 * term: loadFrom / readFrom<br>
 * checkstyle: no exception handling in pipe's, only pass them thru<br>
 * exit while pipe is running?<br>
 * detect endless looping in convert()<br>
 * introduce priority system<br>
 * split pipe in 3 threads: in, cvt and out<br>
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
