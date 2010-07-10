package pleocmd.pipe;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.Icon;

import pleocmd.ImmutableRectangle;
import pleocmd.Log;
import pleocmd.cfg.ConfigBoolean;
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
import pleocmd.itfc.gui.dgr.Diagram;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.itfc.gui.dgr.PipeVisualizationDialog;
import pleocmd.itfc.gui.icons.IconLoader;
import pleocmd.pipe.cvt.Converter;
import pleocmd.pipe.data.Data;
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

	private static final int BUILTIN_VIS = 0;

	public enum HelpKind {
		/**
		 * Name of the Pipe-Part (only one line)
		 */
		Name,
		/**
		 * A short description of this Pipe-Part (a few lines)
		 */
		Description,
		/**
		 * Name of an HTML file displayed when clicking "Help" in the
		 * configuration dialog (defaults to the class-name + ".html")
		 */
		HelpFile,
		/**
		 * Name of an icon for this Pipe-Part (should be 16x16 pixels, defaults
		 * to the class-name + "-icon.png")
		 */
		Icon,
		/**
		 * Name of an image displayed on the left side of the configuration
		 * dialog (should be no larger than 300x500 pixels, defaults to the
		 * class-name + "-cfg.png")
		 */
		ConfigImage,
		/**
		 * Short description of a configuration entry (a few lines)
		 */
		Config1, Config2, Config3, Config4, Config5, //
		Config6, Config7, Config8, Config9, Config10, //
		Config11, Config12, Config13, Config14, Config15, //
		Config16, Config17, Config18, Config19, Config20, //
		// Config... must be the last entries
	}

	private final ConfigLong cfgUID;

	private final Group group;

	private final List<ConfigValue> guiConfigs;

	private Pipe pipe;

	private final Set<PipePart> connected;

	private final ConfigCollection<Long> cfgConnectedUIDs;

	private final ConfigBounds cfgGuiPosition;

	private PipeVisualizationDialog visualizationDialog;

	private final ConfigBoolean cfgVisualize;

	private final PipePartVisualizationConfig visualizationConfig;

	private final ImmutableRectangle immutableGUIPosition;

	private String sanityCache;

	private String shortConfigDescr;

	private final PipePartFeedback feedback;

	public PipePart() {
		feedback = new PipePartFeedback();

		group = new Group(Pipe.class.getSimpleName() + ": "
				+ getClass().getSimpleName(), this);
		guiConfigs = new ArrayList<ConfigValue>();
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
		cfgGuiPosition.getContent().setBounds(0, 0, 0, 0);
		visualizationConfig = new PipePartVisualizationConfig(this, group);
		group.add(cfgVisualize = new ConfigBoolean("Visualization", false));

		immutableGUIPosition = new ImmutableRectangle(cfgGuiPosition
				.getContent());

	}

	final long getUID() {
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
		addConfig(value, true);
	}

	protected final void addConfig(final ConfigValue value,
			final boolean visibleInGUI) {
		try {
			ensureConstructing();
		} catch (final StateException e) {
			throw new IllegalStateException("Cannot add ConfigValue", e);
		}
		group.add(value);
		if (visibleInGUI) guiConfigs.add(value);
	}

	public final List<ConfigValue> getGuiConfigs() {
		return Collections.unmodifiableList(guiConfigs);
	}

	public final void addConfigToGUI(final ConfigValue value) {
		if (group.get(value.getLabel()) != value)
			throw new IllegalArgumentException("ConfigValue is not registered");
		if (guiConfigs.contains(value))
			throw new IllegalArgumentException("ConfigValue is already in GUI");
		guiConfigs.add(value);
	}

	/**
	 * @return <b>null</b> if everything is all-right or a {@link String}
	 *         describing why this {@link PipePart} will probably fail to
	 *         initialize if it would be initialized with it's current
	 *         configuration.
	 */
	public abstract String isConfigurationSane();

	/**
	 * Tries to call {@link #configure()} and writes an error message to the
	 * {@link Log} if configuring fails.
	 * 
	 * @return true if configuring was successful
	 */
	protected final boolean tryConfigure() {
		try {
			feedback.incConfiguredCount();
			configure();
			return true;
		} catch (final PipeException e) {
			feedback.addError(e, e.isPermanent());
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot configure '%s'", getName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #init()} and writes an error message to the
	 * {@link Log} if initializing fails.
	 * 
	 * @return true if initializing was successful
	 */
	protected final boolean tryInit() {
		try {
			init();
			feedback.incInitializedCount();
			feedback.started();
			if (cfgVisualize.getContent()) createVisualization();
			return true;
		} catch (final PipeException e) {
			feedback.addError(e, e.isPermanent());
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot initialize '%s'", getName());
			return false;
		}
	}

	/**
	 * Tries to call {@link #close()} and writes an error message to the
	 * {@link Log} if closing fails.
	 * 
	 * @return true if closing was successful
	 */
	protected final boolean tryClose() {
		try {
			feedback.stopped();
			feedback.incClosedCount();
			close();
			return true;
		} catch (final PipeException e) {
			feedback.addError(e, e.isPermanent());
			pipe.getFeedback().addError(e, e.isPermanent());
			Log.error(e, "Cannot close '%s'", getName());
			return false;
		}
	}

	@Override
	protected final void configure0() throws PipeException, IOException {
		sanityCache = null;
		shortConfigDescr = null;
		configure1();
	}

	/**
	 * Can contain special code which should be invoked in sub-classes during
	 * configuration.
	 * 
	 * @throws PipeException
	 *             if configuration fails
	 * @throws IOException
	 *             if configuration fails
	 */
	protected void configure1() throws PipeException, IOException {
		// do nothing by default
	}

	public abstract String getInputDescription();

	public abstract String getOutputDescription();

	@Override
	public String toString() { // CS_IGNORE_PREV keep overridable
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		if (!guiConfigs.isEmpty()) {
			sb.append(" [");
			boolean first = true;
			for (final ConfigValue cv : guiConfigs) {
				if (!first) sb.append(", ");
				first = false;
				sb.append(cv.toString());
			}
			sb.append("]");
		}
		return sb.toString();
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

	/**
	 * @return true if this {@link PipePart} is connected to a {@link Pipe}.
	 */
	public final boolean isConnected() {
		return pipe != null;
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
		pipe.modified();
	}

	final void disconnectedFromPipe(final Pipe curPipe) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName()) : s1;
		closeVisualization();

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
		curPipe.modified();
	}

	public final Set<PipePart> getConnectedPipeParts() {
		return Collections.unmodifiableSet(connected);
	}

	public final void connectToPipePart(final PipePart target)
			throws StateException {
		ensureConstructed();
		try {
			cfgConnectedUIDs.addContent(target.getUID());
		} catch (final ConfigurationException e) {
			throw new InternalException(e);
		}
		connected.add(target);
		if (pipe != null) pipe.modified();
	}

	public final void disconnectFromPipePart(final PipePart target)
			throws StateException {
		ensureConstructed();
		connected.remove(target);
		cfgConnectedUIDs.removeContent(target.getUID());
		if (pipe != null) pipe.modified();
	}

	/**
	 * @param s1
	 *            first string
	 * @param s2
	 *            second string
	 * @return true only if both strings contain something and that is not the
	 *         same.
	 */
	private static boolean strDiffer(final String s1, final String s2) {
		return s1 != null && s2 != null && !s1.isEmpty() && !s2.isEmpty()
				&& !s1.equals(s2);
	}

	public final boolean isConnectionAllowed(final PipePart trg) {
		return this != trg
				&& !(trg instanceof Input)
				&& isConnectionAllowed0(trg)
				&& !strDiffer(getOutputDescription(), trg.getInputDescription());
	}

	protected abstract boolean isConnectionAllowed0(final PipePart trg);

	/**
	 * Throws an exception when there are any unresolved connections.<br>
	 * The unresolved UIDs will be removed from the internal UID-list.
	 * 
	 * @throws PipeException
	 *             an exception containing the list of unresolved connections.
	 */
	protected final void assertAllConnectionUIDsResolved() throws PipeException {
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

	/**
	 * Resolves all UIDs from connected {@link PipePart}s, while missing ones
	 * are ignored. The internal connection-list only contains those that could
	 * be resolved afterwards.
	 * 
	 * @param map
	 *            a mapping between UIDs and {@link PipePart}s currently known
	 *            to the {@link Pipe}
	 * @throws StateException
	 *             if the {@link PipePart} is being constructed or already
	 *             initialized
	 */
	protected final void resolveConnectionUIDs(final Map<Long, PipePart> map)
			throws StateException {
		ensureConstructed();
		connected.clear();
		for (final Long trgUID : cfgConnectedUIDs.getContent())
			if (map.containsKey(trgUID)) connected.add(map.get(trgUID));
	}

	public final void setGuiPosition(final Rectangle guiPosition) {
		cfgGuiPosition.setContent(guiPosition);
		if (pipe != null) pipe.modified();
	}

	public final ImmutableRectangle getGuiPosition() {
		return immutableGUIPosition;
	}

	protected final void groupWriteback() throws ConfigurationException {
		visualizationConfig.writeback();
		groupWriteback0();
	}

	protected void groupWriteback0() {
		// do nothing by default
	}

	public final boolean isVisualize() {
		return cfgVisualize.getContent();
	}

	public final void setVisualize(final boolean visualize) {
		cfgVisualize.setContent(visualize);
		if (getState() == State.Initialized) if (visualize)
			createVisualization();
		else
			closeVisualization();
		if (pipe != null) pipe.modified();
	}

	private void createVisualization() {
		final int visDataCount = getVisualizeDataSetCount() + BUILTIN_VIS;

		if (visualizationDialog != null) {
			visualizationDialog.reset(visDataCount);
			initVisualize0();
			return;
		}
		visualizationDialog = new PipeVisualizationDialog(this, visDataCount);
		visualizationConfig.assignConfig();
		initVisualize0();
		visualizationDialog.setVisible(true);
	}

	protected void initVisualize0() {
		// do nothing by default
	}

	private void closeVisualization() {
		if (visualizationDialog == null) return;
		try {
			visualizationConfig.writeback();
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot write back visualization configuration");
		}
		visualizationDialog.dispose();
		visualizationDialog = null;
	}

	/**
	 * Invoked directly after {@link #init0()} and if the user enables
	 * visualization while the {@link Pipe} is already running.<br>
	 * Must return a constant value while being initialized.
	 * 
	 * @return the number of {@link DiagramDataSet}s which will be plotted via
	 *         {@link #plot(int, double)}.
	 */
	protected abstract int getVisualizeDataSetCount();

	public final PipeVisualizationDialog getVisualizationDialog() {
		return visualizationDialog;
	}

	protected final DiagramDataSet getVisualizeDataSet(final int index) {
		return visualizationDialog == null ? null : visualizationDialog
				.getDataSet(index + BUILTIN_VIS);
	}

	/**
	 * Don't use this from any other than {@link Input}, {@link Output} and
	 * {@link Converter}.
	 * 
	 * @param index
	 *            real index of {@link DiagramDataSet}
	 * @param x
	 *            value for x-axis
	 * @param y
	 *            value for y-axis
	 */
	protected final void plot0(final int index, final double x, final double y) {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(Pipe.class.getName())
				|| s1.equals(Input.class.getName())
				|| s1.equals(Converter.class.getName())
				|| s1.equals(Output.class.getName()) : s1;
		if (visualizationDialog != null) {
			visualizationDialog.plot(index, x, y);
			feedback.incDataPlotCount();
		}
	}

	/**
	 * Plots a value to the visualization {@link Diagram}.<br>
	 * Because this x-axis always represents the currently elapsed time,
	 * {@link #plot(int, double)} may be more appropriate in most cases.
	 * 
	 * @param index
	 *            index of {@link DiagramDataSet}
	 * @param x
	 *            value for x-axis
	 * @param y
	 *            value for y-axis
	 */
	protected final void plot(final int index, final double x, final double y) {
		plot0(index + BUILTIN_VIS, x, y);
	}

	/**
	 * Plots a value to the visualization {@link Diagram} with the currently
	 * elapsed time of the {@link Pipe} as value for x-axis.
	 * 
	 * @param index
	 *            index of {@link DiagramDataSet}
	 * @param y
	 *            value for y-axis
	 */
	protected final void plot(final int index, final double y) {
		plot0(index + BUILTIN_VIS, Double.NaN, y);
	}

	/**
	 * Checks if this {@link PipePart} is sane. Sane means that the
	 * {@link PipePart} can be reached from an {@link Input}, has a path to an
	 * {@link Output}, doesn't contain a dead-lock in it's path and is correctly
	 * configured.
	 * 
	 * @param sane
	 *            {@link PipePart} is added to this map. The value assigned to
	 *            it will be <b>null</b> if it is sane or some {@link String}
	 *            otherwise.
	 * @param visited
	 *            a set of already visited {@link PipePart}s during the current
	 *            recursion (handled like a kind of stack) to detect dead-locks
	 * @param deadLocked
	 *            a set of already detected dead-locks
	 * @return true if an {@link Output} can be reached from the
	 *         {@link PipePart}.
	 */
	final boolean topDownCheck(final Map<PipePart, String> sane,
			final Set<PipePart> visited, final Set<PipePart> deadLocked) {
		if (visited.contains(this)) {
			deadLocked.add(this);
			return false;
		}
		boolean outputReached = topDownCheck_outputReached();
		boolean validConns = true;
		visited.add(this);
		final List<PipePart> copy = new ArrayList<PipePart>(
				getConnectedPipeParts());
		for (final PipePart ppSub : copy) {
			outputReached |= ppSub.topDownCheck(sane, visited, deadLocked);
			validConns &= isConnectionAllowed(ppSub);
		}
		visited.remove(this);
		final StringBuilder sbError = new StringBuilder();
		if (!outputReached)
			sbError.append("An Output-PipePart cannot be reached\n");
		if (!validConns)
			sbError.append("One or more invalid connections attached\n");
		if (sanityCache == null) {
			sanityCache = isConfigurationSane();
			if (sanityCache == null) sanityCache = "";
		}
		if (!sanityCache.isEmpty()) sbError.append(sanityCache + "\n");
		if (sbError.length() == 0)
			sane.put(this, null);
		else {
			sbError.delete(sbError.length() - 1, sbError.length());
			sane.put(this, sbError.toString());
		}
		return outputReached;
	}

	protected boolean topDownCheck_outputReached() { // CS_IGNORE
		// design for extension: *is* empty
		return false;
	}

	protected static List<Data> asList(final Data data) {
		final List<Data> res = new ArrayList<Data>(1);
		res.add(data);
		return res;
	}

	protected static List<Data> emptyList() {
		return new ArrayList<Data>(0);
	}

	public final String getShortConfigDescr() {
		if (shortConfigDescr == null)
			shortConfigDescr = getShortConfigDescr0();
		return shortConfigDescr;
	}

	protected abstract String getShortConfigDescr0();

	public final String getName() {
		return getName(getClass());
	}

	public static final String getName(final Class<? extends PipePart> ppc) {
		return PipePartDetection.callHelp(ppc, HelpKind.Name);
	}

	public final String getDescription() {
		return getDescription(getClass());
	}

	public static final String getDescription(
			final Class<? extends PipePart> ppc) {
		return PipePartDetection.callHelp(ppc, HelpKind.Description);
	}

	public final String getHelpFile() {
		return getHelpFile(getClass());
	}

	public static final String getHelpFile(final Class<? extends PipePart> ppc) {
		final String name = PipePartDetection.callHelp(ppc, HelpKind.HelpFile);
		return name == null ? ppc.getSimpleName() + ".html" : name;
	}

	public final String getConfigHelp(final int index) {
		return getConfigHelp(getClass(), index);
	}

	public static final String getConfigHelp(
			final Class<? extends PipePart> ppc, final int index) {
		final int cfgIndex = HelpKind.Config1.ordinal() + index;
		if (cfgIndex >= HelpKind.values().length) return null;
		return PipePartDetection.callHelp(ppc, HelpKind.values()[cfgIndex]);
	}

	public final Icon getConfigImage() {
		return getConfigImage(getClass());
	}

	public static final Icon getConfigImage(final Class<? extends PipePart> ppc) {
		String name = PipePartDetection.callHelp(ppc, HelpKind.ConfigImage);
		if (name == null) name = ppc.getSimpleName() + "-cfg.png";
		return IconLoader.isIconAvailable(name) ? IconLoader.getIcon(name)
				: null;
	}

	public final Icon getIcon() {
		return getIcon(getClass());
	}

	public static final Icon getIcon(final Class<? extends PipePart> ppc) {
		String name = PipePartDetection.callHelp(ppc, HelpKind.Icon);
		if (name == null) name = ppc.getSimpleName() + "-icon.png";
		return IconLoader.isIconAvailable(name) ? IconLoader.getIcon(name)
				: null;
	}

	public PipePartFeedback getFeedback() {
		return feedback;
	}

}
