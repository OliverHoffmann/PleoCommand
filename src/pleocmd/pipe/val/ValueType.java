package pleocmd.pipe.val;

// TODO MOD remove this class and only use the ...Value classes for
// distinguishing?
public enum ValueType {

	Int8(0), Int32(1), Int64(2), Float32(3), Float64(4), UTFString(5), NullTermString(
			6), Data(7);

	private int id;

	private ValueType(final int id) {
		this.id = id;
	}

	/**
	 * @return ID of this {@link ValueType} used in binary streams
	 */
	public int getID() {
		return id;
	}

}
