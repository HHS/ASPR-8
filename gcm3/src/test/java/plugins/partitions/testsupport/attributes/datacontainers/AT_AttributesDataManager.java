package plugins.partitions.testsupport.attributes.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.NucleusError;
import nucleus.testsupport.MockSimulationContext;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonId;
import util.ContractException;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributesDataManager.class)
public class AT_AttributesDataManager {

	@Test
	@UnitTestMethod(name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		Map<AttributeId, AttributeDefinition> expectedAttributeDefinitions = new LinkedHashMap<>();

		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);

			expectedAttributeDefinitions.put(testAttributeId, attributeDefinition);
		}

		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition expectedAttributeDefinition = expectedAttributeDefinitions.get(testAttributeId);
			AttributeDefinition actualAttributeDefinition = attributesDataManager.getAttributeDefinition(testAttributeId);
			assertEquals(expectedAttributeDefinition, actualAttributeDefinition);
		}
		/*
		 * Show that an attribute id that is unknown will return a null
		 * attribute definition
		 */
		assertNull(attributesDataManager.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));

		/*
		 * Show that a null attribute id will return a null attribute definition
		 */
		assertNull(attributesDataManager.getAttributeDefinition(null));

	}

	@Test
	@UnitTestMethod(name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		Set<AttributeId> expectedAttributeIds = new LinkedHashSet<>();

		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);

			expectedAttributeIds.add(testAttributeId);
		}

		assertEquals(expectedAttributeIds, attributesDataManager.getAttributeIds());

	}

	@Test
	@UnitTestMethod(name = "getAttributeValue", args = { PersonId.class, AttributeId.class })
	public void testGetAttributeValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3563728721933611714L);

		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		// add the attribute definitions to the manager
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);
		}

		// add some people and set their values
		Map<PersonId, Map<AttributeId, Object>> expectedValuesMap = new LinkedHashMap<>();

		for (int i = 0; i < 30; i++) {
			PersonId personId = new PersonId(i);
			Map<AttributeId, Object> attributeMap = new LinkedHashMap<>();
			expectedValuesMap.put(personId, attributeMap);

			boolean b0 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, b0);
			attributeMap.put(TestAttributeId.BOOLEAN_0, b0);

			boolean b1 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, b1);
			attributeMap.put(TestAttributeId.BOOLEAN_1, b1);

			int i0 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, i0);
			attributeMap.put(TestAttributeId.INT_0, i0);

			int i1 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, i1);
			attributeMap.put(TestAttributeId.INT_1, i1);

			double d0 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, d0);
			attributeMap.put(TestAttributeId.DOUBLE_0, d0);

			double d1 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, d1);
			attributeMap.put(TestAttributeId.DOUBLE_1, d1);
		}

		// show that the expected values are returned
		for (PersonId personId : expectedValuesMap.keySet()) {
			Map<AttributeId, Object> attributeMap = expectedValuesMap.get(personId);
			for (AttributeId attributeId : attributeMap.keySet()) {
				Object expectedValue = attributeMap.get(attributeId);
				Object actualValue = attributesDataManager.getAttributeValue(personId, attributeId);
				assertEquals(expectedValue, actualValue);
			}
		}

	}

	@Test
	@UnitTestMethod(name = "handlePersonRemoval", args = { PersonId.class })
	public void testHandlePersonRemoval() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(978705363753978374L);

		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		// add the attribute definitions to the manager
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);
		}

		Set<PersonId> people = new LinkedHashSet<>();

		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i);
			people.add(personId);

			boolean b0 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, b0);

			boolean b1 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, b1);

			int i0 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, i0);

			int i1 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, i1);

			double d0 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, d0);

			double d1 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, d1);
		}

		// show that the removal of a person causes that person to return to
		// default values
		for (PersonId personId : people) {
			attributesDataManager.handlePersonRemoval(personId);
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object expectedValue = attributesDataManager.getAttributeDefinition(testAttributeId).getDefaultValue();
				Object actualValue = attributesDataManager.getAttributeValue(personId, testAttributeId);
				assertEquals(expectedValue, actualValue);
			}
		}

	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new AttributesDataManager(null));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "attributeExists", args = { AttributeId.class })
	public void testAttributeExists() {

		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		// add the attribute definitions to the manager
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);
		}

		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertTrue(attributesDataManager.attributeExists(testAttributeId));
		}

		assertFalse(attributesDataManager.attributeExists(TestAttributeId.getUnknownAttributeId()));
		assertFalse(attributesDataManager.attributeExists(null));

	}

	@Test
	@UnitTestMethod(name = "setAttributeValue", args = { PersonId.class, AttributeId.class, Object.class })
	public void testSetAttributeValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3563728721933611714L);

		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		// add the attribute definitions to the manager
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			attributesDataManager.addAttribute(testAttributeId, attributeDefinition);
		}

		// add some people and set their values
		Map<PersonId, Map<AttributeId, Object>> expectedValuesMap = new LinkedHashMap<>();

		for (int i = 0; i < 30; i++) {
			PersonId personId = new PersonId(i);
			Map<AttributeId, Object> attributeMap = new LinkedHashMap<>();
			expectedValuesMap.put(personId, attributeMap);

			boolean b0 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, b0);
			attributeMap.put(TestAttributeId.BOOLEAN_0, b0);

			boolean b1 = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, b1);
			attributeMap.put(TestAttributeId.BOOLEAN_1, b1);

			int i0 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, i0);
			attributeMap.put(TestAttributeId.INT_0, i0);

			int i1 = randomGenerator.nextInt();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, i1);
			attributeMap.put(TestAttributeId.INT_1, i1);

			double d0 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, d0);
			attributeMap.put(TestAttributeId.DOUBLE_0, d0);

			double d1 = randomGenerator.nextDouble();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, d1);
			attributeMap.put(TestAttributeId.DOUBLE_1, d1);
		}

		// show that the expected values are returned
		for (PersonId personId : expectedValuesMap.keySet()) {
			Map<AttributeId, Object> attributeMap = expectedValuesMap.get(personId);
			for (AttributeId attributeId : attributeMap.keySet()) {
				Object expectedValue = attributeMap.get(attributeId);
				Object actualValue = attributesDataManager.getAttributeValue(personId, attributeId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// precondition tests

		// if the attribute id is null
		assertThrows(RuntimeException.class, () -> attributesDataManager.setAttributeValue(new PersonId(0), null, 12.2));

		// if the attribute id is unknown
		assertThrows(RuntimeException.class, () -> attributesDataManager.setAttributeValue(new PersonId(0), TestAttributeId.getUnknownAttributeId(), 12.2));

	}

	@Test
	@UnitTestMethod(name = "addAttribute", args = { AttributeId.class, AttributeDefinition.class })
	public void testAddAttribute() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		AttributesDataManager attributesDataManager = new AttributesDataManager(mockSimulationContext);

		AttributeDefinition attributeDefinition = TestAttributeId.BOOLEAN_0.getAttributeDefinition();
		attributesDataManager.addAttribute(TestAttributeId.BOOLEAN_0, attributeDefinition);

		attributeDefinition = TestAttributeId.INT_0.getAttributeDefinition();
		attributesDataManager.addAttribute(TestAttributeId.INT_0, attributeDefinition);

		attributeDefinition = TestAttributeId.DOUBLE_0.getAttributeDefinition();
		attributesDataManager.addAttribute(TestAttributeId.DOUBLE_0, attributeDefinition);

		AttributeDefinition expectedAttributeDefinition = TestAttributeId.BOOLEAN_0.getAttributeDefinition();
		AttributeDefinition actualAttributeDefinition = attributesDataManager.getAttributeDefinition(TestAttributeId.BOOLEAN_0);
		assertEquals(expectedAttributeDefinition, actualAttributeDefinition);

		expectedAttributeDefinition = TestAttributeId.INT_0.getAttributeDefinition();
		actualAttributeDefinition = attributesDataManager.getAttributeDefinition(TestAttributeId.INT_0);
		assertEquals(expectedAttributeDefinition, actualAttributeDefinition);

		expectedAttributeDefinition = TestAttributeId.DOUBLE_0.getAttributeDefinition();
		actualAttributeDefinition = attributesDataManager.getAttributeDefinition(TestAttributeId.DOUBLE_0);
		assertEquals(expectedAttributeDefinition, actualAttributeDefinition);

		// precondition checks

		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.addAttribute(null, TestAttributeId.BOOLEAN_1.getAttributeDefinition()));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute definition is null
		contractException = assertThrows(ContractException.class, () -> attributesDataManager.addAttribute(TestAttributeId.BOOLEAN_1, null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_DEFINITION, contractException.getErrorType());

		// if the attribute definition was previously added
		contractException = assertThrows(ContractException.class, () -> attributesDataManager.addAttribute(TestAttributeId.BOOLEAN_0, TestAttributeId.BOOLEAN_0.getAttributeDefinition()));
		assertEquals(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION, contractException.getErrorType());

	}

}
