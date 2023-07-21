package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AT_AttributesPluginData {

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(AttributesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(AttributesPluginData.builder().build());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.Builder.class, name = "defineAttribute", args = { AttributeId.class,
			AttributeDefinition.class })
	public void testDefineAttribute() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId.getAttributeDefinition(),
					attributesPluginData.getAttributeDefinition(testAttributeId));
		}

		// precondition tests

		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> AttributesPluginData.builder()
				.defineAttribute(null, TestAttributeId.BOOLEAN_0.getAttributeDefinition()));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute definition is null
		contractException = assertThrows(ContractException.class,
				() -> AttributesPluginData.builder().defineAttribute(TestAttributeId.BOOLEAN_0, null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_DEFINITION, contractException.getErrorType());

		// if the attribute id was previously added
		contractException = assertThrows(ContractException.class, () -> {
			AttributesPluginData.builder()//
					.defineAttribute(TestAttributeId.BOOLEAN_0, TestAttributeId.BOOLEAN_0.getAttributeDefinition())
					.defineAttribute(TestAttributeId.BOOLEAN_0, TestAttributeId.BOOLEAN_0.getAttributeDefinition());
		});
		assertEquals(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId.getAttributeDefinition(),
					attributesPluginData.getAttributeDefinition(testAttributeId));
		}

		// precondition tests

		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> attributesPluginData.getAttributeDefinition(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute id is unknown</li>
		contractException = assertThrows(ContractException.class,
				() -> attributesPluginData.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));
		assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		AttributesPluginData attributesPluginData = builder.build();
		PluginDataBuilder cloneBuilder = attributesPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);

		PluginData pluginData = cloneBuilder.build();

		assertTrue(pluginData instanceof AttributesPluginData);

		AttributesPluginData clonePluginData = (AttributesPluginData) pluginData;
		assertEquals(attributesPluginData.getAttributeIds(), clonePluginData.getAttributeIds());
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(attributesPluginData.getAttributeDefinition(testAttributeId),
					clonePluginData.getAttributeDefinition(testAttributeId));
		}
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getAttributeValues", args = {
			AttributeId.class })
	public void testGetAttributeValues_AttributeId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3876639732263152250L);

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(2 * i + 1);
			people.add(personId);
		}

		for (PersonId personId : people) {
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
				expectedValues.put(new MultiKey(personId, testAttributeId), propertyValue);
			}
		}

		AttributesPluginData attributesPluginData = builder.build();

		Map<MultiKey, Object> actualValues = new LinkedHashMap<>();

		for (AttributeId attributeId : attributesPluginData.getAttributeIds()) {
			List<Object> list = attributesPluginData.getAttributeValues(attributeId);
			for (int i = 0; i < list.size(); i++) {
				Object propertyValue = list.get(i);
				if (propertyValue != null) {
					Object replacedValue = actualValues.put(new MultiKey(new PersonId(i), attributeId), propertyValue);
					assertNull(replacedValue);
				}
			}
		}

		assertEquals(expectedValues, actualValues);
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getAttributeValues", args = {})
	public void testGetAttributeValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3876639732263152250L);

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(2 * i + 1);
			people.add(personId);
		}

		for (PersonId personId : people) {
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
				expectedValues.put(new MultiKey(personId, testAttributeId), propertyValue);
			}
		}

		AttributesPluginData attributesPluginData = builder.build();

		Map<MultiKey, Object> actualValues = new LinkedHashMap<>();

		Map<AttributeId, List<Object>> attributeValues = attributesPluginData.getAttributeValues();
		for (AttributeId attributeId : attributeValues.keySet()) {
			List<Object> list = attributeValues.get(attributeId);
			for (int i = 0; i < list.size(); i++) {
				Object propertyValue = list.get(i);
				if (propertyValue != null) {
					Object replacedValue = actualValues.put(new MultiKey(new PersonId(i), attributeId), propertyValue);
					assertNull(replacedValue);
				}
			}
		}

		assertEquals(expectedValues, actualValues);
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.Builder.class, name = "setPersonAttributeValue", args = {
			PersonId.class, AttributeId.class, Object.class })
	public void testSetPersonAttributeValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5976036831935756004L);

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(2 * i + 1);
			people.add(personId);
		}

		for (PersonId personId : people) {
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
				expectedValues.put(new MultiKey(personId, testAttributeId), propertyValue);
			}
		}

		AttributesPluginData attributesPluginData = builder.build();

		Map<MultiKey, Object> actualValues = new LinkedHashMap<>();

		Map<AttributeId, List<Object>> attributeValues = attributesPluginData.getAttributeValues();
		for (AttributeId attributeId : attributeValues.keySet()) {
			List<Object> list = attributeValues.get(attributeId);
			for (int i = 0; i < list.size(); i++) {
				Object propertyValue = list.get(i);
				if (propertyValue != null) {
					Object replacedValue = actualValues.put(new MultiKey(new PersonId(i), attributeId), propertyValue);
					assertNull(replacedValue);
				}
			}
		}

		assertEquals(expectedValues, actualValues);

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> AttributesPluginData.builder().setPersonAttributeValue(null, TestAttributeId.BOOLEAN_0, false));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the attribute property id is null
		contractException = assertThrows(ContractException.class,
				() -> AttributesPluginData.builder().setPersonAttributeValue(new PersonId(0), null, false));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute property value is null
		contractException = assertThrows(ContractException.class, () -> {
			AttributesPluginData.builder().setPersonAttributeValue(new PersonId(0), TestAttributeId.BOOLEAN_0, null);

		});
		assertEquals(AttributeError.NULL_ATTRIBUTE_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "getAttributeDefinitions", args = {})
	public void testGetAttributeDefinitions() {

		Map<AttributeId, AttributeDefinition> expectedAttributeDefinitions = new LinkedHashMap<>();

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			builder.defineAttribute(testAttributeId, attributeDefinition);
			expectedAttributeDefinitions.put(testAttributeId, attributeDefinition);
		}

		AttributesPluginData attributesPluginData = builder.build();

		Map<AttributeId, AttributeDefinition> actualAttributeDefinitions = attributesPluginData
				.getAttributeDefinitions();
		assertEquals(expectedAttributeDefinitions, actualAttributeDefinitions);

	}

	private AttributesPluginData getRandomAttributesPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Random random = new Random(randomGenerator.nextLong());

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		List<TestAttributeId> list = Arrays.asList(TestAttributeId.values());
		Collections.shuffle(list, random);
		int count = Math.max(1, randomGenerator.nextInt(list.size()));
		List<TestAttributeId> selectedAttributeIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			selectedAttributeIds.add(list.get(i));
		}

		for (TestAttributeId testAttributeId : selectedAttributeIds) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			builder.defineAttribute(testAttributeId, attributeDefinition);
		}
		count = randomGenerator.nextInt(5);
		int id = 0;
		for (int i = 0; i < count; i++) {
			id += randomGenerator.nextInt(3);
			PersonId personId = new PersonId(id);
			for (TestAttributeId testAttributeId : selectedAttributeIds) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
			}
		}

		return builder.build();
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8530033813336044717L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributesPluginData attributesPluginData1 = getRandomAttributesPluginData(seed);
			AttributesPluginData attributesPluginData2 = getRandomAttributesPluginData(seed);
			assertEquals(attributesPluginData1, attributesPluginData2);
			assertEquals(attributesPluginData1.hashCode(), attributesPluginData2.hashCode());
		}

		// hash codes are reasonably distributed 
		Set<Integer> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			AttributesPluginData attributesPluginData = getRandomAttributesPluginData(randomGenerator.nextLong());
			set.add(attributesPluginData.hashCode());
		}

		assertTrue(set.size() > 95);

	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8530033813336044717L);

		// never equals null
		for (int i = 0; i < 30; i++) {
			AttributesPluginData attributesPluginData = getRandomAttributesPluginData(randomGenerator.nextLong());
			assertFalse(attributesPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			AttributesPluginData attributesPluginData = getRandomAttributesPluginData(randomGenerator.nextLong());
			assertTrue(attributesPluginData.equals(attributesPluginData));
		}

		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributesPluginData attributesPluginData1 = getRandomAttributesPluginData(seed);
			AttributesPluginData attributesPluginData2 = getRandomAttributesPluginData(seed);
			for (int j = 0; j < 5; j++) {
				assertTrue(attributesPluginData1.equals(attributesPluginData2));
				assertTrue(attributesPluginData2.equals(attributesPluginData1));
			}
		}

		// changes to inputs cause non-equality
		Set<AttributesPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			AttributesPluginData attributesPluginData = getRandomAttributesPluginData(randomGenerator.nextLong());
			set.add(attributesPluginData);
		}

		assertTrue(set.size() > 95);

	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.class, name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8530033813336044717L);

		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			builder.defineAttribute(testAttributeId, attributeDefinition);
		}

		for (int i = 0; i < 3; i++) {
			PersonId personId = new PersonId(2 * i + 1);
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
			}
		}

		AttributesPluginData attributesPluginData = builder.build();

		// expected value validated by inspection
		String expectedValue = "AttributesPluginData [data=Data [attributeDefinitions={"
				+ "INT_0=AttributeDefinition [type=class java.lang.Integer, defaultValue=0], INT_1=AttributeDefinition "
				+ "[type=class java.lang.Integer, defaultValue=1], DOUBLE_0=AttributeDefinition "
				+ "[type=class java.lang.Double, defaultValue=0.0], DOUBLE_1=AttributeDefinition "
				+ "[type=class java.lang.Double, defaultValue=1.0], BOOLEAN_0=AttributeDefinition "
				+ "[type=class java.lang.Boolean, defaultValue=false], BOOLEAN_1=AttributeDefinition "
				+ "[type=class java.lang.Boolean, defaultValue=true]}, " + "personAttributeValues={"
				+ "INT_0=[null, 329463590, null, -1510862987, null, -1603186760], "
				+ "INT_1=[null, -1442300487, null, -2051859947, null, 1530603228], "
				+ "DOUBLE_0=[null, 0.17274128088453056, null, 0.2724502050653399, null, 0.8295695456845824], "
				+ "DOUBLE_1=[null, 0.317216253156253, null, 0.3024007880133237, null, 0.24225942651802534], "
				+ "BOOLEAN_0=[null, false, null, true, null, false], "
				+ "BOOLEAN_1=[null, true, null, true, null, true]}]]";
		String actualValue = attributesPluginData.toString();

		assertEquals(expectedValue, actualValue);

	}
}
