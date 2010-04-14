package pleocmd;

import java.awt.EventQueue;

import pleocmd.itfc.cli.CommandLine;
import pleocmd.itfc.gui.MainFrame;

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
