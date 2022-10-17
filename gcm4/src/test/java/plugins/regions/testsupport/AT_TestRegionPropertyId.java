package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = TestRegionPropertyId.class)
public class AT_TestRegionPropertyId {

	/**
	 * Shows that a generated unknown region property id is not null and
	 * not a member of the enum and is unique.
	 */
	@Test
	@UnitTestMethod(name = "getUnknownRegionPropertyId", args = {})
	public void testGetUnknownRegionPropertyId() {
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		assertNotNull(unknownRegionPropertyId);
		for(TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			assertNotEquals(testRegionPropertyId, unknownRegionPropertyId);
		}
		
		Set<RegionPropertyId> unknownRegionPropertyIds = new LinkedHashSet<>();
		for(int i = 0;i<10;i++) {
			unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			assertNotNull(unknownRegionPropertyId);
			boolean unique = unknownRegionPropertyIds.add(unknownRegionPropertyId);
			assertTrue(unique);			
		}
	}

	/**
	 * Shows that size() returns the number of members in the TestRegionId
	 * enum
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestRegionPropertyId.values().length, TestRegionPropertyId.size());
	}
	
}
