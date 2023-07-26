package gov.hhs.aspr.ms.gcm.plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_ResourceError {

	@Test
	@UnitTestMethod(target = ResourceError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (ResourceError resourceError : ResourceError.values()) {
			String description = resourceError.getDescription();
			assertNotNull(description, "null description for " + resourceError);
			assertTrue(description.length() > 0, "empty string for " + resourceError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + resourceError + " is not unique");
		}
	}
}
