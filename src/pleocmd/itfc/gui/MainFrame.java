package pleocmd.itfc.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;

import pleocmd.Log;
import pleocmd.StandardInput;
import pleocmd.pipe.Pipe;

/**
 * @author oliver
 */
public final class MainFrame extends JFrame {

	private static final long serialVersionUID = 7174844214646208915L;

	private static final File PIPE_CONFIG_FILE = new File(System
			.getProperty("user.home")
			+ "/.pleocommand.pca");

	private static MainFrame guiFrame;

	private final Pipe pipe;

	private final MainPipePanel mainPipePanel;

	private final MainLogPanel mainLogPanel;

	private final MainInputPanel mainInputPanel;

	private final JSplitPane splitPane;

	private final JButton btnExit;

	private Thread pipeThread;

	private MainFrame() {
		guiFrame = this;
		mainLogPanel = new MainLogPanel();
		mainInputPanel = new MainInputPanel();
		pipe = new Pipe();
		mainPipePanel = new MainPipePanel(pipe);

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

		lay.addSpacer();
		btnExit = lay.addButton("Exit", "application-exit",
				"Cancels running pipe (if any) and exits the application",
				new Runnable() {
					@Override
					public void run() {
						exit();
					}
				});

		// Center window on screen
		setSize(800, 500);
		setLocationRelativeTo(null);

		// Load default configuration
		mainPipePanel.readConfigFromFile(PIPE_CONFIG_FILE);
		updateState();

		Log.detail("GUI-Frame created");
		Log.info("Application started");
		setVisible(true);
	}

	public static MainFrame the() {
		if (guiFrame == null) new MainFrame();
		return guiFrame;
	}

	public static boolean hasGUI() {
		return guiFrame != null;
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

	public List<String> getHistory() {
		return mainInputPanel.getHistory();
	}

	public void addLog(final Log log) {
		mainLogPanel.addLog(log);
	}

	public void exit() {
		Log.detail("GUI-Frame has been closed");
		mainPipePanel.writeConfigToFile(PIPE_CONFIG_FILE);
		// TODO show message if pipe still running
		// guiFrame = null;
		dispose();
	}

	public synchronized void startPipeThread() {
		if (isPipeRunning())
			throw new IllegalStateException("Pipe-Thread already running");
		pipeThread = new Thread("Pipe-Thread") {
			@Override
			public void run() {
				pipeCore();
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

	protected void pipeCore() {
		try {
			StandardInput.the().resetCache();
			pipe.configure();
			pipe.init();
			pipe.pipeAllData();
			pipe.close();
		} catch (final Throwable t) { // CS_IGNORE
			Log.error(t);
		}
	}

	public void updateState() {
		btnExit.setEnabled(!isPipeRunning());
		getMainPipePanel().updateState();
		getMainLogPanel().updateState();
		getMainInputPanel().updateState();
	}

	protected synchronized void resetPipeThread() {
		pipeThread = null;
	}

	public synchronized boolean isPipeRunning() {
		return pipeThread != null;
	}

}
