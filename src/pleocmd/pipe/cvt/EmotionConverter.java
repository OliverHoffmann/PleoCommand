package pleocmd.pipe.cvt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.pipe.cfg.ConfigDataSeq;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.DataSequenceMap;

public final class EmotionConverter extends Converter {

	private final ConfigDataSeq cfg0;

	private final DataSequenceMap map = new DataSequenceMap();

	public EmotionConverter() {
		getConfig().add(cfg0 = new ConfigDataSeq("Sequence-File"));
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		map.readFromFile(new File(cfg0.getContent()));
	}

	@Override
	protected void close0() {
		map.reset();
	}

	@Override
	public boolean canHandleData(final Data data) {
		return "BE".equals(data.getSafe(0).asString())
				|| "be".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		try {
			return DataSequenceMap.cloneList(map.findDataList(data.get(1)
					.asString()), data);
		} catch (final ConverterException e) {
			throw new ConverterException(this, false, e,
					"Cannot convert emotion-data '%s'", data);
		}
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "Emotional Data Converter";
		case Description:
			return "Converts emotions like 'BE|foo' into a sequence of "
					+ "simpler commands based on a table lookup for 'foo'";
		case Configuration:
			return "1: Path to a file which contains a mapping between "
					+ "triggers and a list of commands for each of them";
		default:
			return "???";
		}
	}

}
