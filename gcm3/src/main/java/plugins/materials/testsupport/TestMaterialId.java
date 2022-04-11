package plugins.materials.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.MaterialId;

/**
 * A test support enumeration that contains a variety of material id values.
 * Supports random selection and generation of unique, unknown material ids.
 * 
 * @author Shawn Hatch
 *
 */
public enum TestMaterialId implements MaterialId {
	MATERIAL_1, MATERIAL_2, MATERIAL_3;

	/**
	 * Returns a randomly selected member of this enumeration
	 */
	public static TestMaterialId getRandomMaterialId(final RandomGenerator randomGenerator) {
		return TestMaterialId.values()[randomGenerator.nextInt(TestMaterialId.values().length)];
	}

	/**
	 * Returns the number of element in this enumeration
	 */
	public static int size() {
		return values().length;
	}

	private TestMaterialId next;

	/**
	 * Returns the next member of this enumeration in the natural order with roll over.
	 */
	public TestMaterialId next() {
		if (next == null) {
			next = TestMaterialId.values()[(ordinal() + 1) % TestMaterialId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new, unique {@link MaterialId} instance.
	 */
	public static MaterialId getUnknownMaterialId() {
		return new MaterialId() {
		};
	}

}
