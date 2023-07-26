package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;

/**
 * Enumeration of GroupTypeId to support unit testing
 */
public enum TestAuxiliaryGroupTypeId implements GroupTypeId {
	GROUP_AUX_TYPE_1, GROUP_AUX_TYPE_2, GROUP_AUX_TYPE_3;

	/**
	 * Returns a randomly selected member of this enumeration.
	 * 
	 * Precondition: The random generator must not be null
	 */
	public static TestAuxiliaryGroupTypeId getRandomGroupTypeId(final RandomGenerator randomGenerator) {
		return TestAuxiliaryGroupTypeId.values()[randomGenerator.nextInt(TestAuxiliaryGroupTypeId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestAuxiliaryGroupTypeId next;

	/**
	 * Returns the next member of this enumeration
	 */
	public TestAuxiliaryGroupTypeId next() {
		if (next == null) {
			next = TestAuxiliaryGroupTypeId.values()[(ordinal() + 1) % TestAuxiliaryGroupTypeId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link GroupTypeId} instance.
	 */
	public static GroupTypeId getUnknownGroupTypeId() {
		return new GroupTypeId() {
		};
	}

}
