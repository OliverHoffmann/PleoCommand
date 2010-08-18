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

package pleocmd.pipe.cvt;

import java.util.List;

import pleocmd.cfg.ConfigInt;
import pleocmd.exc.ConverterException;
import pleocmd.itfc.gui.dgr.DiagramDataSet;
import pleocmd.pipe.data.CommandData;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.SingleFloatData;

public final class SingleJointMovement extends Converter { // NO_UCD

	private static final int ANGLE_UNDEFINED = 1000000;

	// Time to wait (in ms) after the joint-movement should have been finished
	private static final int ADDITIONAL_WAIT = 500;

	private final ConfigInt cfgJointNumber;
	private final ConfigInt cfgMinAngleMovement;
	private final ConfigInt cfgMovementSpeed;

	private long currentAngle;

	public SingleJointMovement() {
		addConfig(cfgJointNumber = new ConfigInt("Joint-Number", 9, 0, 13));
		addConfig(cfgMinAngleMovement = new ConfigInt("Minimal Angle-Movement",
				3, 0, 50));
		addConfig(cfgMovementSpeed = new ConfigInt("Maximal Movement Speed",
				180, 1, 1000));
		constructed();
	}

	@Override
	protected void init0() {
		currentAngle = ANGLE_UNDEFINED;
	}

	@Override
	protected void initVisualize0() {
		final DiagramDataSet ds = getVisualizeDataSet(0);
		if (ds != null)
			ds.setLabel(String.format("Angle for Joint %d", cfgJointNumber
					.getContent()));
	}

	@Override
	public String getInputDescription() {
		return SingleFloatData.IDENT;
	}

	@Override
	public String getOutputDescription() {
		return "PMC";
	}

	@Override
	protected String getShortConfigDescr0() {
		return String.format("joint %d if >%d°", cfgJointNumber.getContent(),
				cfgMinAngleMovement.getContent());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!SingleFloatData.isSingleFloatData(data)) return null;
		final long val = Math.round(SingleFloatData.getValue(data));
		if (Math.abs(currentAngle - val) < cfgMinAngleMovement.getContent())
			return emptyList(); // ignore small movements

		// convert degree / sec in ms for <val> degree_delta
		final int delta = (int) Math.abs(val
				- (currentAngle == ANGLE_UNDEFINED ? 0 : currentAngle));
		final int wait = 1000 * delta / cfgMovementSpeed.getContent();

		currentAngle = val;
		if (isVisualize()) plot(0, val);
		return asList(new CommandData("PMC", String.format(
				"JOINT ANGLE %d %d %d %d", cfgJointNumber.getContent(), val,
				wait + ADDITIONAL_WAIT, wait), data));
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Single Joint Movement";
		case Description:
			return "Produces a JOINT ANGLE command for the Pleo for one joint "
					+ "based on the data in a single channel";
		case Config1:
			return "Number of Pleo joint (motor) to move";
		case Config2:
			return "Minimal delta of angle in degree; all movements smaller than "
					+ "this value will be ignored";
		case Config3:
			return "Maximal allowed movement speed for the joint (in degree / s)";
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
		return 1;
	}

}
