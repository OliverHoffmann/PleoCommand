package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.List;

import pleocmd.cfg.ConfigDataMap;
import pleocmd.exc.ConverterException;
import pleocmd.pipe.data.CommandData;
import pleocmd.pipe.data.Data;

public final class SimpleConverter extends Converter {

	private final ConfigDataMap cfgMap;

	public SimpleConverter() {
		addConfig(cfgMap = new ConfigDataMap("Data-Sequences"));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public String getInputDescription() {
		return "DO";
	}

	@Override
	public String getOutputDescription() {
		return "";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!CommandData.isCommandData(data, "DO")) return null;
		final String s = CommandData.getArgument(data);
		if (!cfgMap.hasContent(s))
			throw new ConverterException(this, false,
					"No entry for '%s' found", s);
		return ConfigDataMap.cloneDataList(cfgMap.getContent(s), data);
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Simple Data Converter";
		case Description:
			return "Converts commands like 'DO|foo' into a sequence of "
					+ "simpler commands based on a table lookup for 'foo'";
		case Configuration:
			return "1: Path to a file which contains a mapping between "
					+ "triggers and a list of commands for each of them";
		default:
			return "???";
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
