// This file is part of PleoCommand:
// Interactively control Pleo with psychobiological parameters
//
// Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Boston, USA.

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
import pleocmd.itfc.gui.BoardPainter.PaintParameters;
import pleocmd.pipe.Pipe;

public class PipePreviewLabel extends JLabel {

	private static final long serialVersionUID = -2427386445040802543L;

	private Pipe lastPipe;

	public PipePreviewLabel() {
		setBorder(BorderFactory.createBevelBorder(1));
		setPreferredSize(new Dimension(300, 100));
		update((Pipe) null);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				update(getLastPipe());
			}
		});
	}

	protected final void update(final File pipeConfig) {
		if (pipeConfig == null || !pipeConfig.canRead()) {
			update((Pipe) null);
			return;
		}
		final Configuration config = new Configuration();
		final Pipe pipe = new Pipe(config);
		try {
			config.readFromFile(pipeConfig, pipe);
			pipe.setLastSaveFile(pipeConfig);
			update(pipe);
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	protected final void update(final Pipe pipe) {
		lastPipe = pipe;
		final int width = getWidth();
		final int height = getHeight();
		if (pipe == null || width == 0 || height == 0)
			setIcon(null);
		else {
			final BoardPainter painter = new BoardPainter();
			final BufferedImage img = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			painter.setPipe(pipe, img.getGraphics(), false);
			final Dimension pref = painter.getPreferredSize();
			painter.setScale(Math.min((double) width / (double) pref.width,
					(double) height / (double) pref.height));
			painter.setBounds(width, height, false);
			final Graphics g = img.getGraphics();
			g.setClip(0, 0, width, height);
			final PaintParameters p = new PaintParameters();
			p.g = g;
			p.modifyable = true;
			painter.paint(p);
			setIcon(new ImageIcon(img));
		}
	}

	public final Pipe getLastPipe() {
		return lastPipe;
	}

}
