package pleocmd.pipe.cvt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.pipe.cfg.Config;
import pleocmd.pipe.cfg.ConfigDataSeq;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.data.DataSequenceMap;

public final class SimpleConverter extends Converter {

	private final DataSequenceMap map = new DataSequenceMap();

	public SimpleConverter() {
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
		return "DO".equals(data.getSafe(0).asString())
				|| "do".equals(data.getSafe(0).asString());
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		try {
			return DataSequenceMap.cloneList(map.findDataList(data.get(1)
					.asString()), data.getPriority());
		} catch (final ConverterException e) {
			throw new ConverterException(this, false, e,
					"Cannot convert simple-data '%s'", data);
		}
	}

}
