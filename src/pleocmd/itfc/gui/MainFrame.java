package pleocmd.itfc.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;

import pleocmd.Log;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.Pipe;

/**
 * @author oliver
 */
public final class MainFrame extends JFrame {

	private static final long serialVersionUID = 7174844214646208915L;

	private static final File PIPE_CONFIG_FILE = new File(System
			.getProperty("user.home")
			+ "/.pleocommand.pipe");

	private static MainFrame guiFrame;

	private final Pipe pipe = new Pipe();

	private final MainPipePanel mainPipePanel;

	private final MainLogPanel mainLogPanel;

	private final MainInputPanel mainInputPanel;

	private final JSplitPane splitPane;

	private MainFrame() {
		guiFrame = this;

		ToolTipManager.sharedInstance().setInitialDelay(50);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setReshowDelay(Integer.MAX_VALUE);

		mainPipePanel = new MainPipePanel(pipe);
		mainLogPanel = new MainLogPanel(pipe);
		mainInputPanel = new MainInputPanel();

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
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = ConfigFrame.initGBC();
		gbc.gridy = 0;
		gbc.weighty = 0.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(mainPipePanel, gbc);

		++gbc.gridy;

		gbc.weighty = 1.0;
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				mainLogPanel, mainInputPanel);
		splitPane.setResizeWeight(0.75);
		add(splitPane, gbc);

		++gbc.gridy;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;

		gbc.weightx = 1.0;
		add(new JLabel(), gbc);
		gbc.weightx = 0.0;

		final JButton btnExit = new JButton("Exit", IconLoader
				.getIcon("application-exit.png"));
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				exit();
			}
		});
		add(btnExit, gbc);

		// Center window on screen
		setSize(800, 500);
		setLocationRelativeTo(null);

		// Load default configuration
		mainPipePanel.readConfigFromFile(PIPE_CONFIG_FILE);

		Log.detail("GUI-Frame created");
		setVisible(true);
	}

	public static MainFrame the() {
		if (guiFrame == null) new MainFrame();
		return guiFrame;
	}

	public static boolean hasGUI() {
		return guiFrame != null;
	}

	public void addLog(final Log log) {
		mainLogPanel.addLog(log);
	}

	public List<String> getHistory() {
		return mainInputPanel.getHistory();
	}

	public void exit() {
		Log.detail("GUI-Frame has been closed");
		mainPipePanel.writeConfigToFile(PIPE_CONFIG_FILE);
		guiFrame = null;
		dispose();
	}

}
