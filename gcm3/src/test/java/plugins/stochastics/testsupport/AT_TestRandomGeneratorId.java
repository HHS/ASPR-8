package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import plugins.stochastics.support.RandomNumberGeneratorId;

@UnitTest(target = TestRandomGeneratorId.class)
public class AT_TestRandomGeneratorId {
	
	/**
	 * Shows that a generated unknown RandomGeneratorId is not null and not a member
	 * of the enum
	 */
	@Test
	@UnitTestMethod(name = "getUnknownRandomNumberGeneratorId", args = {})
	public void testGetUnknownRandomNumberGeneratorId() {
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			RandomNumberGeneratorId unknownRandomNumberGeneratorId = TestRandomGeneratorId.getUnknownRandomNumberGeneratorId();
			assertNotNull(unknownRandomNumberGeneratorId);
			boolean unique = randomNumberGeneratorIds.add(unknownRandomNumberGeneratorId);
			assertTrue(unique);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				assertNotEquals(testRandomGeneratorId, unknownRandomNumberGeneratorId);
			}
		}
	}
	
	
}
