package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_TestError {

	@Test
	@UnitTestMethod(target = TestError.class,name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (TestError testError : TestError.values()) {
			assertNotNull(testError.getDescription());
			assertTrue(testError.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (TestError testError : TestError.values()) {
			assertTrue(descriptions.add(testError.getDescription().toLowerCase()), testError+": "+"Duplicate ErrorType description: " + testError.getDescription());
		}
	}

	
}
