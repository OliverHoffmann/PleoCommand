package pleocmd.pipe;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import pleocmd.Log;
import pleocmd.cfg.ConfigBounds;
import pleocmd.cfg.ConfigCollection;
import pleocmd.cfg.ConfigLong;
import pleocmd.cfg.ConfigValue;
import pleocmd.cfg.Group;
import pleocmd.cfg.ConfigCollection.Type;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.InternalException;
import pleocmd.exc.PipeException;
import pleocmd.exc.StateException;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.in.Input;
import pleocmd.pipe.out.Output;

/**
 * Base class of all {@link Input}s, {@link Converter} and {@link Output}s which
 * can be connected to the {@link Pipe}.
 * 
 * @author oliver
 */
public abstract class PipePart extends StateHandling {

	private static final Random RAND = new Random();

	public enum HelpKind {
		Name, Description, Configuration
	}

	private final ConfigLong cfgUID;

	private final Group group;

	private Pipe pipe;

	private final Set<PipePart> connected;

	private final ConfigCollection<Long> cfgConnectedUIDs;

	private final ConfigBounds cfgGuiPosition;

	public PipePart() {
		group = new Group(Pipe.class.getSimpleName() + ": "
				+ getClass().getSimpleName(), this);
		connected = new HashSet<PipePart>();
		group.add(cfgUID = new ConfigLong("UID", RAND.nextLong()));
		group.add(cfgConnectedUIDs = new ConfigCollection<Long>(
				"Connected UIDs", Type.Set) {
			@Override
			protected Long createItem(final String itemAsString)
					throws ConfigurationException {
				try {
					return Long.parseLong(itemAsString);
				} catch (final NumberFormatException e) {
					throw new ConfigurationException("Not a valid UID: "
							+ itemAsString, e);
				}
			}
		});
		group.add(cfgGuiPosition = new ConfigBounds("GUI-Position"));
		cfgGuiPosition.getContent().setBounds(0, 0, 150, 20);
	}

	long getUID() {
		return cfgUID.getContent();
	}

	/**
	 * @return the {@link Group} with all available configuration for this
	 *         {@link PipePart}. Changes made to it will be ignored until
	 *         {@link #configure()} is called (again).
	 */
	public final Group getGroup() {
		return group;
	}

	public final void addConfig(final ConfigValue value) {
		try {
			ensureConstructing();
		} catch (final StateException e) {
			throw new IllegalStateException("Cannot add ConfigValue", e);
		}
		group.add(value);
	}

	/**
	 * Tries to call {@link #configure()} and writes an error message to the
	 * {@link Log} if configuring fails.
	 * 
	 * @return true if configuring was successful
	 */
	public final boolean tryConfigure() {
		try {
			configure();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot configure '%s'", getClass().getSimpleName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #init()} and writes an error message to the
	 * {@link Log} if initializing fails.
	 * 
	 * @return true if initializing was successful
	 */
	public final boolean tryInit() {
		try {
			init();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot initialize '%s'", getClass().getSimpleName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #close()} and writes an error message to the
	 * {@link Log} if closing fails.
	 * 
	 * @return true if closing was successful
	 */
	public final boolean tryClose() {
		try {
			close();
			return true;
		} catch (final PipeException e) {
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot close '%s'", getClass().getSimpleName());
			return false;
		}
	}

	@Override
	public String toString() { // CS_IGNORE_PREV keep overridable
		return group.toString();
	}

	/**
	 * Returns the {@link Pipe} to which this {@link PipePart} is currently
	 * connected.
	 * 
	 * @return a {@link Pipe}
	 * @throws IllegalArgumentException
	 *             if this {@link PipePart} is not connected to any {@link Pipe}
	 */
	public final Pipe getPipe() {
		if (pipe == null)
			throw new IllegalArgumentException("Not connected to any pipe");
		return pipe;
	}

	final void connectedToPipe(final Pipe newPipe) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;

		if (newPipe == null) throw new NullPointerException("newPipe");
		if (pipe != null)
			throw new IllegalArgumentException(String.format(
					"Cannot connect to pipe '%s': Already connected to '%s'",
					newPipe, pipe));

		pipe = newPipe;
		Log.detail("Connected '%s' to '%s'", this, pipe);
	}

	final void disconnectedFromPipe(final Pipe curPipe) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;

		if (curPipe == null) throw new NullPointerException("curPipe");
		if (pipe == null)
			throw new IllegalArgumentException(String.format(
					"Cannot disconnect from pipe '%s': Not connected", curPipe));
		if (pipe != curPipe)
			throw new IllegalArgumentException(String.format(
					"Cannot disconnect from pipe '%s': Connected to '%s'",
					curPipe, pipe));

		pipe = null;
		Log.detail("Disconnected '%s' from '%s'", this, curPipe);
	}

	public Set<PipePart> getConnectedPipeParts() {
		return Collections.unmodifiableSet(connected);
	}

	public void connectToPipePart(final PipePart target) {
		connected.add(target);
		try {
			cfgConnectedUIDs.addContent(target.getUID());
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
	}

	public void disconnectFromPipePart(final PipePart target) {
		connected.remove(target);
		cfgConnectedUIDs.removeContent(target.getUID());
	}

	public void assertAllConnectionUIDsResolved() throws PipeException {
		if (cfgConnectedUIDs.getContent().size() != connected.size()) {
			final Set<Long> goodUIDs = new HashSet<Long>();
			final Set<Long> badUIDs = new HashSet<Long>();
			for (final Long trgUID : cfgConnectedUIDs.getContent()) {
				boolean found = false;
				for (final PipePart pp : connected)
					if (pp.getUID() == trgUID) {
						found = true;
						break;
					}
				if (found)
					goodUIDs.add(trgUID);
				else
					badUIDs.add(trgUID);
			}
			try {
				cfgConnectedUIDs.setContent(goodUIDs);
			} catch (final ConfigurationException e) {
				throw new InternalException(e);
			}
			throw new PipeException(this, true, "Some UIDs could not "
					+ "be resolved: %s. Check connections of Pipe.", badUIDs);
		}
	}

	public void resolveConnectionUIDs(final Map<Long, PipePart> map) {
		connected.clear();
		for (final Long trgUID : cfgConnectedUIDs.getContent())
			if (map.containsKey(trgUID)) connected.add(map.get(trgUID));
	}

	public Rectangle getGuiPosition() {
		return cfgGuiPosition.getContent();
	}

}
