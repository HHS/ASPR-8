package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestAttributeId {

	@Test
	@UnitTestMethod(target = TestAttributeId.class,name = "getUnknownAttributeId", args = {})
	public void testGetUnknownAttributeId() {
		/*
		 * Show that a generated unknown attribute id is not null and not a
		 * member of the enum
		 */
		Set<AttributeId> attributeIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			AttributeId unknownAttributeId = TestAttributeId.getUnknownAttributeId();
			assertNotNull(unknownAttributeId);
			boolean unique = attributeIds.add(unknownAttributeId);
			assertTrue(unique);
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertNotEquals(testAttributeId, unknownAttributeId);
			}
		}
	}

	@Test
	@UnitTestMethod(target = TestAttributeId.class,name = "getAttributeDefinition", args = {})
	public void testGetAttributeDefinition() {
		// show that each member has an attribute definition
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertNotNull(testAttributeId.getAttributeDefinition());
		}
	}

	@Test
	@UnitTestMethod(target = TestAttributeId.class,name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(675234329644922354L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				values.add(propertyValue);
				assertTrue(attributeDefinition.getType().isAssignableFrom(propertyValue.getClass()));
			}
			// show that the values are reasonable unique
			if (attributeDefinition.getType() != Boolean.class) {
				assertTrue(values.size() > 10);
			} else {
				assertEquals(2, values.size());
			}
		}
	}

}
