package pleocmd.pipe.cvt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pleocmd.exc.ConverterException;
import pleocmd.exc.InternalException;
import pleocmd.pipe.data.Data;
import pleocmd.pipe.val.Value;
import pleocmd.pipe.val.ValueType;

public final class BCIChannelSplitter extends Converter {

	public BCIChannelSplitter() {
		constructed();
	}

	@Override
	protected void configure0() {
		// nothing to do
	}

	@Override
	protected void init0() {
		// nothing to do
	}

	@Override
	protected void close0() {
		// nothing to do
	}

	@Override
	public String getInputDescription() {
		return "FromBCI";
	}

	@Override
	public String getOutputDescription() {
		return "BCIChannel";
	}

	@Override
	protected List<Data> convert0(final Data data) throws ConverterException {
		if (!"FromBCI".equals(data.getSafe(0).asString())) return null;
		final List<Data> res = new ArrayList<Data>(data.size() - 1);
		for (int i = 1; i < data.size(); ++i) {
			final List<Value> vals = new ArrayList<Value>(4);
			try {
				vals.add(Value.createForType(ValueType.NullTermString).set(
						"BCIChannel"));
				vals.add(Value.createForType(ValueType.Int8).set(
						String.valueOf(i - 1)));
				vals.add(data.get(i));
				vals.add(Value.createForType(ValueType.Int32).set("0"));
			} catch (final IOException e) {
				throw new InternalException(e);
			}
			res.add(new Data(vals, data));
		}
		return res;
	}

	public static String help(final HelpKind kind) {
		switch (kind) {
		case Name:
			return "BCI Channel Splitter";
		case Description:
			return "Splits a data packet received from BCI into its "
					+ "single channels, like 'FromBCI|foo|bar' to "
					+ "'BCIChannel|0|foo' and 'BCIChannel|1|bar'";
		case Configuration:
			return "";
		default:
			return "???";
		}
	}

	@Override
	public boolean isConfigurationSane() {
		return true;
	}

}
