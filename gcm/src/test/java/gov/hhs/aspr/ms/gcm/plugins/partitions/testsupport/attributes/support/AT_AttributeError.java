package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_AttributeError {
	@Test
	@UnitTestMethod(target = AttributeError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (AttributeError attributeError : AttributeError.values()) {
			String description = attributeError.getDescription();
			assertNotNull(description, "null description for " + attributeError);
			assertTrue(description.length() > 0, "empty string for " + attributeError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + attributeError + " is not unique");
		}
	}

}
