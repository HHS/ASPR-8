package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;

public class AT_GroupError {

	@Test
	@UnitTestMethod(target = GroupError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (GroupError groupError : GroupError.values()) {
			String description = groupError.getDescription();
			assertNotNull(description, "null description for " + groupError);
			assertTrue(description.length() > 0, "empty string for " + groupError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + groupError + " is not unique");
		}
	}
}
