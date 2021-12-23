package plugins.support;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.MaterialsProducerPropertyId;

/**
 * Enumeration that identifies region components for all tests
 */
public enum XTestMaterialsProducerPropertyId implements MaterialsProducerPropertyId {
	MATERIALS_PRODUCER_PROPERTY_1,
	MATERIALS_PRODUCER_PROPERTY_2,
	MATERIALS_PRODUCER_PROPERTY_3,
	MATERIALS_PRODUCER_PROPERTY_4;

	public static XTestMaterialsProducerPropertyId getRandomMaterialsProducerId(final RandomGenerator randomGenerator) {
		return XTestMaterialsProducerPropertyId.values()[randomGenerator.nextInt(XTestMaterialsProducerPropertyId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private XTestMaterialsProducerPropertyId next;

	public XTestMaterialsProducerPropertyId next() {
		if (next == null) {
			next = XTestMaterialsProducerPropertyId.values()[(ordinal() + 1) % XTestMaterialsProducerPropertyId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link MaterialsProducerPropertyId} instance.
	 */
	public static MaterialsProducerPropertyId getUnknownMaterialsProducerPropertyId() {
		return new MaterialsProducerPropertyId() {
		};
	}
}
