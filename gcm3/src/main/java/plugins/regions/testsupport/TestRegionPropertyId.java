package plugins.regions.testsupport;

import plugins.compartments.support.CompartmentId;
import plugins.regions.support.RegionPropertyId;

/**
 * Enumeration that identifies region property definitions
 */
public enum TestRegionPropertyId implements RegionPropertyId {
	REGION_PROPERTY_1, REGION_PROPERTY_2, REGION_PROPERTY_3, REGION_PROPERTY_4, REGION_PROPERTY_5, REGION_PROPERTY_6, REGION_PROPERTY_7, REGION_PROPERTY_8, REGION_PROPERTY_9, REGION_PROPERTY_10, REGION_PROPERTY_11;
	
	/**
	 * Return the size of this enum
	 */
	public static int size() {
		return values().length;
	}

	/**
	 * Returns a new {@link CompartmentId} instance.
	 */
	public static RegionPropertyId getUnknownRegionPropertyId() {
		return new RegionPropertyId() {
		};
	}

}