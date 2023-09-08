package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestAuxiliaryGroupPropertyId {

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestAuxiliaryGroupPropertyId testGroupPropertyId : TestAuxiliaryGroupPropertyId.values()) {
			assertNotNull(testGroupPropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4599982550528313954L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestAuxiliaryGroupPropertyId testGroupPropertyId : TestAuxiliaryGroupPropertyId.values()) {
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
	@UnitTestMethod(target = TestAuxiliaryGroupPropertyId.class, name = "getTestGroupTypeId", args = {})
	public void testGetTestGroupTypeId() {
		for (TestAuxiliaryGroupPropertyId testGroupPropertyId : TestAuxiliaryGroupPropertyId.values()) {
			assertNotNull(testGroupPropertyId.getTestGroupTypeId());
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupPropertyId.class, name = "getUnknownGroupPropertyId", args = {})
	public void testGetUnknownGroupPropertyId() {
		/*
		 * Shows that a generated unknown group property id is unique, not null
		 * and not a member of the enum
		 */
		Set<TestAuxiliaryGroupPropertyId> testProperties = EnumSet.allOf(TestAuxiliaryGroupPropertyId.class);
		Set<GroupPropertyId> unknownGroupPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GroupPropertyId unknownGroupPropertyId = TestAuxiliaryGroupPropertyId.getUnknownGroupPropertyId();
			assertNotNull(unknownGroupPropertyId);
			boolean unique = unknownGroupPropertyIds.add(unknownGroupPropertyId);
			assertTrue(unique);
			assertFalse(testProperties.contains(unknownGroupPropertyId));
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupPropertyId.class, name = "getTestGroupPropertyIds", args = { TestAuxiliaryGroupTypeId.class })
	public void testGetTestAuxiliaryGroupPropertyIds() {

		for (TestAuxiliaryGroupTypeId testGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
			// show that each group type has at least one associated property id
			Set<TestAuxiliaryGroupPropertyId> testGroupPropertyIds = TestAuxiliaryGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			assertNotNull(testGroupPropertyIds);
			assertFalse(testGroupPropertyIds.isEmpty());

			// show that each such property id is associated with that group
			// type
			for (TestAuxiliaryGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				assertEquals(testGroupTypeId, testGroupPropertyId.getTestGroupTypeId());
			}
		}
	}
}
