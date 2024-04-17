package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;


public class AT_NucleusError {

	/**
	 * Tests {@link NucleusError#getDescription()}
	 */
	@Test
	@UnitTestMethod(target = NucleusError.class, name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (NucleusError nucleusError : NucleusError.values()) {
			assertNotNull(nucleusError.getDescription());
			assertTrue(nucleusError.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (NucleusError nucleusError : NucleusError.values()) {
			boolean isUnique = descriptions.add(nucleusError.getDescription().toLowerCase());
			assertTrue(isUnique, nucleusError + " duplicates the description of another member");
		}
	}

}
