package pleocmd.itfc.cli;

import java.util.Date;

public final class Version {

	public static final String VERSION_GITREV = "--ERROR--";

	public static final long VERSION_DATE = 0;

	public static final String VERSION_STR = String.format(
			"PleoCommand rev:%s %s", Version.VERSION_GITREV, new Date(
					Version.VERSION_DATE * 1000));

}
