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

package pleocmd.cfg;

import java.util.List;
import java.util.Set;

import pleocmd.exc.ConfigurationException;

public interface ConfigurationInterface {

	/**
	 * A group with a name registered by this {@link ConfigurationInterface} has
	 * been found. This method may return a {@link Group} with fitting
	 * {@link ConfigValue}s so that the {@link Configuration} can check data
	 * format and illegal input directly during reading.
	 * <p>
	 * This is an optional method and may simply return <b>null</b>
	 * 
	 * @param groupName
	 *            name of the new {@link Group}
	 * @return a {@link Group} or <b>null</b>
	 * @throws ConfigurationException
	 *             if creating the {@link Group} fails
	 */
	Group getSkeleton(String groupName) throws ConfigurationException;

	/**
	 * Invoked before the configuration will be completely (re)read from a file
	 * or some other source.
	 * 
	 * @throws ConfigurationException
	 *             if something is wrong which prevents the configuration from
	 *             being read.
	 */
	void configurationAboutToBeChanged() throws ConfigurationException;

	/**
	 * The global {@link Configuration} has been changed and every objects needs
	 * reload itself from the {@link Configuration}.<br>
	 * This method will be called once for every group in the configuration in
	 * the order they appear in the configuration file.
	 * 
	 * @param group
	 *            one of the {@link Group}s that have been changed. If the
	 *            {@link #getSkeleton(String)} for this group-name returned a
	 *            {@link Group}, exactly the same one will be given here.
	 * @throws ConfigurationException
	 *             if reading {@link ConfigValue}s from the {@link Group} fails
	 */
	void configurationChanged(Group group) throws ConfigurationException;

	/**
	 * The global {@link Configuration} is about to be written to persistent
	 * storage and every objects needs write its local modifications back to the
	 * {@link Configuration}.<br>
	 * The names of the {@link Group}s returned by this method should be a
	 * subset of the names used during
	 * {@link Configuration#registerConfigurableObject(ConfigurationInterface, Set)}
	 * 
	 * @return the list of {@link Group}s which should be written back to the
	 *         {@link Configuration}
	 * @throws ConfigurationException
	 *             if writing back configuration changes or creating
	 *             {@link Group}s fails
	 */
	List<Group> configurationWriteback() throws ConfigurationException;

	/**
	 * Invoked after the configuration has been completely (re)read from a file
	 * or some other source.
	 * 
	 * @throws ConfigurationException
	 *             if something in the just read configuration is wrong (like a
	 *             specific group seems to be missing, ...)
	 */
	void configurationRead() throws ConfigurationException;

}
