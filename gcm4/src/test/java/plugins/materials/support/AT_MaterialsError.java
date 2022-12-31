package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;

public class AT_MaterialsError {

	@Test
	@UnitTestMethod(target = MaterialsError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (MaterialsError materialsError : MaterialsError.values()) {
			String description = materialsError.getDescription();
			assertNotNull(description, "null description for " + materialsError);
			assertTrue(description.length() > 0, "empty string for " + materialsError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + materialsError + " is not unique");
		}
	}
}
