package plugins.support;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.MaterialsProducerId;

/**
 * Enumeration that identifies region components for all tests
 */
public enum XTestMaterialsProducerId implements MaterialsProducerId {
	MATERIALS_PRODUCER_1, MATERIALS_PRODUCER_2, MATERIALS_PRODUCER_3;

	public static XTestMaterialsProducerId getRandomMaterialsProducerId(final RandomGenerator randomGenerator) {
		return XTestMaterialsProducerId.values()[randomGenerator.nextInt(XTestMaterialsProducerId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private XTestMaterialsProducerId next;

	public XTestMaterialsProducerId next() {
		if (next == null) {
			next = XTestMaterialsProducerId.values()[(ordinal() + 1) % XTestMaterialsProducerId.values().length];
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
