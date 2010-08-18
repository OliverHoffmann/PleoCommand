package pleocmd.cfg;

import java.io.IOException;

import javax.swing.JLabel;

import pleocmd.Log;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.dse.DataBlockField;
import pleocmd.pipe.data.Data;

public final class ConfigDataBlock extends ConfigString {

	public ConfigDataBlock(final String label) {
		super(label, false);
	}

	public ConfigDataBlock(final String label, final Data content) {
		super(label, content.asString());
	}

	public ConfigDataBlock(final String label, final String contentAsData) {
		this(label);
		try {
			setContent(Data.createFromAscii(contentAsData).asString());
		} catch (final ConfigurationException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		} catch (final IOException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		} catch (final FormatException e) {
			throw new IllegalArgumentException(
					"Cannot initialize default content", e);
		}
	}

	@Override
	public boolean insertGUIComponents(final Layouter lay) {
		final JLabel errorLabel = new JLabel();
		setTc(new DataBlockField(getContent(), 20, errorLabel));
		lay.add(getTc(), true);
		lay.newLine();
		lay.add(errorLabel, true);
		invokeChangingContent(getTc().getText());
		return false;
	}

	@Override
	public void setFromGUIComponents() {
		try {
			setContent(Data.createFromAscii(getTc().getText()).asString());
		} catch (final ConfigurationException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		} catch (final IOException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		} catch (final FormatException e) {
			Log.error(e, "Cannot set value '%s'", getLabel());
		}
	}

	@Override
	public final String getIdentifier() {
		return "datablock";
	}

}
