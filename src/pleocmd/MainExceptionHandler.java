package pleocmd;

final class MainExceptionHandler {

	public void handle(final Throwable thrown) {
		// we need to print the Exception additionally to
		// logging it because logging itself may have caused
		// the exception or it just is not yet initialized
		thrown.printStackTrace(); // CS_IGNORE
		Log.error(thrown);
	}

}
