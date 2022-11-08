package plugins.globalproperties.testsupport;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = TestGlobalPropertyId.class)
public class AT_TestGlobalPropertyId {

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			assertNotNull(testGlobalPropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6173923848365818813L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
				values.add(propertyValue);
				assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));
			}
			//show that the values are reasonable unique
			if (propertyDefinition.getType() != Boolean.class) {
				assertTrue(values.size() > 10);
			} else {
				assertEquals(2, values.size());
			}
		}
	}
	
	@Test
	@UnitTestMethod(name = "getUnknownGlobalPropertyId", args = {})
	public void testGetUnknownRegionId() {
		Set<GlobalPropertyId> unknownGlobalPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GlobalPropertyId unknownGlobalPropertyId = TestGlobalPropertyId.getUnknownGlobalPropertyId();
			assertNotNull(unknownGlobalPropertyId);
			boolean unique = unknownGlobalPropertyIds.add(unknownGlobalPropertyId);
			assertTrue(unique);
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				assertNotEquals(testGlobalPropertyId, unknownGlobalPropertyId);
			}
		}
	}

	
}
