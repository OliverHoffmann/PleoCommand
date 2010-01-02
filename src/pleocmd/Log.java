package pleocmd;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import pleocmd.itfc.gui.MainFrame;

/**
 * Contains all relevant content of one log message (one line in log view) as
 * well as static methods to create log messages of any {@link Type}.
 * 
 * @author oliver
 */
public final class Log {

	/**
	 * Specifies the type of a message.
	 * 
	 * @author oliver
	 */
	public enum Type {
		/**
		 * Detailed or debug messages.
		 */
		Detail,
		/**
		 * Informational messages.
		 */
		Info,
		/**
		 * Warnings and other severe messages.
		 */
		Warn,
		/**
		 * Errors and Exceptions.
		 */
		Error,
		/**
		 * The standard output is captured via this {@link Type} in GUI mode.<br>
		 * Will not be used in console mode.
		 */
		Console
	}

	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			"HH:mm:ss.SSS");

	private static Type minLogType = Type.Detail;

	/**
	 * Needed to inline next line after a line-break when writing messages to a
	 * plain {@link String}. Number of spaces must fit to format specificier in
	 * {@link #toString()} and the length of the {@link #DATE_FORMATTER}.
	 */
	private static final String SPACES = String.format("%68s", "");

	private final Type type;

	private final String caller;

	private final String msg;

	private final Throwable backtrace;

	private final long time;

	private Log(final Type type, final String caller, final String msg,
			final Throwable backtrace) {
		this.type = type;
		this.caller = caller;
		this.msg = msg;
		this.backtrace = backtrace;
		time = System.currentTimeMillis();
		process();
	}

	/**
	 * @return the {@link Type} of this log entry.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return a {@link String} describing the class and method which created
	 *         this log entry.
	 */
	public String getCaller() {
		return caller;
	}

	/**
	 * @return a {@link String} with the message of this log entry.
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @return the complete backtrace for this log entry if this log is an
	 *         {@link Type#Error} or null if not.
	 */
	public Throwable getBacktrace() {
		return backtrace;
	}

	/**
	 * @return the time in milliseconds since the epoch when this log entry has
	 *         been created.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return a {@link Color} matching the {@link Type} of this log entry.
	 * @see #getType()
	 */
	public Color getTypeColor() {
		switch (type) {
		case Detail:
			return Color.GRAY;
		case Info:
			return Color.BLUE;
		case Warn:
			return Color.ORANGE;
		case Error:
			return Color.RED;
		case Console:
		default:
			return Color.BLACK;
		}
	}

	/**
	 * @return the time (with milliseconds - no date), from {@link #getTime()}
	 *         formatted as a {@link String}
	 */
	public String getFormattedTime() {
		return DATE_FORMATTER.format(new Date(time));
	}

	/**
	 * @return a three character long {@link String} matching the {@link Type}
	 *         of this log entry.
	 * @see #getType()
	 */
	public String getTypeShortString() {
		switch (type) {
		case Detail:
			return "DTL";
		case Info:
			return "INF";
		case Warn:
			return "WRN";
		case Error:
			return "ERR";
		case Console:
			return ">  ";
		default:
			return "!?!";
		}
	}

	@Override
	public String toString() {
		return String.format("%s %s %-50s %s", getFormattedTime(),
				getTypeShortString(), caller, msg.replace("\n", "\n" + SPACES));
	}

	/**
	 * Prints messages to the GUI's log, if any, or to the standard error
	 * otherwise if their {@link #type} is not "lower" than the
	 * {@link #minLogType}.<br>
	 * Always prints messages of type Console to the standard output (instead of
	 * standard error) no matter if a GUI exists or not.
	 */
	private void process() {
		if (type.ordinal() >= minLogType.ordinal()) {
			if (type == Type.Console) System.out.println(getMsg()); // CS_IGNORE
			if (MainFrame.hasGUI())
				MainFrame.the().addLog(this);
			else if (type != Type.Console) System.err.println(toString()); // CS_IGNORE
		}
	}

	private static String getCallersName(final int stepsBack) {
		return getCallersName(new Throwable(), stepsBack);
	}

	private static String getCallersName(final Throwable t, final int stepsBack) {
		final StackTraceElement[] st = t.getStackTrace();
		return String.format("%s.%s()", st.length < stepsBack ? "???"
				: st[stepsBack].getClassName().replaceFirst("^.*\\.([^.]*)$",
						"$1"), st[stepsBack].getMethodName());
	}

	private static void msg(final Type type, final String msg,
			final Object... args) {
		new Log(type, getCallersName(3), args.length == 0 ? msg : String
				.format(msg, args), null);
	}

	/**
	 * Creates a new message of {@link Type#Detail} which will be printed to
	 * error output or send to the GUI.
	 * 
	 * @param msg
	 *            the message - interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero)
	 */
	public static void detail(final String msg, final Object... args) {
		msg(Type.Detail, msg, args);
	}

	/**
	 * Creates a new message of {@link Type#Info} which will be printed to error
	 * output or send to the GUI.
	 * 
	 * @param msg
	 *            the message - interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero)
	 */
	public static void info(final String msg, final Object... args) {
		msg(Type.Info, msg, args);
	}

	/**
	 * Creates a new message of {@link Type#Warn} which will be printed to error
	 * output or send to the GUI.
	 * 
	 * @param msg
	 *            the message - interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero)
	 */
	public static void warn(final String msg, final Object... args) {
		msg(Type.Warn, msg, args);
	}

	/**
	 * Creates a new message of {@link Type#Error} which will be printed to
	 * error output or send to the GUI.
	 * 
	 * @param msg
	 *            the message - interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero)
	 */
	public static void error(final String msg, final Object... args) {
		msg(Type.Error, msg, args);
	}

	/**
	 * Creates a new message of {@link Type#Console} which will be printed to
	 * standard output <b>and</b> send to the GUI.
	 * 
	 * @param msg
	 *            the message - interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero)
	 */
	public static void consoleOut(final String msg, final Object... args) {
		msg(Type.Console, msg, args);
	}

	/**
	 * Creates a new message of {@link Type#Error} which will be printed to
	 * error output or send to the GUI. This message will contain a complete
	 * backtrace.
	 * 
	 * @param throwable
	 *            the {@link Exception} that occurred as a reason for this
	 *            message.
	 * @see #getBacktrace()
	 */
	public static void error(final Throwable throwable) {
		error(throwable, "");
	}

	/**
	 * Creates a new message of {@link Type#Error} which will be printed to
	 * error output or send to the GUI. This message will contain a complete
	 * backtrace.
	 * 
	 * @param throwable
	 *            the {@link Exception} that occurred as a reason for this
	 *            message.
	 * @param msg
	 *            an optional message prepended before the exception -
	 *            interpreted as a format string (like in
	 *            {@link String#format(String, Object...)}) if any arguments are
	 *            given or just used as is otherwise.
	 * @param args
	 *            arbitrary number of arguments for the format string (may also
	 *            be zero) - will be ignored if the message string is empty.
	 * @see #getBacktrace()
	 */
	public static void error(final Throwable throwable, final String msg,
			final Object... args) {
		final StringBuilder sb = new StringBuilder();
		if (!msg.isEmpty()) {
			sb.append(args.length == 0 ? msg : String.format(msg, args));
			sb.append(": ");
		}
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

		new Log(Type.Error, getCallersName(t, 0), sb.toString(), throwable);
	}

	/**
	 * Sets the "lowest" {@link Type} which will be processed. Messages of
	 * "lower" types will be ignored.
	 * <p>
	 * Possible values:
	 * <table>
	 * <tr>
	 * <td>{@link Type#Detail}</td>
	 * <td>all messages will be processed</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Type#Info}</td>
	 * <td>all but detailed messages will be processed</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Type#Warn}</td>
	 * <td>only warnings and errors will be processed</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Type#Error}</td>
	 * <td>only errors will be processed</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Type#Console}</td>
	 * <td>no messages will be processed</td>
	 * </tr>
	 * </table>
	 * <p>
	 * Note that messages of {@link Type#Console} are treated as wrapped
	 * standard output and not as normal messages and are therefore always be
	 * processed even if minLogType is set to {@link Type#Console}.
	 * 
	 * @param minLogType
	 *            true if {@link Type#Detail} will be processed
	 */
	public static void setMinLogType(final Type minLogType) {
		if (minLogType.ordinal() < Type.Detail.ordinal()
				|| minLogType.ordinal() > Type.Console.ordinal())
			throw new IllegalArgumentException("Invalid value for minLogType");
		Log.minLogType = minLogType;
	}

	/**
	 * @return the "lowest" {@link Type} which will be processed. Messages of
	 *         "lower" types will be ignored.
	 */
	public static Type getMinLogType() {
		return minLogType;
	}

	/**
	 * @param type
	 *            one of the log {@link Type}s
	 * @return true if messages of the given {@link Type} can be logged.
	 */
	public static boolean canLog(final Type type) {
		return type.ordinal() >= minLogType.ordinal();
	}

}
