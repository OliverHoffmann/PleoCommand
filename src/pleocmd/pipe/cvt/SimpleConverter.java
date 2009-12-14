package pleocmd.pipe.cvt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.pipe.Config;
import pleocmd.pipe.ConfigDataSeq;
import pleocmd.pipe.Data;
import pleocmd.pipe.DataSequenceMap;

public final class SimpleConverter extends Converter {

	private final DataSequenceMap map = new DataSequenceMap();

	public SimpleConverter() {
		super(new Config().addV(new ConfigDataSeq("Sequence-File")));
	}

	@Override
	protected void configured0() {
		// nothing to do
	}

	@Override
	protected void init0() throws IOException {
		map.loadFromFile(new File(getConfig().get(0).getContentAsString()));
	}

	@Override
	protected void close0() {
		map.reset();
	}

	@Override
	public boolean canHandleData(final Data data) {
		return "DO".equals(data.getSafe(0).asString())
				|| "do".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		final String tn = data.getSafe(1).asString();
		if (tn == null)
			throw new ConverterException(this, false, String
					.format("Invalid data: First value must "
							+ "be a non-empty string"));
		try {
			return map.findDataList(tn);
		} catch (final IndexOutOfBoundsException e) {
			throw new ConverterException(this, false,
					"Cannot convert simple data", e);
		}
	}
}
