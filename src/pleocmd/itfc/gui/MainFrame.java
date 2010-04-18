package pleocmd.itfc.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.ConfigInt;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.StateException;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.pipe.Pipe;
import pleocmd.pipe.PipePartDetection;

/**
 * @author oliver
 */
public final class MainFrame extends JFrame implements ConfigurationInterface {

	private static final long serialVersionUID = 7174844214646208915L;

	private static MainFrame guiFrame;

	private static boolean hasGUI;

	private final ConfigBounds cfgBounds = new ConfigBounds("Bounds");

	private final ConfigInt cfgSplitterPos = new ConfigInt("Splitter Position",
			-1);

	private final MainPipePanel mainPipePanel;

	private final MainLogPanel mainLogPanel;

	private final MainInputPanel mainInputPanel;

	private final JSplitPane splitPane;

	private final JButton btnHelp;

	private final JLabel lblStatus;

	private final JButton btnExit;

	private final List<AutoDisposableWindow> knownWindows;

	private final Pipe pipe;

	private Thread pipeThread;

	private final Map<Object, String> statusMessages;

	private MainFrame() {
		// don't change the order of the following lines !!!
		// we need this order to avoid race conditions
		knownWindows = new ArrayList<AutoDisposableWindow>();
		statusMessages = new HashMap<Object, String>();
		guiFrame = this;
		mainLogPanel = new MainLogPanel();
		pipe = new Pipe(Configuration.getMain());
		hasGUI = true;
		mainInputPanel = new MainInputPanel();
		mainPipePanel = new MainPipePanel();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		ToolTipManager.sharedInstance().setInitialDelay(50);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setReshowDelay(Integer.MAX_VALUE);

		Log.detail("Creating GUI-Frame");
		setTitle("PleoCommand");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exit();
			}
		});

		// Add components
		final Layouter lay = new Layouter(this);

		lay.addWholeLine(mainPipePanel, false);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				mainLogPanel, mainInputPanel);
		splitPane.setResizeWeight(0.75);

		lay.addWholeLine(splitPane, true);

		btnHelp = lay.addButton(Button.Help, Layouter.help(this, getClass()
				.getSimpleName()));
		lblStatus = new JLabel("...", SwingConstants.CENTER);
		lay.add(lblStatus, true);
		btnExit = lay.addButton("Exit", "application-exit",
				"Cancels running pipe (if any) and exits the application",
				new Runnable() {
					@Override
					public void run() {
						exit();
					}
				});

		pack();
		setLocationRelativeTo(null);
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}

		Log.detail("GUI-Frame created");

		PipePartDetection.checkStaticValidity();
	}

	public void showModalGUI() {
		Log.info("Application started");
		updateState();
		HelpDialog.closeHelpIfOpen();
		setVisible(true);
	}

	public static MainFrame the() {
		if (guiFrame == null) new MainFrame();
		return guiFrame;
	}

	public static boolean hasGUI() {
		return hasGUI;
	}

	public MainPipePanel getMainPipePanel() {
		return mainPipePanel;
	}

	public MainLogPanel getMainLogPanel() {
		return mainLogPanel;
	}

	public MainInputPanel getMainInputPanel() {
		return mainInputPanel;
	}

	public void exit() {
		if (isPipeRunning()) {
			if (JOptionPane.showOptionDialog(this,
					"The pipe is still running. Exiting "
							+ "will abort the pipe.", "Error",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, null) != JOptionPane.YES_OPTION) return;
			abortPipeThread();
		}
		try {
			Configuration.getMain().unregisterConfigurableObject(this);
			Configuration.getMain().writeToDefaultFile();
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
		dispose();

		// dispose all other dialogs and frames, so that the
		// Java AWT thread can exit cleanly
		// special case: ErrorDialog will still be shown if it has unread
		// messages (need copy because of concurrent modifications)
		final List<AutoDisposableWindow> copy = new ArrayList<AutoDisposableWindow>(
				knownWindows);
		for (final AutoDisposableWindow wnd : copy)
			wnd.autoDispose();
		ErrorDialog.canDisposeIfHidden();
		HelpDialog.closeHelpIfOpen();
		Log.detail("GUI-Frame has been closed");

		final Thread thr = new Thread("Fallback Exit Thread") {
			@Override
			public void run() {
				ErrorDialog dlg;
				while ((dlg = ErrorDialog.the()) != null && dlg.isVisible())
					try {
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						// just ignore
					}
				try {
					Thread.sleep(3000);
				} catch (final InterruptedException e) {
					// just ignore
				}
				System.err.println("Application has not been shut down "
						+ "normally but has to be exited forcefully.");
				System.exit(0);
			}
		};
		thr.setDaemon(true);
		thr.start();
	}

	public synchronized void startPipeThread() {
		if (isPipeRunning())
			throw new IllegalStateException("Pipe-Thread already running");
		pipeThread = new Thread("Pipe-Thread") {
			@Override
			public void run() {
				try {
					StandardInput.the().resetCache();
					getPipe().configure();
					getPipe().pipeAllData();
				} catch (final Throwable t) { // CS_IGNORE
					Log.error(t);
				}
				resetPipeThread();
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateState();
					}
				});
			}
		};
		updateState();
		pipeThread.start();
	}

	public synchronized void abortPipeThread() {
		if (!isPipeRunning())
			throw new IllegalStateException("Pipe-Thread not running");
		try {
			pipe.abortPipe();
		} catch (final InterruptedException e) {
			Log.error(e);
		} catch (final StateException e) {
			Log.error(e);
		}
	}

	public void updateState() {
		// update all which depend on isPipeRunning()
		btnHelp.setEnabled(true);
		btnExit.setEnabled(!isPipeRunning());
		getMainPipePanel().updateState();
		getMainLogPanel().updateState();
		getMainInputPanel().updateState();
	}

	public void updateStatusLabel(final Object caller, final String text) {
		if (text == null || text.equals(statusMessages.get(caller))) return;
		statusMessages.put(caller, text);
		final StringBuilder sb = new StringBuilder();
		for (final String s : statusMessages.values()) {
			sb.append(s);
			sb.append(" ");
		}
		lblStatus.setText(sb.toString().trim());
	}

	protected synchronized void resetPipeThread() {
		pipeThread = null;
	}

	public synchronized boolean isPipeRunning() {
		return pipeThread != null;
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(cfgBounds).add(cfgSplitterPos);
	}

	@Override
	public void configurationAboutToBeChanged() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		cfgBounds.assignContent(this);
		splitPane.setDividerLocation(cfgSplitterPos.getContent());
	}

	@Override
	public List<Group> configurationWriteback() throws ConfigurationException {
		cfgBounds.setContent(getBounds());
		cfgSplitterPos.setContent(splitPane.getDividerLocation());
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

	public void addKnownWindow(final AutoDisposableWindow wnd) {
		knownWindows.add(wnd);
	}

	public void removeKnownWindow(final AutoDisposableWindow wnd) {
		knownWindows.remove(wnd);
	}

	public Pipe getPipe() {
		return pipe;
	}

}
