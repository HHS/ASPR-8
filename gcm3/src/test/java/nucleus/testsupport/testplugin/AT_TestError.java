package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;

@UnitTest(target = TestError.class)
public class AT_TestError {

	@Test
	@UnitTestMethod(name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (TestError nucleusError : TestError.values()) {
			assertNotNull(nucleusError.getDescription());
			assertTrue(nucleusError.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (TestError nucleusError : TestError.values()) {
			assertTrue(descriptions.add(nucleusError.getDescription().toLowerCase()), nucleusError+": "+"Duplicate ErrorType description: " + nucleusError.getDescription());
		}
	}

	
}
