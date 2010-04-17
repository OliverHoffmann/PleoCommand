package pleocmd.itfc.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import pleocmd.Log;
import pleocmd.cfg.Configuration;
import pleocmd.exc.ConfigurationException;
import pleocmd.pipe.Pipe;

public class PipePreviewLabel extends JLabel {

	private static final long serialVersionUID = -2427386445040802543L;

	private Pipe lastPipe;

	private BoardPainter painter;

	public PipePreviewLabel() {
		setBorder(BorderFactory.createBevelBorder(1));
		setPreferredSize(new Dimension(300, 100));
		update((Pipe) null, true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				update(getLastPipe(), false);
			}
		});
	}

	public final void update(final File pipeConfig) {
		if (pipeConfig == null || !pipeConfig.canRead()) {
			update((Pipe) null, true);
			return;
		}
		final Configuration config = new Configuration();
		final Pipe pipe = new Pipe(config);
		try {
			config.readFromFile(pipeConfig, pipe);
			pipe.setLastSaveFile(pipeConfig);
			update(pipe, true);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	public final void update(final Pipe pipe) {
		update(pipe, true);
	}

	protected final void update(final Pipe pipe, final boolean setPipe) {
		lastPipe = pipe;
		final int width = getWidth();
		final int height = getHeight();
		if (pipe == null || width == 0 || height == 0)
			setIcon(null);
		else {
			final BufferedImage img = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			if (painter == null) {
				painter = new BoardPainter();
				painter.setPipe(pipe, img.getGraphics(), false);
			} else if (setPipe)
				painter.setPipe(pipe, img.getGraphics(), false);
			final Dimension pref = painter.getPreferredSize();
			painter.setScale(Math.min((double) width / (double) pref.width,
					(double) height / (double) pref.height));
			painter.setBounds(width, height, false);
			final Graphics g = img.getGraphics();
			g.setClip(0, 0, width, height);
			painter.paint(g, null, null, null, null, false, null, true);
			setIcon(new ImageIcon(img));
		}
	}

	public final Pipe getLastPipe() {
		return lastPipe;
	}

}
