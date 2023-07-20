package plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_GlobalPropertiesError {

	@Test
	@UnitTestMethod(target = GlobalPropertiesError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (GlobalPropertiesError globalPropertiesError : GlobalPropertiesError.values()) {
			String description = globalPropertiesError.getDescription();
			assertNotNull(description, "null description for " + globalPropertiesError);
			assertTrue(description.length() > 0, "empty string for " + globalPropertiesError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + globalPropertiesError + " is not unique");
		}
	}
}
