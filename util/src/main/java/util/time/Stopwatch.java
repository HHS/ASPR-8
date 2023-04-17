package util.time;



/**
 * A nano-time based accumulator of elapsed time that allows the client to start
 * and stop accumulation.
 * 
 *
 */

public final class Stopwatch {

	private boolean running;

	private long elapsedTime;

	private int executionCount;

	private final TimeElapser timeElapser = new TimeElapser();

	/**
	 * Returns the time accumulated in milliseconds
	 */
	public double getElapsedMilliSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000001;
		return result;
	}

	/**
	 * Returns the time accumulated in nanoseconds
	 */
	public long getElapsedNanoSeconds() {
		long result = elapsedTime;
		if (running) {
			result += timeElapser.getElapsedNanoSeconds();
		}
		return result;
	}

	/**
	 * Returns the time accumulated in seconds
	 */
	public double getElapsedSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000000001;
		return result;
	}

	/**
	 * Returns the number of times this StopWatch has been started
	 */
	public int getExecutionCount() {
		return executionCount;
	}

	/**
	 * Returns true if and only if this StopWatch is accumulating time.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Stops this StopWatch and resets the elapsed time and execution count
	 */
	public void reset() {
		stop();
		elapsedTime = 0;
		executionCount = 0;
	}

	/**
	 * Starts this StopWatch if it has not already started
	 */
	public void start() {
		if (!running) {
			timeElapser.reset();
			running = true;
		}
	}

	/**
	 * Starts this StopWatch if it has not already stopped
	 */
	public void stop() {
		if (running) {
			elapsedTime += timeElapser.getElapsedNanoSeconds();
			executionCount++;
			running = false;
		}
	}
}
