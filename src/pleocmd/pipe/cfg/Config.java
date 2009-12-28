package pleocmd.pipe.cfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pleocmd.Log;
import pleocmd.exc.PipeException;
import pleocmd.pipe.PipePart;

/**
 * Contains the whole configuration of a {@link PipePart} as well as mechanisms
 * for reading and writing it from and to files.
 * 
 * @author oliver
 */
public final class Config implements Iterable<ConfigValue> {

	private final List<ConfigValue> list = new ArrayList<ConfigValue>();

	private PipePart owner;

	/**
	 * Should only be called in a constructor of {@link PipePart}.
	 */
	public Config() {
		String s1;
		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
				.equals(PipePart.class.getName()) : s1;
	}

	@Override
	public Iterator<ConfigValue> iterator() {
		return list.iterator();
	}

	/**
	 * @param index
	 *            the index of the configuration which should be returned
	 * @return one of the available / needed configurations for the
	 *         {@link PipePart} which owns this {@link Config}
	 */
	public ConfigValue get(final int index) {
		return list.get(index);
	}

	/**
	 * @return the number of available / needed configurations for the
	 *         {@link PipePart} which owns this {@link Config}
	 */
	public int size() {
		return list.size();
	}

	/**
	 * @return true if no configuration is available / needed for the
	 *         {@link PipePart} which owns this {@link Config}
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	public PipePart getOwner() {
		return owner;
	}

	public void setOwner(final PipePart owner) {
		if (this.owner != null)
			throw new IllegalStateException(
					"Config's owner has already been assigned");
		try {
			owner.ensureConstructing();
		} catch (final PipeException e) {
			throw new IllegalStateException("Cannot set Config's owner", e);
		}
		this.owner = owner;
	}

	public Config add(final ConfigValue value) {
		try {
			if (owner != null) owner.ensureConstructing();
		} catch (final PipeException e) {
			throw new IllegalStateException(
					"Cannot add ConfigValue to configuration", e);
		}
		list.add(value);
		return this;
	}

	public void readFromFile(final BufferedReader in) throws IOException,
			PipeException {
		Log.detail("Reading config from BufferedReader");
		for (final ConfigValue v : list) {
			in.mark(10240);
			String line = in.readLine();
			if (line == null)
				throw new IOException(String.format(
						"Missing configuration for '%s'", v.getLabel()));
			line = line.trim();
			final int idx = line.indexOf(':');
			if (idx == -1)
				throw new IOException(String.format(
						"Missing ':' delimiter in '%s'", line));
			final String label = line.substring(0, idx).trim();
			if (!label.equals(v.getLabel()))
				throw new IOException(String.format(
						"Wrong configuration value '%s' - excepted '%s'",
						label, v.getLabel()));
			v.setFromString(line.substring(idx + 1).trim());
		}
		Log.detail("Read config from BufferedReader: %s", this);
		owner.configure();
	}

	public void writeToFile(final Writer out) throws IOException {
		Log.detail("Writing config to Writer: %s", this);
		for (final ConfigValue v : list) {
			Log.detail("Writing '%s'", v);
			out.write('\t');
			out.write(v.getLabel());
			out.write(':');
			out.write(' ');
			out.write(v.getContentAsString());
			out.write('\n');
		}
		out.flush();
	}

	@Override
	public String toString() {
		return list.toString() + " owned by " + owner;
	}

}
