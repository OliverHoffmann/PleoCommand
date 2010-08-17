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

import java.awt.Color;
import java.util.List;

import pleocmd.Log;
import pleocmd.cfg.ConfigBoolean;
import pleocmd.cfg.ConfigColor;
import pleocmd.cfg.ConfigDouble;
import pleocmd.cfg.ConfigInt;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;

final class BoardConfiguration implements ConfigurationInterface {

	// Main colors

	/**
	 * True to draw background in alternating colors to visualize clipping
	 * rectangles. Also prints timing information.
	 */
	public static final ConfigBoolean CFG_PAINT_DEBUG = new ConfigBoolean(
			"Debug", false);

	/**
	 * Background color.
	 */
	public static final ConfigColor CFG_BACKGROUND = new ConfigColor(
			"Background Color", new Color(220, 220, 220));

	/**
	 * ConfigColor CFG_for target-area which doesn't involve a reordering of the
	 * Pipe.
	 */
	public static final ConfigColor CFG_MOVEMENT_HINT = new ConfigColor(
			"Movement Hint Color", new Color(220, 240, 220));

	// Drawing of section borders

	/**
	 * ConfigColor CFG_of section delimiter.
	 */
	public static final ConfigColor CFG_SECT_BORDER = new ConfigColor(
			"Section Border Color", Color.BLACK);

	/**
	 * Amount of fragments when calculating section borders, i.e. parts of the
	 * whole width to use for the first and last section.
	 */
	public static final ConfigInt CFG_SECTION_FRAG = new ConfigInt(
			"Outer Section Size Divisor", 5);

	/**
	 * Amount of pixels to try to keep free when calculating section borders.
	 */
	public static final ConfigInt CFG_SECTION_SPACE = new ConfigInt(
			"Section Minimal Free Size", 20);

	// Drawing of shadows

	/**
	 * ConfigColor CFG_of shadows.
	 */
	public static final ConfigColor CFG_SHADOW_COLOR = new ConfigColor(
			"Shadow Color", Color.GRAY);

	/**
	 * Visual depth of shadows in pixels.
	 */
	public static final ConfigInt CFG_SHADOW_DEPTH = new ConfigInt(
			"Shadow Depth", 4);

	/**
	 * Whether to draw a shadow for top-to-down hint arrow.
	 */
	public static final ConfigBoolean CFG_SHADOW_ORDERHINT = new ConfigBoolean(
			"Draw Shadow On Hint", true);

	/**
	 * Whether to draw a shadow for PipeParts.
	 */
	public static final ConfigBoolean CFG_SHADOW_RECTS = new ConfigBoolean(
			"Draw Shadow On PipeParts", true);

	/**
	 * Whether to draw a shadow for connections.
	 */
	public static final ConfigBoolean CFG_SHADOW_CONNECTIONS = new ConfigBoolean(
			"Draw Shadow On Connections", true);

	/**
	 * Whether to draw a shadow for connections' labels.
	 */
	public static final ConfigBoolean CFG_SHADOW_CONNECTIONS_LABEL = new ConfigBoolean(
			"Draw Shadow On Labels", false);

	// Order Hint Arrow

	/**
	 * Background ConfigColor CFG_of top-to-down hint arrow.
	 */
	public static final ConfigColor CFG_ORDER_HINT_BACK = new ConfigColor(
			"Hint Color", new Color(255, 255, 128));

	/**
	 * Width of top-to-down hint arrow's trunk relative to first section width.
	 */
	public static final ConfigDouble CFG_ORDER_HINT_TRUNK_WIDTH = new ConfigDouble(
			"Hint Relative Trunk Width", 0.3);

	/**
	 * Height of top-to-down hint arrow's trunk relative to the boards height.
	 */
	public static final ConfigDouble CFG_ORDER_HINT_TRUNK_HEIGHT = new ConfigDouble(
			"Hint Relative Trunk Height", 0.65);

	/**
	 * Width of top-to-down hint arrow's head relative to first section width.
	 */
	public static final ConfigDouble CFG_ORDER_HINT_ARROW_WIDTH = new ConfigDouble(
			"Hint Relative Arrow Width", 0.3);

	/**
	 * Height of top-to-down hint arrow's head relative to the boards height.
	 */
	public static final ConfigDouble CFG_ORDER_HINT_ARROW_HEIGHT = new ConfigDouble(
			"Hint Relative Arrow Height", 0.3);

	// Drawing of icons inside a PipePart

	/**
	 * ConfigColor CFG_of the rectangle around a icon which is not selected nor
	 * hovered.
	 */
	public static final ConfigColor CFG_ICON_OUTLINE = new ConfigColor(
			"Icon Outline Color", new Color(128, 128, 0));

	/**
	 * ConfigColor CFG_of the rectangle around a icon which is not selected but
	 * hovered.
	 */
	public static final ConfigColor CFG_ICON_OUTLINE_HOVER = new ConfigColor(
			"Icon Outline Hover Color", new Color(255, 255, 128));

	/**
	 * ConfigColor CFG_of the rectangle around a icon which is selected but not
	 * hovered.
	 */
	public static final ConfigColor CFG_ICON_OUTLINE_SEL = new ConfigColor(
			"Icon Outline Selected Color", new Color(128, 128, 0));

	/**
	 * ConfigColor CFG_of the rectangle around a icon which is selected and
	 * hovered.
	 */
	public static final ConfigColor CFG_ICON_OUTLINE_SEL_HOVER = new ConfigColor(
			"Icon Outline Selected Hover Color", new Color(255, 255, 128));

	/**
	 * General width of an icon.
	 */
	public static final ConfigInt CFG_ICON_WIDTH = new ConfigInt("Icon Width",
			18);

	/**
	 * Maximal amount of icons possible inside a PipePart.
	 */
	public static final ConfigInt CFG_ICON_MAX = new ConfigInt(
			"Maximum Icon Count", 4);

	/**
	 * Position of the Configuration icon.
	 */
	public static final ConfigInt CFG_ICON_CONF_POS = new ConfigInt(
			"Configuration Icon Position", -1);

	/**
	 * Position of the Visualization icon.
	 */
	public static final ConfigInt CFG_ICON_DGR_POS = new ConfigInt(
			"Visualization Icon Position", 0);

	// Drawing of a PipePart

	/**
	 * Background ConfigColor CFG_of a PipePart.
	 */
	public static final ConfigColor CFG_RECT_BACKGROUND = new ConfigColor(
			"Icon PipePart Background Color", new Color(255, 255, 255));
	/**
	 * Background ConfigColor CFG_of inner section and icons of a modifiable
	 * PipePart.
	 */
	public static final ConfigColor CFG_INNER_MODIFIABLE = new ConfigColor(
			"Icon PipePart Inner Modifiable Color", new Color(200, 200, 255));

	/**
	 * Background ConfigColor CFG_of inner section and icons of a read-only
	 * PipePart.
	 */
	public static final ConfigColor CFG_INNER_READONLY = new ConfigColor(
			"Icon PipePart Inner Readonly Color", Color.LIGHT_GRAY);

	/**
	 * ConfigColor CFG_of PipePart's border if the PipePart is sane but not
	 * selected.
	 */
	public static final ConfigColor CFG_OUTER_OK = new ConfigColor(
			"Icon PipePart Outer Color", Color.BLACK);

	/**
	 * ConfigColor CFG_of PipePart's border if the PipePart is not sane nor
	 * selected.
	 */
	public static final ConfigColor CFG_OUTER_BAD = new ConfigColor(
			"Icon PipePart Outer Bad Color", Color.RED);

	/**
	 * ConfigColor CFG_of PipePart's border if the PipePart is sane and
	 * selected.
	 */
	public static final ConfigColor CFG_OUTER_SEL_OK = new ConfigColor(
			"Icon PipePart Outer Selected Color", Color.BLUE);

	/**
	 * ConfigColor CFG_of PipePart's border if the PipePart is not sane but
	 * selected.
	 */
	public static final ConfigColor CFG_OUTER_SEL_BAD = new ConfigColor(
			"Icon PipePart Outer Selected Bad Color", Color.MAGENTA);

	/**
	 * Maximal width of a PipePart's rectangle in pixel.
	 */
	public static final ConfigInt CFG_MAX_RECT_WIDTH = new ConfigInt(
			"Maximum PipePart Width", 200);

	/**
	 * Amount of pixels between inner and outer part left and right of a
	 * PipePart's rectangle.
	 */
	public static final ConfigInt CFG_INNER_WIDTH = new ConfigInt(
			"PipePart Outer-Inner Width", 2);

	/**
	 * Amount of pixels between inner and outer part top and bottom of a
	 * PipePart's rectangle.
	 */
	public static final ConfigInt CFG_INNER_HEIGHT = new ConfigInt(
			"PipePart Outer-Inner Height", 6);

	/**
	 * If true, a short summarize of the configuration is drawn instead of the
	 * PipePart's name.
	 */
	public static final ConfigBoolean CFG_DRAW_SHORTCONFIG = new ConfigBoolean(
			"Draw Configuration Instead Of Label", true);

	// Drawing of connections

	/**
	 * ConfigColor CFG_of a connections whose PipePart is sane but not selected.
	 */
	public static final ConfigColor CFG_CONNECTION_OK = new ConfigColor(
			"Icon Connection Color", Color.BLACK);

	/**
	 * ConfigColor CFG_of a connections whose PipePart is not sane nor selected.
	 */
	public static final ConfigColor CFG_CONNECTION_BAD = new ConfigColor(
			"Icon Connection Bad Color", Color.RED);

	/**
	 * ConfigColor CFG_of a connections whose PipePart is sane and selected.
	 */
	public static final ConfigColor CFG_CONNECTION_SEL_OK = new ConfigColor(
			"Icon Connection Selected Color", Color.BLUE);

	/**
	 * ConfigColor CFG_of a connections whose PipePart is not sane but selected.
	 */
	public static final ConfigColor CFG_CONNECTION_SEL_BAD = new ConfigColor(
			"Icon Connection Selected Bad Color", Color.MAGENTA);

	/**
	 * Thickness of the arrows' head of a connector.
	 */
	public static final ConfigInt CFG_CONN_ARROW_HEAD = new ConfigInt(
			"Connection Arrow Head Thickness", 14);

	/**
	 * Thickness of the arrows' wings of a connector.
	 */
	public static final ConfigInt CFG_CONN_ARROW_WING = new ConfigInt(
			"Connection Arrow Wing Thickness", 8);

	// Preferred Size Calculation
	/**
	 * Minimum width and height to return in getPreferredSize()
	 */
	public static final ConfigInt CFG_PREFSIZE_MIN = new ConfigInt(
			"Minimum Preferred Dimension", 50);

	/**
	 * Number of pixel for free space right and below the PipePart at the most
	 * lower-right position.
	 */
	public static final ConfigDouble CFG_PREFSIZE_FREE = new ConfigDouble(
			"Free Space Of Lower Right PipePart", 4);

	// Drawing of PipeFlow symbols

	/**
	 * Width of one Pipe-Flow symbol
	 */
	public static final ConfigDouble CFG_FLOW_WIDTH = new ConfigDouble(
			"PipeFlow Symbol Width", 3);

	static {
		// must be *after* declaration of all static fields !!!
		new BoardConfiguration();
	}

	private BoardConfiguration() {
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(CFG_BACKGROUND)
				.add(CFG_CONN_ARROW_HEAD).add(CFG_CONN_ARROW_WING).add(
						CFG_CONNECTION_BAD).add(CFG_CONNECTION_OK).add(
						CFG_CONNECTION_SEL_BAD).add(CFG_CONNECTION_SEL_OK).add(
						CFG_DRAW_SHORTCONFIG).add(CFG_FLOW_WIDTH).add(
						CFG_ICON_CONF_POS).add(CFG_ICON_DGR_POS).add(
						CFG_ICON_MAX).add(CFG_ICON_OUTLINE).add(
						CFG_ICON_OUTLINE_HOVER).add(CFG_ICON_OUTLINE_SEL).add(
						CFG_ICON_OUTLINE_SEL_HOVER).add(CFG_ICON_WIDTH).add(
						CFG_INNER_HEIGHT).add(CFG_INNER_MODIFIABLE).add(
						CFG_INNER_READONLY).add(CFG_INNER_WIDTH).add(
						CFG_MAX_RECT_WIDTH).add(CFG_MOVEMENT_HINT).add(
						CFG_ORDER_HINT_ARROW_HEIGHT).add(
						CFG_ORDER_HINT_ARROW_WIDTH).add(CFG_ORDER_HINT_BACK)
				.add(CFG_ORDER_HINT_TRUNK_HEIGHT).add(
						CFG_ORDER_HINT_TRUNK_WIDTH).add(CFG_OUTER_BAD).add(
						CFG_OUTER_OK).add(CFG_OUTER_SEL_BAD).add(
						CFG_OUTER_SEL_OK).add(CFG_PAINT_DEBUG).add(
						CFG_PREFSIZE_FREE).add(CFG_PREFSIZE_MIN).add(
						CFG_RECT_BACKGROUND).add(CFG_SECT_BORDER).add(
						CFG_SECTION_FRAG).add(CFG_SECTION_SPACE).add(
						CFG_SHADOW_COLOR).add(CFG_SHADOW_CONNECTIONS).add(
						CFG_SHADOW_CONNECTIONS_LABEL).add(CFG_SHADOW_DEPTH)
				.add(CFG_SHADOW_ORDERHINT).add(CFG_SHADOW_RECTS);
	}

	@Override
	public void configurationAboutToBeChanged() {
		// nothing to do
	}

	@Override
	public void configurationRead() {
		// nothing to do
	}

	@Override
	public void configurationChanged(final Group group) {
		// nothing to do
	}

	@Override
	public List<Group> configurationWriteback() {
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

}
