package pleocmd.cfg;

import java.util.List;
import java.util.Set;

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

	void configurationAboutToBeChanged() throws ConfigurationException;

	/**
	 * The global {@link Configuration} has been changed and every objects needs
	 * reload itself from the {@link Configuration}.<br>
	 * This method will be called once for every group in the configuration in
	 * the order they appear in the configuration file.
	 * 
	 * @param group
	 *            one of the {@link Group}s that have been changed
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

}
