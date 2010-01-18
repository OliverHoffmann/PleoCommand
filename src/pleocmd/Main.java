package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.DataSequenceEditorFrame;
import pleocmd.itfc.gui.MainFrame;

/**
 * TODO: List of tasks:<br>
 * make all Data timed Data before writing to Output<br>
 * checkstyle: no exception handling in pipe's, only pass them thru<br>
 * implement DataFilter<br>
 * check terminology (Value, Data, Block, Sequence, Ascii <-> ASCII)<br>
 * add javadoc<br>
 * <br>
 * more Log.debug<br>
 * display power status<br>
 * disable "OK" and "Apply" if bad input in PipePart configuration<br>
 * online help<br>
 * combine History and {@link DataSequenceEditorFrame}<br>
 * make dialogs non-modal?<br>
 */

public final class Main {

	private Main() {
		// utility class => hidden
	}

	public static void main(final String[] args) {
		try {
			System.setProperty("java.library.path", System
					.getProperty("java.library.path")
					+ ":.");
			System.setProperty("sun.awt.exception.handler",
					MainExceptionHandler.class.getName());

			if (args.length > 0)
				CommandLine.the().parse(args);
			else
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						MainFrame.the().showModalGUI();
					}
				});
		} catch (final Throwable t) { // CS_IGNORE catch all here
			t.printStackTrace(); // CS_IGNORE logging may not work here
			Log.error(t);
		}
	}

}
