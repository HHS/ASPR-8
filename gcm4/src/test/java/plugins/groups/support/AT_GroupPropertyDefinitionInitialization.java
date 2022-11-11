package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = GroupPropertyDefinitionInitialization.class)
public class AT_GroupPropertyDefinitionInitialization {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// Show that builder doesn't return null
		assertNotNull(GroupPropertyDefinitionInitialization.builder());
	}

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6987473140772497019L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeId", args = {})
	public void testGetGroupTypeId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5813541400218786441L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(groupTypeId, definitionInitialization.getGroupTypeId());
	}

	@Test
	@UnitTestMethod(name = "getPropertyId", args = {})
	public void testGetPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1367511048381780862L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(groupPropertyId, definitionInitialization.getPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5838196849857214331L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		GroupPropertyDefinitionInitialization.Builder definitionInitializationBuilder = GroupPropertyDefinitionInitialization
				.builder();
		List<Pair<GroupId, String>> expectedListOfPropertyValues = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			GroupPropertyId groupPropertyId = TestGroupPropertyId.values()[randomGenerator
					.nextInt(TestGroupPropertyId.values().length)];

			definitionInitializationBuilder
					.setGroupTypeId(groupTypeId)
					.setPropertyDefinition(propertyDefinition)
					.setPropertyId(groupPropertyId);

			GroupId groupId = new GroupId(10000 + i);
			for (int j = 0; j < 3; j++) {
				String value = Integer.toString(randomGenerator.nextInt(100));
				Pair<GroupId, String> propertyValue = new Pair<GroupId, String>(groupId, value);
				expectedListOfPropertyValues.add(propertyValue);
				definitionInitializationBuilder.addPropertyValue(groupId, value);
			}

			GroupPropertyDefinitionInitialization definitionInitialization = definitionInitializationBuilder.build();
			assertNotNull(definitionInitialization);

			assertEquals(expectedListOfPropertyValues,
					definitionInitialization.getPropertyValues());
			expectedListOfPropertyValues.clear();
		}

	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6019590927036411078L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		GroupId groupId = new GroupId(0);

		GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		// precondition: null property definition
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder
						.setGroupTypeId(groupTypeId)
						.setPropertyId(groupPropertyId)
						.build());
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition: incompatible property definition value
		contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(propertyDefinition)
						.setPropertyId(groupPropertyId)
						.setGroupTypeId(groupTypeId)
						.addPropertyValue(groupId, 1)
						.build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		// precondition: null groupTypeId
		contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(propertyDefinition)
						.setPropertyId(groupPropertyId)
						.build());
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition: null propertyId
		contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(propertyDefinition)
						.setGroupTypeId(groupTypeId)
						.build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = {
			PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(853904864534105353L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

		// precondition: null property definition
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(null)
						.setGroupTypeId(groupTypeId)
						.setPropertyId(groupPropertyId)
						.build());
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setGroupTypeId", args = {
			GroupTypeId.class })
	public void testSetGroupTypeId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1959708886343213469L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(groupTypeId, definitionInitialization.getGroupTypeId());

		// precondition: null group type id
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(propertyDefinition)
						.setGroupTypeId(null)
						.setPropertyId(groupPropertyId)
						.build());
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setPropertyId", args = {
			GroupPropertyId.class })
	public void testSetPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7319624484285657037L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

		GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(groupTypeId)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(groupPropertyId)
				.build();

		assertNotNull(definitionInitialization);

		assertEquals(groupPropertyId, definitionInitialization.getPropertyId());

		// precondition: null property id
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder
						.setPropertyDefinition(propertyDefinition)
						.setGroupTypeId(groupTypeId)
						.setPropertyId(null)
						.build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = {
			GroupId.class, Object.class })
	public void testAddPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2210384535662631024L);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()
				.setDefaultValue(Integer.toString(randomGenerator.nextInt(100)))
				.setType(String.class)
				.build();

		List<Pair<GroupId, String>> expectedListOfPropertyValues = new ArrayList<>();
		GroupPropertyDefinitionInitialization.Builder definitionInitializationBuilder = GroupPropertyDefinitionInitialization
				.builder();

		for (int i = 0; i < 10; i++) {
			GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			GroupPropertyId groupPropertyId = TestGroupPropertyId.values()[randomGenerator
					.nextInt(TestGroupPropertyId.values().length)];

			definitionInitializationBuilder
					.setGroupTypeId(groupTypeId)
					.setPropertyDefinition(propertyDefinition)
					.setPropertyId(groupPropertyId);

			GroupId groupId = new GroupId(10000 + i);
			for (int j = 0; j < 3; j++) {
				String value = Integer.toString(randomGenerator.nextInt(100));
				Pair<GroupId, String> propertyValue = new Pair<GroupId, String>(groupId, value);
				expectedListOfPropertyValues.add(propertyValue);
				definitionInitializationBuilder.addPropertyValue(groupId, value);
			}

			GroupPropertyDefinitionInitialization definitionInitialization = definitionInitializationBuilder.build();
			assertNotNull(definitionInitialization);

			assertNotNull(definitionInitialization.getPropertyValues());
			assertFalse(definitionInitialization.getPropertyValues().isEmpty());
			assertEquals(expectedListOfPropertyValues, definitionInitialization.getPropertyValues());

			List<Pair<GroupId, Object>> actualListOfPropertyValues = definitionInitialization.getPropertyValues();
			for (int k = 0; k < actualListOfPropertyValues.size(); k++) {
				assertEquals(expectedListOfPropertyValues.get(k), actualListOfPropertyValues.get(k));
				assertEquals(String.class, actualListOfPropertyValues.get(k).getSecond().getClass());
				assertEquals(actualListOfPropertyValues.get(k).getSecond(),
						actualListOfPropertyValues.get(k).getSecond());
				assertEquals(groupId, actualListOfPropertyValues.get(k).getFirst());
			}
		}

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		// precondition: null group id
		ContractException contractException = assertThrows(ContractException.class,
				() -> definitionInitializationBuilder
						.setPropertyDefinition(propertyDefinition)
						.setGroupTypeId(groupTypeId)
						.setPropertyId(groupPropertyId)
						.addPropertyValue(null, Integer.toString(randomGenerator.nextInt(100)))
						.build());
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition: null property value
		contractException = assertThrows(ContractException.class,
				() -> definitionInitializationBuilder
						.setPropertyDefinition(propertyDefinition)
						.setGroupTypeId(groupTypeId)
						.setPropertyId(groupPropertyId)
						.addPropertyValue(new GroupId(15000), null)
						.build());
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}
}
