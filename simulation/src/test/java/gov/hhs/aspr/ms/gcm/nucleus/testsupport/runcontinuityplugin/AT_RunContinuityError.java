package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_RunContinuityError {

	@Test
	@UnitTestMethod(target = RunContinuityError.class,name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (RunContinuityError runContinuityError : RunContinuityError.values()) {
			assertNotNull(runContinuityError.getDescription());
			assertTrue(runContinuityError.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (RunContinuityError runContinuityError : RunContinuityError.values()) {
			assertTrue(descriptions.add(runContinuityError.getDescription().toLowerCase()), runContinuityError+": "+"Duplicate ErrorType description: " + runContinuityError.getDescription());
		}
	}

	
}
