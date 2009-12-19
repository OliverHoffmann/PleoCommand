package pleocmd.pipe.cvt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigDataSeq;
import pleocmd.pipe.Data;
import pleocmd.pipe.DataSequenceMap;

public final class EmotionConverter extends Converter {

	private final DataSequenceMap map = new DataSequenceMap();

	public EmotionConverter() {
		super(new Config().addV(new ConfigDataSeq("Sequence-File")));
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		map.readFromFile(new File(getConfig().get(0).getContentAsString()));
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
		final String tn = data.getSafe(1).asString();
		if (tn == null)
			throw new ConverterException(this, false,
					"Invalid data: Second value must be a non-empty "
							+ "string: '%s'", data);
		try {
			return DataSequenceMap.cloneList(map.findDataList(tn), data
					.getPriority());
		} catch (final ConverterException e) {
			throw new ConverterException(this, false, e,
					"Cannot convert emotion-data '%s'", data);
		}
	}

}
