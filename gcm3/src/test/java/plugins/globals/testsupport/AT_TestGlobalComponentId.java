package plugins.globals.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.globals.support.GlobalComponentId;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestGlobalComponentId.class)
public class AT_TestGlobalComponentId {

	
	@Test
	@UnitTestMethod(name = "getUnknownGlobalComponentId", args = {})
	public void testGetUnknownRegionId() {
		Set<GlobalComponentId> unknGlobalComponentIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GlobalComponentId unknownGlobalComponentId = TestGlobalComponentId.getUnknownGlobalComponentId();
			assertNotNull(unknownGlobalComponentId);
			boolean unique = unknGlobalComponentIds.add(unknownGlobalComponentId);
			assertTrue(unique);
			for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
				assertNotEquals(testGlobalComponentId, unknownGlobalComponentId);
			}
		}
	}

	
}
