package pleocmd.itfc.gui;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import pleocmd.Log;
import pleocmd.itfc.gui.Layouter.Button;
import pleocmd.itfc.gui.help.HelpLoader;

public final class HelpDialog extends JDialog {

	private static final long serialVersionUID = 2506240487807329597L;

	private static HelpDialog helpDialog;

	private final JTextPane tpHelp;

	private final JButton btnBack;

	private final List<URL> history = new ArrayList<URL>();

	private HelpDialog(final Window owner) {
		super(owner);
		helpDialog = this;
		setTitle("Help");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				closeHelp();
			}
		});

		final Layouter lay = new Layouter(this);

		tpHelp = new JTextPane();
		tpHelp.setEditable(false);
		tpHelp.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED)
					display(new File(e.getURL().getPath()).getName());
			}
		});
		lay.addWholeLine(new JScrollPane(tpHelp), true);

		lay.addButton("Index", "help-contents",
				"Display an overview of important help texts", new Runnable() {
					@Override
					public void run() {
						display("Index");
					}
				});
		btnBack = lay.addButton("Back", "go-previous",
				"Display the previous help text", new Runnable() {
					@Override
					public void run() {
						back();
					}
				});
		lay.addSpacer();
		getRootPane().setDefaultButton(
				lay.addButton(Button.Ok, "Close this Help Dialog",
						new Runnable() {
							@Override
							public void run() {
								closeHelp();
							}
						}));

		setSize(300, Toolkit.getDefaultToolkit().getScreenSize().height);
		setLocation(0, 0);

		setAlwaysOnTop(true);
	}

	public static HelpDialog the(final Window owner) {
		if (helpDialog == null) new HelpDialog(owner);
		return helpDialog;
	}

	public void display(final String category) {
		setVisible(true);
		try {
			final URL url = HelpLoader.getHelp(category);
			tpHelp.setPage(url);
			history.add(url);
			btnBack.setEnabled(history.size() > 1);
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	protected void back() {
		try {
			if (history.size() > 1) {
				history.remove(history.size() - 1);
				tpHelp.setPage(history.get(history.size() - 1));
				btnBack.setEnabled(history.size() > 1);
			}
		} catch (final IOException e) {
			Log.error(e);
		}
	}

	public void closeHelp() {
		dispose();
		helpDialog = null;
	}

	public static void closeHelpIfOpen() {
		if (helpDialog != null) helpDialog.closeHelp();
	}

}
