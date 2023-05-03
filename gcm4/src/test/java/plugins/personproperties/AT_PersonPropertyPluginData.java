package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AT_PersonPropertyPluginData {

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PersonPropertiesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		TestPersonPropertyId propertyId = TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK;
		PropertyDefinition def = propertyId.getPropertyDefinition();
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		// build a plugin data
		PersonPropertiesPluginData pluginData1 = builder//
														.definePersonProperty(propertyId, def, 1.2, true)//
														.setPersonPropertyValue(new PersonId(5), propertyId, 7)//
														.setPersonPropertyTime(new PersonId(5), propertyId, 2.5)//
														.build();

		// show that it was not null
		assertNotNull(pluginData1);

		// show that the builder returns an identical plugin data if build is
		// invoked again
		PersonPropertiesPluginData pluginData2 = builder.build();
		assertEquals(pluginData1, pluginData2);

		/*
		 * precondition test: if a person is assigned a property value for a
		 * property that was not defined
		 */
		ContractException contractException = assertThrows(ContractException.class, //
				() -> PersonPropertiesPluginData.builder()//
												.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true)//
												.build());//
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment time
		 * for a property that was not defined
		 */
		contractException = assertThrows(ContractException.class, //
				() -> PersonPropertiesPluginData.builder()//
												.setPersonPropertyTime(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 2.3)//
												.build());//
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property value that is
		 * incompatible with the associated property definition
		 */
		contractException = assertThrows(ContractException.class, //
				() -> {//
					TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
					PersonPropertiesPluginData	.builder()//
												.definePersonProperty(testPersonPropertyId, propertyDefinition, 0.0, false)//
												.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45)//
												.build();//
				});//
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment
		 * time, but the corresponding property is not marked for time tracking
		 */
		contractException = assertThrows(ContractException.class, //
				() -> {//

					TestPersonPropertyId prop1 = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition def1 = prop1.getPropertyDefinition();

					PersonPropertiesPluginData	.builder()//
												.definePersonProperty(prop1, def1, 0.0, false)//
												.setPersonPropertyTime(new PersonId(0), prop1, 3.2)//
												.build();//
				});//
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment
		 * time, but that value precedes default tracking time for the
		 * corresponding property id
		 */

		contractException = assertThrows(ContractException.class, //
				() -> {//

					TestPersonPropertyId prop1 = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition def1 = prop1.getPropertyDefinition();

					PersonPropertiesPluginData	.builder()//
												.definePersonProperty(prop1, def1, 4.2, true)//
												.setPersonPropertyTime(new PersonId(0), prop1, 3.2)//
												.build();//
				});//
		assertEquals(PersonPropertyError.PROPERTY_TIME_PRECEDES_DEFAULT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7695820040353096191L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		Set<MultiKey> expectedValues = new LinkedHashSet<>();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			TestPersonPropertyId testPersonPropertyId2 = testPersonPropertyId.next();
			double time = randomGenerator.nextDouble();
			boolean trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId2.getPropertyDefinition(), time, trackTime);
			// replacing data to show that the value persists
			time = randomGenerator.nextDouble();
			trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			// adding duplicate data to show that the value persists
			time = randomGenerator.nextDouble();
			trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			expectedValues.add(new MultiKey(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), time, trackTime));
		}

		// build the person property plugin data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the
		 * expected definitions.
		 */
		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(testPersonPropertyId);
			double time = personPropertiesPluginData.getPropertyDefinitionTime(testPersonPropertyId);
			boolean tracked = personPropertiesPluginData.propertyAssignmentTimesTracked(testPersonPropertyId);
			actualValues.add(new MultiKey(testPersonPropertyId, propertyDefinition, time, tracked));
		}

		assertEquals(expectedValues, actualValues);
		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(null, testPersonPropertyId.getPropertyDefinition(), 0, false);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property definition value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, null, 0, false);
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the person property definition value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), Double.NaN, false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), Double.POSITIVE_INFINITY, false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), Double.NEGATIVE_INFINITY, false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyDefinition", args = { PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testGetPersonPropertyDefinition() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null

		ContractException contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().getPersonPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}
	
	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyDefinitionTime", args = { PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testGetPropertyDefinitionTime() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().getPropertyDefinitionTime(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().getPropertyDefinitionTime(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}
	
	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "propertyAssignmentTimesTracked", args = { PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testPropertyAssignmentTimesTracked() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().propertyAssignmentTimesTracked(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build().propertyAssignmentTimesTracked(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}
	

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), 0, false);
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the person proproperty ids match expectations
		 */
		assertEquals(EnumSet.allOf(TestPersonPropertyId.class), personPropertyInitialData.getPersonPropertyIds());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3666595741799189966L);
		PersonPropertiesPluginData.Builder pluginBuilder = PersonPropertiesPluginData.builder();

		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			pluginBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), randomGenerator.nextDouble(), randomGenerator.nextBoolean());
		}
		int personCount = 100;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean hasDefault = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
				if (!hasDefault || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					pluginBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}

		PersonPropertiesPluginData expectedPluginData = pluginBuilder.build();

		PluginDataBuilder cloneBuilder = expectedPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		PluginData actualPluginData = cloneBuilder.build();

		// show that the two plugin datas are equal
		assertEquals(expectedPluginData, actualPluginData);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6340277988168121078L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		List<TestPersonPropertyId> propertiesWithoutDefaultValues = new ArrayList<>();
		List<TestPersonPropertyId> propertiesWithDefaultValues = new ArrayList<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			if (propertyDefinition.getDefaultValue().isPresent()) {
				propertiesWithDefaultValues.add(testPersonPropertyId);
			} else {
				propertiesWithoutDefaultValues.add(testPersonPropertyId);
			}
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition, 0, false);
		}

		Set<MultiKey> expectedPersonPropertyValues = new LinkedHashSet<>();

		int personCount = 150;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			Set<TestPersonPropertyId> propertiesToUse = new LinkedHashSet<>(propertiesWithoutDefaultValues);
			for (TestPersonPropertyId testPersonPropertyId : propertiesWithDefaultValues) {
				if (randomGenerator.nextBoolean()) {
					propertiesToUse.add(testPersonPropertyId);
				}
			}
			for (TestPersonPropertyId testPersonPropertyId : propertiesToUse) {
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				Object value2 = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				if (value instanceof Boolean) {
					value2 = !(Boolean) value;
				}
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value2);
				// replacing data to show that the value persists
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				// adding duplicate data to show that the value persists
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);

				expectedPersonPropertyValues.add(new MultiKey(personId, testPersonPropertyId, value));
			}

		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that property values match expectations
		 */
		Set<MultiKey> acutalPersonPropertyValues = new LinkedHashSet<>();

		for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
			List<Object> propertyValues = personPropertiesPluginData.getPropertyValues(personPropertyId);
			for (int i = 0; i < propertyValues.size(); i++) {
				Object value = propertyValues.get(i);
				if (value != null) {
					acutalPersonPropertyValues.add(new MultiKey(new PersonId(i), personPropertyId, value));
				}
			}
		}
		assertEquals(expectedPersonPropertyValues, acutalPersonPropertyValues);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.setPersonPropertyValue(null, testPersonPropertyId, true);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person property value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		// precondition test: if the person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			builder.setPersonPropertyValue(new PersonId(0), null, true);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyValues", args = { int.class })
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6340277988168121078L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		List<TestPersonPropertyId> propertiesWithoutDefaultValues = new ArrayList<>();
		List<TestPersonPropertyId> propertiesWithDefaultValues = new ArrayList<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			if (propertyDefinition.getDefaultValue().isPresent()) {
				propertiesWithDefaultValues.add(testPersonPropertyId);
			} else {
				propertiesWithoutDefaultValues.add(testPersonPropertyId);
			}
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition, 0, false);
		}

		Set<MultiKey> expectedPersonPropertyValues = new LinkedHashSet<>();

		int personCount = 150;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			Set<TestPersonPropertyId> propertiesToUse = new LinkedHashSet<>(propertiesWithoutDefaultValues);
			for (TestPersonPropertyId testPersonPropertyId : propertiesWithDefaultValues) {
				if (randomGenerator.nextBoolean()) {
					propertiesToUse.add(testPersonPropertyId);
				}
			}
			for (TestPersonPropertyId testPersonPropertyId : propertiesToUse) {
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				Object value2 = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				if (value instanceof Boolean) {
					value2 = !(Boolean) value;
				}
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value2);
				// replacing data to show that the value persists
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				// adding duplicate data to show that the value persists
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);

				expectedPersonPropertyValues.add(new MultiKey(personId, testPersonPropertyId, value));
			}

		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that property values match expectations
		 */
		Set<MultiKey> acutalPersonPropertyValues = new LinkedHashSet<>();

		for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
			List<Object> propertyValues = personPropertiesPluginData.getPropertyValues(personPropertyId);
			for (int i = 0; i < propertyValues.size(); i++) {
				Object value = propertyValues.get(i);
				if (value != null) {
					acutalPersonPropertyValues.add(new MultiKey(new PersonId(i), personPropertyId, value));
				}
			}
		}
		assertEquals(expectedPersonPropertyValues, acutalPersonPropertyValues);

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().build().getPropertyValues(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().build().getPropertyValues(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyTimes", args = { PersonPropertyId.class })
	public void testGetPropertyTimes() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7969263718268675163L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		Map<TestPersonPropertyId, Double> baseTimes = new LinkedHashMap<>();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			double time = randomGenerator.nextInt() * 10;
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition, time, true);
			baseTimes.put(testPersonPropertyId, time);
		}

		Set<MultiKey> expectedPersonPropertyTimes = new LinkedHashSet<>();

		int personCount = 150;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				Double time = randomGenerator.nextDouble() + baseTimes.get(testPersonPropertyId);
				Double time2 = randomGenerator.nextDouble() + baseTimes.get(testPersonPropertyId);
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time2);
				// replacing data to show that the value persists
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time);
				// adding duplicate data to show that the value persists
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time);

				expectedPersonPropertyTimes.add(new MultiKey(personId, testPersonPropertyId, time));
			}

		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that property times match expectations
		 */
		Set<MultiKey> acutalPersonPropertyTimes = new LinkedHashSet<>();

		for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
			List<Double> propertyTimes = personPropertiesPluginData.getPropertyTimes(personPropertyId);
			for (int i = 0; i < propertyTimes.size(); i++) {
				Double time = propertyTimes.get(i);
				if (time != null) {
					acutalPersonPropertyTimes.add(new MultiKey(new PersonId(i), personPropertyId, time));
				}
			}
		}
		assertEquals(expectedPersonPropertyTimes, acutalPersonPropertyTimes);

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().build().getPropertyTimes(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().build().getPropertyTimes(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "setPersonPropertyTime", args = { PersonId.class, PersonPropertyId.class, Double.class })
	public void testSetPersonPropertyTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7969263718268675163L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		Map<TestPersonPropertyId, Double> baseTimes = new LinkedHashMap<>();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			double time = randomGenerator.nextInt() * 10;
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition, time, true);
			baseTimes.put(testPersonPropertyId, time);
		}

		Set<MultiKey> expectedPersonPropertyTimes = new LinkedHashSet<>();

		int personCount = 150;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				Double time = randomGenerator.nextDouble() + baseTimes.get(testPersonPropertyId);
				Double time2 = randomGenerator.nextDouble() + baseTimes.get(testPersonPropertyId);
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time2);
				// replacing data to show that the value persists
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time);
				// adding duplicate data to show that the value persists
				personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, time);

				expectedPersonPropertyTimes.add(new MultiKey(personId, testPersonPropertyId, time));
			}

		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that property times match expectations
		 */
		Set<MultiKey> acutalPersonPropertyTimes = new LinkedHashSet<>();

		for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
			List<Double> propertyTimes = personPropertiesPluginData.getPropertyTimes(personPropertyId);
			for (int i = 0; i < propertyTimes.size(); i++) {
				Double time = propertyTimes.get(i);
				if (time != null) {
					acutalPersonPropertyTimes.add(new MultiKey(new PersonId(i), personPropertyId, time));
				}
			}
		}
		assertEquals(expectedPersonPropertyTimes, acutalPersonPropertyTimes);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(null, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 2.3);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), null, 2.3);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property time is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, null);
		});
		assertEquals(PersonPropertyError.NULL_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.NaN);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.POSITIVE_INFINITY);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.NEGATIVE_INFINITY);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// equality on property definitions
		PersonPropertyId propId1 = new PersonPropertyId() {
		};
		PropertyDefinition def1 = PropertyDefinition.builder()//
													.setType(Integer.class)//
													.setDefaultValue(5)//
													.setPropertyValueMutability(true)//
													.build();

		PersonPropertyId propId2 = new PersonPropertyId() {
		};
		PropertyDefinition def2 = PropertyDefinition.builder()//
													.setType(Double.class)//
													.setDefaultValue(3.0)//
													.setPropertyValueMutability(true)//
													.build();

		PersonPropertiesPluginData pluginData1 = PersonPropertiesPluginData.builder().build();
		PersonPropertiesPluginData pluginData2 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 2.0, true)//
																			.build();
		PersonPropertiesPluginData pluginData3 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 2.0, true)//
																			.build();
		PersonPropertiesPluginData pluginData4 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId2, def1, 0, true)//
																			.build();
		PersonPropertiesPluginData pluginData5 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId2, def2, 0, true)//
																			.build();
		PersonPropertiesPluginData pluginData6 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 0, true)//
																			.definePersonProperty(propId2, def2, 0, true)//
																			.build();

		// reflexive
		assertEquals(pluginData1, pluginData1);
		assertEquals(pluginData2, pluginData2);
		// symmetric
		assertEquals(pluginData2, pluginData3);
		assertEquals(pluginData3, pluginData2);

		// transitive implicitly covered

		// not equals
		assertNotEquals(pluginData1, pluginData2);
		assertNotEquals(pluginData2, pluginData4);
		assertNotEquals(pluginData4, pluginData5);
		assertNotEquals(pluginData2, pluginData5);
		assertNotEquals(pluginData5, pluginData6);

		// equality on time tracking
		pluginData1 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2.3, true)//
												.definePersonProperty(propId2, def2, 0, false)//
												.build();

		// same as 1
		pluginData2 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2.3, true)//
												.definePersonProperty(propId2, def2, 0, false)//
												.build();

		// use a different property id
		pluginData3 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 0, false)//
												.definePersonProperty(propId2, def2, 2.3, true)//
												.build();

		// changed the default time
		pluginData4 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 5.3, true)//
												.definePersonProperty(propId2, def2, 0, false)//
												.build();

		// now has two default times
		pluginData5 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2.3, true)//
												.definePersonProperty(propId2, def2, 4.7, true)//
												.build();

		assertEquals(pluginData1, pluginData1);
		assertEquals(pluginData1, pluginData2);
		assertNotEquals(pluginData1, pluginData3);
		assertNotEquals(pluginData1, pluginData4);
		assertNotEquals(pluginData1, pluginData5);

		// equality on person property values
		pluginData1 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 6, true)//
												.definePersonProperty(propId2, def2, 5.4, true)//
												.build();

		pluginData2 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 6, true)//
												.definePersonProperty(propId2, def2, 5.4, true)//
												.build();

		// change a person
		pluginData3 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 6, true)//
												.definePersonProperty(propId2, def2, 5.4, false)//
												.build();

		// change a value
		pluginData4 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 6, true)//
												.definePersonProperty(propId2, def2, 5.5, true)//
												.build();

		// add a person
		pluginData5 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 0, false)//
												.definePersonProperty(propId2, def2, 0, false)//
												.setPersonPropertyValue(new PersonId(2), propId1, 6)//
												.setPersonPropertyValue(new PersonId(5), propId2, 5.4)//
												.setPersonPropertyValue(new PersonId(8), propId2, 8.4)//
												.build();

		assertEquals(pluginData1, pluginData1);
		assertEquals(pluginData1, pluginData2);
		assertNotEquals(pluginData1, pluginData3);
		assertNotEquals(pluginData1, pluginData4);
		assertNotEquals(pluginData1, pluginData5);

		// equality on person property times
		pluginData1 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//
												.setPersonPropertyTime(new PersonId(5), propId2, 5.4)//
												.build();

		pluginData2 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//
												.setPersonPropertyTime(new PersonId(5), propId2, 5.4)//
												.build();

		// change a person
		pluginData3 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//
												.setPersonPropertyTime(new PersonId(1), propId1, 6.0)//
												.setPersonPropertyTime(new PersonId(5), propId2, 5.4)//
												.build();

		// change a time
		pluginData4 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//
												.setPersonPropertyTime(new PersonId(5), propId2, 5.5)//
												.build();

		// add a person
		pluginData5 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//
												.setPersonPropertyTime(new PersonId(5), propId2, 5.5)//
												.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
												.build();

		assertEquals(pluginData1, pluginData1);
		assertEquals(pluginData1, pluginData2);
		assertNotEquals(pluginData1, pluginData3);
		assertNotEquals(pluginData1, pluginData4);
		assertNotEquals(pluginData1, pluginData5);

		/*
		 * Some examples of equals() being true when inputs are not identical --
		 * note that both property definitions in these examples contain default
		 * values of 5 and 3.0
		 */
		pluginData1 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//

												.setPersonPropertyValue(new PersonId(2), propId1, 5)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

												.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
												.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

												.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
												.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
												.build();

		// we eliminate the value of person 2 since it is the default
		pluginData2 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//

												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

												.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
												.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

												.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
												.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
												.build();

		// we eliminate the property time for person 5 since it has the default
		// time
		pluginData3 = PersonPropertiesPluginData.builder()//
												.definePersonProperty(propId1, def1, 2, true)//
												.definePersonProperty(propId2, def2, 3, true)//

												.setPersonPropertyValue(new PersonId(2), propId1, 5)//
												.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

												.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
												// .setPersonPropertyTime(new
												// PersonId(5), propId2, 3.0)//

												.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
												.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
												.build();

		assertEquals(pluginData1, pluginData2);
		assertEquals(pluginData1, pluginData3);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {

		// some setup first
		PersonPropertyId propId1 = new PersonPropertyId() {
		};
		PropertyDefinition def1 = PropertyDefinition.builder()//
													.setType(Integer.class)//
													.setDefaultValue(5)//
													.setPropertyValueMutability(true)//
													.build();

		PersonPropertyId propId2 = new PersonPropertyId() {
		};
		PropertyDefinition def2 = PropertyDefinition.builder()//
													.setType(Double.class)//
													.setDefaultValue(3.0)//
													.setPropertyValueMutability(true)//
													.build();

		PersonPropertiesPluginData pluginData1 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 2, true)//
																			.definePersonProperty(propId2, def2, 3, true)//

																			.setPersonPropertyValue(new PersonId(2), propId1, 5)//
																			.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

																			.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
																			.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

																			.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
																			.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
																			.build();

		// we eliminate the value of person 2 since it is the default
		PersonPropertiesPluginData pluginData2 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 2, true)//
																			.definePersonProperty(propId2, def2, 3, true)//

																			.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

																			.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
																			.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

																			.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
																			.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
																			.build();

		// we eliminate the property time for person 5 since it has the default
		// time
		PersonPropertiesPluginData pluginData3 = PersonPropertiesPluginData	.builder()//
																			.definePersonProperty(propId1, def1, 2, true)//
																			.definePersonProperty(propId2, def2, 3, true)//

																			.setPersonPropertyValue(new PersonId(2), propId1, 5)//
																			.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

																			.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//

																			.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
																			.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
																			.build();

		// equal objects have equal hash codes
		assertEquals(pluginData1, pluginData2);
		assertEquals(pluginData1, pluginData3);

		assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		assertEquals(pluginData1.hashCode(), pluginData3.hashCode());

		// show that hash codes are reasonably distributed
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3839519625960869013L);

		int n = 100;
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < n; i++) {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					boolean trackTimes = randomGenerator.nextBoolean();
					double baseTime = randomGenerator.nextDouble();
					PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
					builder.definePersonProperty(testPersonPropertyId, propertyDefinition, baseTime, trackTimes);//
					for (int j = 0; j < 5; j++) {
						if (randomGenerator.nextBoolean()) {
							builder.setPersonPropertyValue(new PersonId(j), testPersonPropertyId, testPersonPropertyId.getRandomPropertyValue(randomGenerator));
						}
					}
					if (trackTimes) {
						for (int j = 0; j < 5; j++) {
							if (randomGenerator.nextBoolean()) {
								builder.setPersonPropertyTime(new PersonId(j), testPersonPropertyId, baseTime + randomGenerator.nextDouble());
							}
						}
					}
				}
			}
			PersonPropertiesPluginData personPropertiesPluginData = builder.build();
			hashCodes.add(personPropertiesPluginData.hashCode());
		}
		int expectedCount = (9 * n) / 10;
		assertTrue(hashCodes.size() > expectedCount);
	}

}
