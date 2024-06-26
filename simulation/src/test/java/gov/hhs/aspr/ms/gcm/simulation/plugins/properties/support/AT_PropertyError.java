package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PropertyError {

	@Test
	@UnitTestMethod(target = PropertyError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (PropertyError propertyError : PropertyError.values()) {
			String description = propertyError.getDescription();
			assertNotNull(description, "null description for " + propertyError);
			assertTrue(description.length() > 0, "empty string for " + propertyError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + propertyError + " is not unique");
		}
	}
}
