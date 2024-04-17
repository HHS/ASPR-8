package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_RegionError {

	@Test
	@UnitTestMethod(target = RegionError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (RegionError regionError : RegionError.values()) {
			String description = regionError.getDescription();
			assertNotNull(description, "null description for " + regionError);
			assertTrue(description.length() > 0, "empty string for " + regionError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + regionError + " is not unique");
		}
	}
}
