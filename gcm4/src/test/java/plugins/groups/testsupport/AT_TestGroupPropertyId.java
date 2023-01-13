package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestGroupPropertyId {

	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			assertNotNull(testGroupPropertyId.getPropertyDefinition());
		}

	}

	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "getTestGroupTypeId", args = {})
	public void testGetTestGroupTypeId() {
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			assertNotNull(testGroupPropertyId.getTestGroupTypeId());
		}
	}

	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "getTestGroupPropertyIds", args = { TestGroupTypeId.class })
	public void testGetTestGroupPropertyIds() {

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			// show that each group type has at least one associated property id
			Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			assertNotNull(testGroupPropertyIds);
			assertFalse(testGroupPropertyIds.isEmpty());

			// show that each such property id is associated with that group
			// type
			for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				assertEquals(testGroupTypeId, testGroupPropertyId.getTestGroupTypeId());
			}
		}
	}

	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "getUnknownGroupPropertyId", args = {})
	public void testGetUnknownGroupPropertyId() {
		/*
		 * Shows that a generated unknown group property id is unique, not null
		 * and not a member of the enum
		 */
		Set<TestGroupPropertyId> testProperties = EnumSet.allOf(TestGroupPropertyId.class);
		Set<GroupPropertyId> unknownGroupPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GroupPropertyId unknownGroupPropertyId = TestGroupPropertyId.getUnknownGroupPropertyId();
			assertNotNull(unknownGroupPropertyId);
			boolean unique = unknownGroupPropertyIds.add(unknownGroupPropertyId);
			assertTrue(unique);
			assertFalse(testProperties.contains(unknownGroupPropertyId));
		}
	}

	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6173923848365818813L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			PropertyDefinition propertyDefinition = testGroupPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				values.add(propertyValue);
				assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));
			}
			// show that the values are reasonable unique
			if (propertyDefinition.getType() != Boolean.class) {
				assertTrue(values.size() > 10);
			} else {
				assertEquals(2, values.size());
			}
		}
	}
	
	@Test
	@UnitTestMethod(target = TestGroupPropertyId.class, name = "next", args = {})
	public void testNext() {
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			int index = (testGroupPropertyId.ordinal() + 1) % TestGroupPropertyId.values().length;
			TestGroupPropertyId expectedNext = TestGroupPropertyId.values()[index];
			assertEquals(expectedNext, testGroupPropertyId.next());
		}
		
	}
	
	 
}
