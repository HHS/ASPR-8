package gov.hhs.aspr.ms.taskit.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_CoreTranslationError {

	@Test
	@UnitTestMethod(target = CoreTranslationError.class, name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (CoreTranslationError coreTranslationError : CoreTranslationError.values()) {
			assertNotNull(coreTranslationError.getDescription());
			assertTrue(coreTranslationError.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (CoreTranslationError coreTranslationError : CoreTranslationError.values()) {
			boolean isUnique = descriptions.add(coreTranslationError.getDescription().toLowerCase());
			assertTrue(isUnique, coreTranslationError + " duplicates the description of another member");
		}
	}
}
