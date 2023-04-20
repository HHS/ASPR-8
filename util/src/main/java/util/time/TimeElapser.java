package util.time;

/**
 * A debug-convenience class for measuring elapsed time via System.nanoTime()
 *
 *
 */
public final class TimeElapser {

	private long startTime;

	/**
	 * Creates the time elaspser. Time begins accumulating immediately.
	 */
	public TimeElapser() {
		reset();
	}

	/**
	 * Returns the elapsed time in milliseconds
	 */
	public double getElapsedMilliSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000001;
		return result;
	}

	/**
	 * Return the number of nanoseconds since the construction or reset of this
	 * TimeElapser. Note that nanosecond precision does not imply nanosecond
	 * accuracy.
	 *
	 * @return
	 */
	public long getElapsedNanoSeconds() {
		return System.nanoTime() - startTime;
	}

	/**
	 * Returns the elapsed time in seconds
	 */
	public double getElapsedSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000000001;
		return result;
	}

	/**
	 * Sets the elapsed time back to zero.
	 */
	public void reset() {
		startTime = System.nanoTime();
	}
}