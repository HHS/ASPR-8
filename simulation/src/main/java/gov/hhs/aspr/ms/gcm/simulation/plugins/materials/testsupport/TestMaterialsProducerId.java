package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;

/**
 * A test support enumeration that contains a variety of materials producer id
 * values. Supports random selection and generation of unique, unknown material
 * producer ids.
 */
public enum TestMaterialsProducerId implements MaterialsProducerId {
	MATERIALS_PRODUCER_1, MATERIALS_PRODUCER_2, MATERIALS_PRODUCER_3;

	public static TestMaterialsProducerId getRandomMaterialsProducerId(final RandomGenerator randomGenerator) {
		return TestMaterialsProducerId.values()[randomGenerator.nextInt(TestMaterialsProducerId.values().length)];
	}

	/**
	 * Returns the number of elements in this enumeration
	 */
	public static int size() {
		return values().length;
	}

	private TestMaterialsProducerId next;

	/**
	 * Returns the next member of this enumeration in natural order with roll over.
	 */
	public TestMaterialsProducerId next() {
		if (next == null) {
			next = TestMaterialsProducerId.values()[(ordinal() + 1) % TestMaterialsProducerId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link MaterialsProducerId} instance.
	 */
	public static MaterialsProducerId getUnknownMaterialsProducerId() {
		return new MaterialsProducerId() {
		};
	}
}
