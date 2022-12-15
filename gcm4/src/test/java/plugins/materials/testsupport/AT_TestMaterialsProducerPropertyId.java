package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = TestMaterialsProducerPropertyId.class)
public class AT_TestMaterialsProducerPropertyId {

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);			
		}
	}

	@Test
	@UnitTestMethod(name = "getUnknownMaterialsProducerPropertyId", args = {})
	public void testGetUnknownMaterialsProducerPropertyId() {
		MaterialsProducerPropertyId unknownMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
		assertNotNull(unknownMaterialsProducerPropertyId);
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			assertNotEquals(testMaterialsProducerPropertyId, unknownMaterialsProducerPropertyId);
		}
		MaterialsProducerPropertyId unknownMaterialsProducerPropertyId2 = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
		assertNotEquals(unknownMaterialsProducerPropertyId, unknownMaterialsProducerPropertyId2);

	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7973900878959109442L);

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			for (int i = 0; i < 10; i++) {
				Object value = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				assertNotNull(value);
				assertTrue(propertyDefinition.getType().isAssignableFrom(value.getClass()));
			}
		}

	}

	@Test
	@UnitTestMethod(name = "getRandomMaterialsProducerPropertyId", args = { RandomGenerator.class })
	public void testGetRandomMaterialsProducerPropertyId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5963531689679394818L);

		for (int i = 0; i < 10; i++) {
			TestMaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.getRandomMaterialsProducerPropertyId(randomGenerator);
			assertNotNull(producerPropertyId);
		}

	}

	@Test
	@UnitTestMethod(name = "getRandomMutableMaterialsProducerPropertyId", args = { RandomGenerator.class })
	public void testGetRandomMutableMaterialsProducerPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8476649750185982818L);
		for (int i = 0; i < 10; i++) {
			TestMaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
			assertNotNull(producerPropertyId);
			assertTrue(producerPropertyId.getPropertyDefinition().propertyValuesAreMutable());
		}

		
	}

	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestMaterialsProducerPropertyId.values().length, TestMaterialsProducerPropertyId.size());
	}

	@Test
	@UnitTestMethod(name = "getPropertiesWithDefaultValues", args = {})
	public void testGetPropertesWithDefaultValues() {
		List<TestMaterialsProducerPropertyId> expectedValues = new ArrayList<>();

		for (TestMaterialsProducerPropertyId id : TestMaterialsProducerPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isPresent()) {
				expectedValues.add(id);
			}
		}

		List<TestMaterialsProducerPropertyId> actualValues = TestMaterialsProducerPropertyId.getPropertiesWithDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestMaterialsProducerPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestMaterialsProducerPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(name = "getPropertiesWithoutDefaultValues", args = {})
	public void testGetPropertesWithoutDefaultValues() {
		List<TestMaterialsProducerPropertyId> expectedValues = new ArrayList<>();

		for (TestMaterialsProducerPropertyId id : TestMaterialsProducerPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isEmpty()) {
				expectedValues.add(id);
			}
		}

		List<TestMaterialsProducerPropertyId> actualValues = TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestMaterialsProducerPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestMaterialsProducerPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}
}
