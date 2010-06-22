package pleocmd.cfg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.dse.DataSequenceTriggerPanel;
import pleocmd.pipe.data.Data;

public final class ConfigDataMap extends ConfigMap<String, Data> {

	private DataSequenceTriggerPanel dp;
	private boolean internalMod;

	public ConfigDataMap(final String label) {
		super(label);
	}

	@Override
	protected String createKey(final String keyAsString) {
		return keyAsString;
	}

	@Override
	protected Data createValue(final String valueAsString)
			throws ConfigurationException {
		try {
			return Data.createFromAscii(valueAsString);
		} catch (final IOException e) {
			throw new ConfigurationException(e, "Cannot convert String to Data");
		} catch (final FormatException e) {
			throw new ConfigurationException(e, "Cannot convert String to Data");
		}
	}

	@Override
	protected String convertKey(final String key) {
		return key;
	}

	@Override
	protected String convertValue(final Data value) {
		return value.asString();
	}

	@Override
	protected void contentChanged() {
		if (!internalMod && dp != null) dp.externalChanged(this);
	}

	public Map<String, List<Data>> getContentGUI() {
		return dp == null ? getContent() : dp.getMap().getContent();
	}

	public void setContentGUI(final ConfigDataMap other) {
		internalMod = true;
		try {
			if (dp != null) dp.externalChanged(other);
		} finally {
			internalMod = false;
		}
	}

	@Override
	public boolean insertGUIComponents(final Layouter lay) {
		dp = new DataSequenceTriggerPanel(this);
		lay.addWholeLine(dp, true);
		invokeChangingContent(this);
		return true;
	}

	@Override
	public void setFromGUIComponents() {
		dp.saveChanges();
	}

	@Override
	public void setGUIEnabled(final boolean enabled) {
		if (dp != null) dp.setEnabled(enabled);
	}

	/**
	 * Creates a deep copy of the given list while setting the parent and
	 * changing the priority of every {@link Data} in the list which has the
	 * default priority to the new one.
	 * 
	 * @param org
	 *            the original list of {@link Data}
	 * @param parent
	 *            the parent for every new {@link Data} - may be <b>null</b>
	 * @return a new list of {@link Data} copies
	 */
	public static List<Data> cloneDataList(final List<Data> org,
			final Data parent) {
		Log.detail("Cloning list '%s' with parent '%s'", org, parent);
		final List<Data> res = new ArrayList<Data>(org.size());
		for (final Data data : org)
			res.add(new Data(data, parent));
		return res;
	}

	public void dstpModified(final ConfigDataMap map) {
		if (!internalMod) invokeChangingContent(map);
	}

	/**
	 * Should only be used from {@link DataSequenceTriggerPanel}.
	 * 
	 * @param internalMod
	 *            true if invocations of {@link #contentChanged()} should be
	 *            ignored
	 */
	public void setInternalMod(final boolean internalMod) {
		this.internalMod = internalMod;
	}

}
