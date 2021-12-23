package plugins.support;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.properties.support.TimeTrackingPolicy;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * Enumeration that identifies resources for all tests
 */
public enum XTestResourceId implements ResourceId {
	RESOURCE1(TimeTrackingPolicy.TRACK_TIME, "ResourceProperty_1_1", "ResourceProperty_1_2", "ResourceProperty_1_3", "ResourceProperty_1_4"),
	RESOURCE2(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_2_1", "ResourceProperty_2_2"),
	RESOURCE3(TimeTrackingPolicy.TRACK_TIME, "ResourceProperty_3_1", "ResourceProperty_3_2", "ResourceProperty_3_3", "ResourceProperty_3_4", "ResourceProperty_3_5"),
	RESOURCE4(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_4_1", "ResourceProperty_4_2", "ResourceProperty_4_3"),
	RESOURCE5(TimeTrackingPolicy.TRACK_TIME, "ResourceProperty_5_1", "ResourceProperty_5_2"),
	RESOURCE6(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_6_1", "ResourceProperty_6_2", "ResourceProperty_6_3", "ResourceProperty_6_4"),
	RESOURCE7(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_7_1", "ResourceProperty_7_2", "ResourceProperty_7_3", "ResourceProperty_7_4"),
	RESOURCE8(TimeTrackingPolicy.TRACK_TIME, "ResourceProperty_8_1", "ResourceProperty_8_2", "ResourceProperty_8_3"),
	RESOURCE9(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_9_1", "ResourceProperty_9_2", "ResourceProperty_9_3", "ResourceProperty_9_4", "ResourceProperty_9_5", "ResourceProperty_9_6"),
	RESOURCE10(TimeTrackingPolicy.DO_NOT_TRACK_TIME, "ResourceProperty_10_1", "ResourceProperty_10_2", "ResourceProperty_10_3", "ResourceProperty_10_4");

	private static class TestResourcePropertyId implements ResourcePropertyId {
		private final String id;

		private TestResourcePropertyId(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestResourcePropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private final TimeTrackingPolicy trackValueAssignmentTimes;

	private ResourcePropertyId[] resourcePropertyIds;

	private XTestResourceId(TimeTrackingPolicy trackValueAssignmentTimes, String... strings) {
		this.trackValueAssignmentTimes = trackValueAssignmentTimes;

		resourcePropertyIds = new ResourcePropertyId[strings.length];
		for (int i = 0; i < strings.length; i++) {
			resourcePropertyIds[i] = new TestResourcePropertyId(strings[i]);
		}
	}

	public TimeTrackingPolicy trackValueAssignmentTimes() {
		return trackValueAssignmentTimes;
	}

	public static XTestResourceId getRandomResourceId(final RandomGenerator randomGenerator) {
		return XTestResourceId.values()[randomGenerator.nextInt(XTestResourceId.values().length)];
	}

	/**
	 * Returns a new {@link ResourceId} instance.
	 */
	public static ResourceId getUnknownResourceId() {
		return new ResourceId() {
		};
	}

	public ResourcePropertyId[] getResourcePropertyIds() {
		return Arrays.copyOf(resourcePropertyIds, resourcePropertyIds.length);
	}

	public static int size() {
		return values().length;
	}

	private XTestResourceId next;

	public XTestResourceId next() {
		if (next == null) {
			next = XTestResourceId.values()[(ordinal() + 1) % XTestResourceId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link ResourcePropertyId} instance.
	 */
	public static ResourcePropertyId getUnknownResourcePropertyId() {
		return new ResourcePropertyId() {
		};
	}

}