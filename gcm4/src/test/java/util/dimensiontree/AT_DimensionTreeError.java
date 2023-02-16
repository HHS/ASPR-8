package util.dimensiontree;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_DimensionTreeError {

	@Test
	@UnitTestMethod(target = DimensionTreeError.class, name = "getDescription", args = {})
	public void test() {
		// show that each description is a unique, non-null and non-empty string
		Set<String> descriptions = new LinkedHashSet<>();
		for (DimensionTreeError dimensionTreeError : DimensionTreeError.values()) {
			String description = dimensionTreeError.getDescription();
			assertNotNull(description, "null description for " + dimensionTreeError);
			assertTrue(description.length() > 0, "empty string for " + dimensionTreeError);
			boolean unique = descriptions.add(description);
			assertTrue(unique, "description for " + dimensionTreeError + " is not unique");
		}
	}
}
