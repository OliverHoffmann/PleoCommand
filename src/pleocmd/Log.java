package pleocmd;

import pleocmd.itfc.gui.GUIFrame;

public final class Log {

	public enum MsgType {
		Detail, Info, Warn, Error
	}

	private static final boolean PRINT_DETAIL = true;

	private Log() {

	}

	public static void detail(final String msg) {
		msg(MsgType.Detail, msg);
	}

	public static void info(final String msg) {
		msg(MsgType.Info, msg);
	}

	public static void warn(final String msg) {
		msg(MsgType.Warn, msg);
	}

	public static void error(final String msg) {
		msg(MsgType.Error, msg);
	}

	public static void error(final Throwable throwable) {
		final StringBuilder sb = new StringBuilder();
		Throwable t = throwable;
		while (true) {
			sb.append(t.getClass().getSimpleName());
			sb.append(": '");
			sb.append(t.getMessage());
			sb.append("'");
			if (t.getCause() == null) break;
			t = t.getCause();
			sb.append(" caused by ");
		}

		msg(MsgType.Error, getCallersName(t, 0), sb.toString());
	}

	private static String getCallersName(final int stepsBack) {
		return getCallersName(new Throwable(), stepsBack);
	}

	private static String getCallersName(final Throwable t, final int stepsBack) {
		final StackTraceElement[] st = t.getStackTrace();
		return st.length < stepsBack ? "???" : st[stepsBack].getClassName()
				.replaceFirst("^.*\\.([^.]*)$", "$1")
				+ "." + st[stepsBack].getMethodName() + "()";
	}

	private static void msg(final MsgType type, final String msg) {
		msg(type, getCallersName(3), msg);
	}

	private static void msg(final MsgType type, final String caller,
			final String msg) {
		if (GUIFrame.hasGUI()) {
			GUIFrame.the().getLogTableModel().addLog(type, caller, msg);
			return;
		}
		String s;
		switch (type) {
		case Detail:
			if (!PRINT_DETAIL) return;
			s = "DTL";
			break;
		case Info:
			s = "INF";
			break;
		case Warn:
			s = "WRN";
			break;
		case Error:
			s = "ERR";
			break;
		default:
			s = "!?!";
			break;
		}
		System.err.println(String.format("%s %-50s %s", s, caller, msg));
	}

}
