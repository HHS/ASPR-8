package util;

import net.jcip.annotations.ThreadSafe;

/**
 * Threadsafe version of {@link StopWatch}
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class ConcurrentStopWatch {

	private boolean running;

	private long elapsedTime;

	private int executionCount;

	private final TimeElapser timeElapser = new TimeElapser();

	public synchronized double getElapsedMilliSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000001;
		return result;
	}

	public synchronized long getElapsedNanoSeconds() {
		long result = elapsedTime;
		if (running) {
			result += timeElapser.getElapsedNanoSeconds();
		}
		return result;
	}

	public synchronized double getElapsedSeconds() {
		double result = getElapsedNanoSeconds();
		result *= 0.000000001;
		return result;
	}

	public synchronized int getExecutionCount() {
		return executionCount;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void reset() {
		stop();
		elapsedTime = 0;
		executionCount = 0;
	}

	public synchronized void start() {
		if (!running) {
			timeElapser.reset();
			running = true;
		}
	}

	public synchronized void stop() {
		if (running) {
			elapsedTime += timeElapser.getElapsedNanoSeconds();
			executionCount++;
			running = false;
		}
	}

}
