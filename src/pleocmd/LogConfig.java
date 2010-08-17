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

package pleocmd;

import java.text.SimpleDateFormat;
import java.util.List;

import pleocmd.cfg.ConfigEnum;
import pleocmd.cfg.ConfigString;
import pleocmd.cfg.Configuration;
import pleocmd.cfg.ConfigurationInterface;
import pleocmd.cfg.Group;
import pleocmd.exc.ConfigurationException;
import pleocmd.itfc.gui.MainFrame;

final class LogConfig implements ConfigurationInterface {

	public static final ConfigString CFG_TIMEFORMAT = new ConfigString(
			"Time Format", "HH:mm:ss.SSS");

	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			CFG_TIMEFORMAT.getContent());

	public static ConfigEnum<Log.Type> CFG_MIN_LOG_TYPE = new ConfigEnum<Log.Type>(
			"Minimal Log-Type", Log.Type.Info);

	public static ConfigString CFG_EXPORT_COLUMNS = new ConfigString(
			"Columns To Export", "TYSM");

	static {
		// must be *after* declaration of all static fields !!!
		new LogConfig();
	}

	private LogConfig() {
		try {
			Configuration.getMain().registerConfigurableObject(this,
					getClass().getSimpleName());
		} catch (final ConfigurationException e) {
			Log.error(e);
		}
	}

	@Override
	public Group getSkeleton(final String groupName) {
		return new Group(groupName).add(CFG_TIMEFORMAT).add(CFG_MIN_LOG_TYPE)
				.add(CFG_EXPORT_COLUMNS);
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
		DATE_FORMATTER.applyPattern(CFG_TIMEFORMAT.getContent());
		if (MainFrame.hasGUI()) MainFrame.the().updateState();
		Log.setMinLogType(Log.getMinLogType());
	}

	@Override
	public List<Group> configurationWriteback() {
		return Configuration.asList(getSkeleton(getClass().getSimpleName()));
	}

}
