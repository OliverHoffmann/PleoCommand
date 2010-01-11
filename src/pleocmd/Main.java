package pleocmd;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

/**
 * TODO: List of tasks:<br>
 * make all Data timed Data before writing to Output<br>
 * only enable InputPanel if a ConsoleInput is in the pipe<br>
 * checkstyle: no exception handling in pipe's, only pass them thru<br>
 * implement DataFilter<br>
 * check terminology (Value, Data, Block, Sequence, Ascii <-> ASCII)<br>
 * add javadoc<br>
 * <br>
 * more Log.debug<br>
 */

public final class Main {

	private Main() {
		// utility class => hidden
	}

	public static void main(final String[] args) {
		System.setProperty("sun.awt.exception.handler",
				MainExceptionHandler.class.getName());
		if (args.length > 0)
			CommandLine.the().parse(args);
		else
			new Thread(new ThreadGroup("Exception-handling Group") {
				@Override
				public void uncaughtException(final Thread thread,
						final Throwable thrown) {
					new MainExceptionHandler().handle(thrown);
				}
			}, "GUI-Thread") {
				@Override
				public void run() {
					MainFrame.the().showModalGUI();
				}
			}.start();
	}

}
