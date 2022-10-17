package plugins.resources.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.resources.support.ResourceId;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * Enumeration that identifies resources for all tests
 */
public enum TestResourceId implements ResourceId {
	RESOURCE_1(TimeTrackingPolicy.TRACK_TIME),
	RESOURCE_2(TimeTrackingPolicy.DO_NOT_TRACK_TIME),
	RESOURCE_3(TimeTrackingPolicy.TRACK_TIME),
	RESOURCE_4(TimeTrackingPolicy.DO_NOT_TRACK_TIME),
	RESOURCE_5(TimeTrackingPolicy.TRACK_TIME);
		

	private final TimeTrackingPolicy timeTrackingPolicy;

	private TestResourceId(TimeTrackingPolicy timeTrackingPolicy) {
		this.timeTrackingPolicy = timeTrackingPolicy;
	}

	public TimeTrackingPolicy getTimeTrackingPolicy() {
		return timeTrackingPolicy;
	}

	public static TestResourceId getRandomResourceId(final RandomGenerator randomGenerator) {
		return TestResourceId.values()[randomGenerator.nextInt(TestResourceId.values().length)];
	}

	/**
	 * Returns a new {@link ResourceId} instance.
	 */
	public static ResourceId getUnknownResourceId() {
		return new ResourceId() {
		};
	}
	
	public static int size() {
		return values().length;
	}

	private TestResourceId next;

	public TestResourceId next() {
		if (next == null) {
			next = TestResourceId.values()[(ordinal() + 1) % TestResourceId.values().length];
		}
		return next;
	}


}