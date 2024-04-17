package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;

/**
 * Enumeration that identifies region components for all tests
 */
public enum TestRegionId implements RegionId {
	REGION_1, REGION_2, REGION_3, REGION_4, REGION_5, REGION_6;

	public static TestRegionId getRandomRegionId(final RandomGenerator randomGenerator) {
		return TestRegionId.values()[randomGenerator.nextInt(TestRegionId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestRegionId next;

	public TestRegionId next() {
		if (next == null) {
			next = TestRegionId.values()[(ordinal() + 1) % TestRegionId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link RegionId} instance.
	 */
	public static RegionId getUnknownRegionId() {
		return new RegionId() {
		};
	}
}
