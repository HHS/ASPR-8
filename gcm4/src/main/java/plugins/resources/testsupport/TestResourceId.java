package plugins.resources.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.resources.support.ResourceId;

/**
 * Enumeration that identifies resources for all tests
 */
public enum TestResourceId implements ResourceId {
	RESOURCE_1(true),
	RESOURCE_2(false),
	RESOURCE_3(true),
	RESOURCE_4(false),
	RESOURCE_5(true);
		

	private final boolean timeTrackingPolicy;

	private TestResourceId(boolean timeTrackingPolicy) {
		this.timeTrackingPolicy = timeTrackingPolicy;
	}

	public boolean getTimeTrackingPolicy() {
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