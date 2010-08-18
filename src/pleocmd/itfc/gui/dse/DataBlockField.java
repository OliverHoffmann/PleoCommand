package pleocmd.itfc.gui.dse;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import pleocmd.itfc.gui.dse.DataSequenceEditorPanel.FadeTimerTask;

public class DataBlockField extends JTextPane implements UpdateErrorInterface {

	private static final long serialVersionUID = 6442009735721513493L;

	private final JLabel errorLabel;

	private final Timer errorLabelTimer = new Timer("ErrorLabelTimer", true);

	private TimerTask errorLabelTimerTask;

	public DataBlockField(final String data, final int columns,
			final JLabel errorLabel) {
		super();
		setPreferredSize(new Dimension(columns
				* getFontMetrics(getFont()).getMaxAdvance(),
				getPreferredSize().height));
		this.errorLabel = errorLabel;
		final DataSequenceEditorKit kit = new DataSequenceEditorKit(this);
		setEditorKitForContentType("text/datasequence", kit);
		setContentType("text/datasequence");
		setFont(getFont().deriveFont(Font.BOLD));
		setText(data);
		errorLabel.setForeground(Color.RED);
	}

	@Override
	public void updateErrorLabel(final String text) {
		if (errorLabel == null) return;
		if (text.equals(errorLabel.getText())) return;
		errorLabel.setText(text);
		Color.RGBtoHSB(255, 0, 0, null);
		final Color src = Color.RED;
		final Color trg = errorLabel.getBackground();
		errorLabel.setForeground(src);
		if (errorLabelTimerTask != null) errorLabelTimerTask.cancel();
		errorLabelTimerTask = new FadeTimerTask(errorLabel, src.getRed(), src
				.getGreen(), src.getBlue(), trg.getRed(), trg.getGreen(), trg
				.getBlue());
		errorLabelTimer.schedule(errorLabelTimerTask, 1000, 100);
	}

}
