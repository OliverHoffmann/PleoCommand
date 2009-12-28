package pleocmd;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

/**
 * TODO: List of tasks:<br>
 * only enable InputPanel if a ConsoleInput is in the pipe<br>
 * new input: direct-input which directly uses configured Data<br>
 * checkstyle: no exception handling in pipe's, only pass them thru<br>
 * implement DataFilter<br>
 * check terminology (Value, Data, Block, Sequence, Ascii <-> ASCII)<br>
 * add javadoc<br>
 */

public final class Main {

	private Main() {
		// utility class => hidden
	}

	public static void main(final String[] args) {
		if (args.length > 0)
			CommandLine.the().parse(args);
		else
			new Thread(new ThreadGroup("Exception-handling Group") {
				@Override
				public void uncaughtException(final Thread thread,
						final Throwable throwable) {
					// we need to print the Exception additionally to
					// logging it because logging itself may have caused
					// the exception or it just is not yet initialized
					throwable.printStackTrace(); // CS_IGNORE
					Log.error(throwable);
				}
			}, "GUI-Thread") {
				@Override
				public void run() {
					MainFrame.the().showModalGUI();
				}
			}.start();
	}

}
