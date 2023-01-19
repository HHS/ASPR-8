package plugins.util.properties;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * An enumeration used to control the tracking of assignment times of properties
 * and other values.
 * 
 *
 */
public enum TimeTrackingPolicy {
	TRACK_TIME, DO_NOT_TRACK_TIME;

	private TimeTrackingPolicy next;

	public synchronized TimeTrackingPolicy next() {
		if (next == null) {
			next = TimeTrackingPolicy.values()[(ordinal() + 1) % TimeTrackingPolicy.values().length];
		}
		return next;
	}

	public static TimeTrackingPolicy getRandomTimeTrackingPolicy(final RandomGenerator randomGenerator) {
		if (randomGenerator.nextBoolean()) {
			return TRACK_TIME;
		}
		return DO_NOT_TRACK_TIME;
	}
}
