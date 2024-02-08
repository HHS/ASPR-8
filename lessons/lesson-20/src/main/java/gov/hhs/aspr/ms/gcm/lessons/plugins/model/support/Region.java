package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;

/**
 * Identifier for all regions
 *
 *
 */

public enum Region implements RegionId {

	REGION_1, REGION_2, REGION_3;
	
	public static Region getRandomRegion(final RandomGenerator randomGenerator) {
		return Region.values()[randomGenerator.nextInt(Region.values().length)];
	}

}
