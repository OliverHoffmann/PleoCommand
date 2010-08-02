package pleocmd.pipe.out;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.Log;
import pleocmd.cfg.ConfigEnum;
import pleocmd.exc.InternalException;
import pleocmd.exc.OutputException;
import pleocmd.itfc.gui.MainFrame;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Syntax;

public final class ConsoleOutput extends Output {

	private final ConfigEnum<PrintType> cfgType;

	private Data lastRoot;

	public ConsoleOutput() {
		addConfig(cfgType = new ConfigEnum<PrintType>(PrintType.class));
		constructed();
	}

	public ConsoleOutput(final PrintType type) {
		this();
		cfgType.setEnum(type);
	}

	@Override
	protected void close0() {
		lastRoot = null;
	}

	@Override
	public String getInputDescription() {
		return "";
	}

	@Override
	protected String getShortConfigDescr0() {
		return cfgType.getContent();
	}

	@Override
	protected boolean write0(final Data data) throws OutputException,
			IOException {
		Data root;
		switch (cfgType.getEnum()) {
		case Ascii:
			printAscii(data);
			break;
		case Binary: {
			printBinary(data);
			break;
		}
		case AsciiOriginal:
			if (lastRoot != (root = data.getRoot()))
				printAscii(lastRoot = root);
			break;
		case BinaryOriginal:
			if (lastRoot != (root = data.getRoot()))
				printBinary(lastRoot = root);
			break;
		case PleoMonitorCommands:
			if ("PMC".equals(data.getSafe(0).asString()))
				Log.consoleOut(data.get(1).asString());
			break;
		default:
			throw new InternalException(cfgType.getEnum());
		}
		return true;
	}

	private static void printBinary(final Data data) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.writeToBinary(new DataOutputStream(out));
		if (MainFrame.hasGUI()) {
			final StringBuilder sb = new StringBuilder();
			final List<Syntax> syntaxList = new ArrayList<Syntax>();
			data.writeToBinary(sb, syntaxList);
			// html like formatting, no real xml code here
			sb.insert(0, "<html>");
			int o = 6;
			for (final Syntax sy : syntaxList) {
				final Color c = sy.getType().getColor();
				sb.insert(sy.getPosition() + o, String.format(
						"<font color=#%02X%02X%02X>", c.getRed(), c.getGreen(),
						c.getBlue()));
				o += 20;
			}
			Log.consoleOut2(out.toString("ISO-8859-1"), sb.toString());
		} else
			Log.consoleOut(out.toString("ISO-8859-1"));
	}

	private static void printAscii(final Data data) throws IOException {
		if (MainFrame.hasGUI()) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final List<Syntax> syntaxList = new ArrayList<Syntax>();
			data.writeToAscii(new DataOutputStream(out), false, syntaxList);
			// html like formatting, no real xml code here
			final StringBuilder sb = new StringBuilder(out.toString());
			sb.insert(0, "<html>");
			int o = 6;
			for (final Syntax sy : syntaxList) {
				final Color c = sy.getType().getColor();
				sb.insert(sy.getPosition() + o, String.format(
						"<font color=#%02X%02X%02X>", c.getRed(), c.getGreen(),
						c.getBlue()));
				o += 20;
			}
			Log.consoleOut2(data.asString(), sb.toString());
		} else
			Log.consoleOut(data.asString());
	}

	public static String help(final HelpKind kind) { // NO_UCD
		switch (kind) {
		case Name:
			return "Console Output";
		case Description:
			return "Writes Data blocks to the standard output";
		case Config1:
			return "'Ascii' if Data blocks will be in ASCII format or\n"
					+ "   'Binary' if Data blocks will be written as binary";
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

}
