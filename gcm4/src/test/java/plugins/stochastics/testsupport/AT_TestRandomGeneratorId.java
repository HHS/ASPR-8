package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.stochastics.support.RandomNumberGeneratorId;
import tools.annotations.UnitTestMethod;

public class AT_TestRandomGeneratorId {

	/**
	 * Shows that a generated unknown RandomGeneratorId is not null and not a
	 * member of the enum
	 */
	@Test
	@UnitTestMethod(target = TestRandomGeneratorId.class, name = "getUnknownRandomNumberGeneratorId", args = {})
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
