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

package pleocmd.pipe.in;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import pleocmd.Log;
import pleocmd.cfg.ConfigString;
import pleocmd.exc.ConfigurationException;
import pleocmd.exc.FormatException;
import pleocmd.exc.InputException;
import pleocmd.itfc.gui.Layouter;
import pleocmd.itfc.gui.dse.DataSequenceEditorPanel;
import pleocmd.pipe.data.Data;

public final class StaticInput extends Input { // NO_UCD

	private final ConfigString cfgInput;

	private DataInputStream in;

	public StaticInput() {
		addConfig(cfgInput = new ConfigString("Input", true) {

			private DataSequenceEditorPanel dse;

			@Override
			public boolean insertGUIComponents(final Layouter lay) {
				dse = new DataSequenceEditorPanel() {

					private static final long serialVersionUID = 6973128265186613390L;

					@Override
					protected void stateChanged() {
						// nothing to do here
					}

				};
				try {
					dse.updateTextPaneFromReader(new BufferedReader(
							new StringReader(getContent())));
				} catch (final IOException e) {
					Log.error(e);
				}
				lay.addWholeLine(dse, true);
				return true;
			}

			@Override
			public void setFromGUIComponents() {
				final StringWriter out = new StringWriter();
				try {
					dse.writeTextPaneToWriter(out);
					setContent(out.toString());
				} catch (final IOException e) {
					Log.error(e, "Cannot set value '%s'", getLabel());
				} catch (final ConfigurationException e) {
					Log.error(e, "Cannot set value '%s'", getLabel());
				}
			}

		});
		constructed();
	}

	public StaticInput(final String staticData) throws ConfigurationException {
		this();
		cfgInput.setContent(staticData);
	}

	@Override
	protected void init0() throws IOException {
		in = new DataInputStream(new ByteArrayInputStream(cfgInput.getContent()
				.getBytes("ISO-8859-1")));
	}

	@Override
	protected void close0() throws IOException {
		in.close();
		in = null;
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		final int count = cfgInput.getContentList().size();
		return String.format("%d Data block%s", count, count == 1 ? "" : "s");
	}

	@Override
	protected Data readData0() throws InputException, IOException {
		if (in.available() <= 0) {
			Log.info("End Of List in Static-Input");
			return null;
		}
		try {
			return Data.createFromAscii(in);
		} catch (final FormatException e) {
			throw new InputException(this, false, e, "Cannot read static data");
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Static Input";
		case Description:
			return "Returns ASCII Data blocks directly from configuration";
		case Config1:
			return "A new-line delimited list of Data blocks in ASCII form";
		default:
			return null;
		}
	}

	@Override
	public String isConfigurationSane() {
		return null;
	}

	@Override
	protected int getVisualizeDataSetCount() {
		return 0;
	}

	@Override
	public String toString() { // CS_IGNORE_PREV keep overridable
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" [");
		sb.append(cfgInput.getContentList().size());
		sb.append("]");
		return sb.toString();
	}
}
