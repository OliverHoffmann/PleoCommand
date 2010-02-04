package pleocmd.exc;

import java.util.List;

import pleocmd.pipe.val.Syntax;
import pleocmd.pipe.val.Syntax.Type;

public final class FormatException extends Exception {

	private static final long serialVersionUID = -2642142547584763520L;

	private final int index;

	public FormatException(final List<Syntax> syntaxList, final int index,
			final String msg, final Object... args) {
		super(String.format("At position %d: ", index)
				+ String.format(msg, args));
		this.index = index;
		if (syntaxList != null) syntaxList.add(new Syntax(Type.Error, index));
	}

	public FormatException(final int index, final String msg,
			final Object... args) {
		super(String.format("At position %d: ", index)
				+ String.format(msg, args));
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
