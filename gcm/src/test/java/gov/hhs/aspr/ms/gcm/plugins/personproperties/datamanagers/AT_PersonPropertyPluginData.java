package gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers;

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

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyError;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
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
		 * precondition test: if a person is assigned a property value for a property
		 * that was not defined
		 */
		ContractException contractException = assertThrows(ContractException.class, //
				() -> PersonPropertiesPluginData.builder()//
						.setPersonPropertyValue(new PersonId(0),
								TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true)//
						.build());//
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment time for a
		 * property that was not defined
		 */
		contractException = assertThrows(ContractException.class, //
				() -> PersonPropertiesPluginData.builder()//
						.setPersonPropertyTime(new PersonId(0),
								TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 2.3)//
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
					PersonPropertiesPluginData.builder()//
							.definePersonProperty(testPersonPropertyId, propertyDefinition, 0.0, false)//
							.setPersonPropertyValue(new PersonId(0),
									TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45)//
							.build();//
				});//
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment time, but
		 * the corresponding property is not marked for time tracking
		 */
		contractException = assertThrows(ContractException.class, //
				() -> {//

					TestPersonPropertyId prop1 = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition def1 = prop1.getPropertyDefinition();

					PersonPropertiesPluginData.builder()//
							.definePersonProperty(prop1, def1, 0.0, false)//
							.setPersonPropertyTime(new PersonId(0), prop1, 3.2)//
							.build();//
				});//
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property assignment time, but
		 * that value precedes default tracking time for the corresponding property id
		 */

		contractException = assertThrows(ContractException.class, //
				() -> {//

					TestPersonPropertyId prop1 = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition def1 = prop1.getPropertyDefinition();

					PersonPropertiesPluginData.builder()//
							.definePersonProperty(prop1, def1, 4.2, true)//
							.setPersonPropertyTime(new PersonId(0), prop1, 3.2)//
							.build();//
				});//
		assertEquals(PersonPropertyError.PROPERTY_TIME_PRECEDES_DEFAULT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "definePersonProperty", args = {
			PersonPropertyId.class, PropertyDefinition.class, double.class, boolean.class })
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
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId2.getPropertyDefinition(), time, trackTime);
			// replacing data to show that the value persists
			time = randomGenerator.nextDouble();
			trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			// adding duplicate data to show that the value persists
			time = randomGenerator.nextDouble();
			trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			expectedValues.add(
					new MultiKey(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), time, trackTime));
		}

		// build the person property plugin data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the expected
		 * definitions.
		 */
		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = personPropertiesPluginData
					.getPersonPropertyDefinition(testPersonPropertyId);
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
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), Double.NaN,
					false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(),
					Double.POSITIVE_INFINITY, false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(),
					Double.NEGATIVE_INFINITY, false);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyDefinitions", args = {}, tags = {})
	public void testGetPropertyDefinitions() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5091700094092190515L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		Map<PersonPropertyId, PropertyDefinition> expectedPropertyDefinitions = new LinkedHashMap<>();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			double time = randomGenerator.nextDouble();
			boolean trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			expectedPropertyDefinitions.put(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property plugin data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		Map<PersonPropertyId, PropertyDefinition> actualPropertyDefinitions = personPropertiesPluginData
				.getPropertyDefinitions();

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyDefinition", args = {
			PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testGetPersonPropertyDefinition() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null

		ContractException contractException = assertThrows(ContractException.class,
				() -> PersonPropertiesPluginData.builder().build().getPersonPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build()
				.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyDefinitionTimes", args = {}, tags = {
			UnitTag.LOCAL_PROXY })
	public void testGetPropertyDefinitionTimes() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5091700094092190515L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		Map<PersonPropertyId, Double> expectedPropertyDefinitionTimes = new LinkedHashMap<>();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			double time = randomGenerator.nextDouble();
			boolean trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), time, trackTime);
			expectedPropertyDefinitionTimes.put(testPersonPropertyId, time);
		}

		// build the person property plugin data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		Map<PersonPropertyId, Double> actualPropertyDefinitionTimes = personPropertiesPluginData
				.getPropertyDefinitionTimes();

		assertEquals(expectedPropertyDefinitionTimes, actualPropertyDefinitionTimes);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyDefinitionTime", args = {
			PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testGetPropertyDefinitionTime() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PersonPropertiesPluginData.builder().build().getPropertyDefinitionTime(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build()
				.getPropertyDefinitionTime(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyTrackingPolicies", args = {}, tags = {
			UnitTag.LOCAL_PROXY })
	public void testGetPropertyTrackingPolicies() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7695820040353096191L);
		Map<PersonPropertyId, Boolean> expectedPropertyTrackingPolicies = new LinkedHashMap<>();
		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			TestPersonPropertyId testPersonPropertyId2 = testPersonPropertyId.next();
			double time = randomGenerator.nextDouble();
			boolean trackTime = randomGenerator.nextBoolean();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId2.getPropertyDefinition(), time, trackTime);
			expectedPropertyTrackingPolicies.put(testPersonPropertyId, trackTime);
		}

		// build the person property plugin data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the expected
		 * definitions.
		 */

		Map<PersonPropertyId, Boolean> actualPropertyTrackingPolicies = personPropertiesPluginData
				.getPropertyTrackingPolicies();
		assertEquals(expectedPropertyTrackingPolicies, actualPropertyTrackingPolicies);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "propertyAssignmentTimesTracked", args = {
			PersonPropertyId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testPropertyAssignmentTimesTracked() {

		// the test: testDefinePersonProperty() covers all the postcondition
		// tests

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PersonPropertiesPluginData.builder().build().propertyAssignmentTimesTracked(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> PersonPropertiesPluginData.builder().build()
				.propertyAssignmentTimesTracked(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), 0, false);
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
			pluginBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(),
					randomGenerator.nextDouble(), randomGenerator.nextBoolean());
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
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "setPersonPropertyValue", args = {
			PersonId.class, PersonPropertyId.class, Object.class })
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
//	PersonPropertiesPluginData	public java.util.Map plugins.personproperties.datamanagers.PersonPropertiesPluginData.getPropertyValues()

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyValues", args = {})
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

		Map<PersonPropertyId, List<Object>> expectedPropertyValues = new LinkedHashMap<>();

		int personCount = 150;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);

			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				Object value = null;
				if (randomGenerator.nextBoolean()) {
					value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				}
				List<Object> list = expectedPropertyValues.get(testPersonPropertyId);
				if (list == null) {
					list = new ArrayList<>();
					expectedPropertyValues.put(testPersonPropertyId, list);
				}
				if (value != null) {
					int n = personId.getValue();
					while (list.size() < n) {
						list.add(null);
					}
					list.add(value);
				}
			}
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		Map<PersonPropertyId, List<Object>> actualPropertyValues = personPropertiesPluginData.getPropertyValues();

		assertEquals(expectedPropertyValues, actualPropertyValues);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyValues", args = {
			PersonPropertyId.class })
	public void testGetPropertyValues_PersonPropertyId() {
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
			PersonPropertiesPluginData.builder().build()
					.getPropertyValues(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyTimes", args = {})
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

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyTimes", args = {
			PersonPropertyId.class })
	public void testGetPropertyTimes_PersonPropertyId() {
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
			PersonPropertiesPluginData.builder().build()
					.getPropertyTimes(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "setPersonPropertyTime", args = {
			PersonId.class, PersonPropertyId.class, Double.class })
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
			PersonPropertiesPluginData.builder().setPersonPropertyTime(null,
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 2.3);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4), null, 2.3);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property time is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, null);
		});
		assertEquals(PersonPropertyError.NULL_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.NaN);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.POSITIVE_INFINITY);
		});
		assertEquals(PersonPropertyError.NON_FINITE_TIME, contractException.getErrorType());

		// precondition test: if the person property time is not finite
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.builder().setPersonPropertyTime(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, Double.NEGATIVE_INFINITY);
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
		PersonPropertiesPluginData pluginData2 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId1, def1, 2.0, true)//
				.build();
		PersonPropertiesPluginData pluginData3 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId1, def1, 2.0, true)//
				.build();
		PersonPropertiesPluginData pluginData4 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId2, def1, 0, true)//
				.build();
		PersonPropertiesPluginData pluginData5 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId2, def2, 0, true)//
				.build();
		PersonPropertiesPluginData pluginData6 = PersonPropertiesPluginData.builder()//
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

		PersonPropertiesPluginData pluginData1 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId1, def1, 2, true)//
				.definePersonProperty(propId2, def2, 3, true)//

				.setPersonPropertyValue(new PersonId(2), propId1, 5)//
				.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

				.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
				.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

				.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
				.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
				.build();

		PersonPropertiesPluginData pluginData2 = PersonPropertiesPluginData.builder()//
				.definePersonProperty(propId1, def1, 2, true)//
				.definePersonProperty(propId2, def2, 3, true)//

				.setPersonPropertyValue(new PersonId(2), propId1, 5)//
				.setPersonPropertyTime(new PersonId(2), propId1, 6.0)//

				.setPersonPropertyValue(new PersonId(5), propId2, 12.5)//
				.setPersonPropertyTime(new PersonId(5), propId2, 3.0)//

				.setPersonPropertyTime(new PersonId(8), propId2, 8.4)//
				.setPersonPropertyTime(new PersonId(8), propId2, 12.7)//
				.build();

		// equal objects have equal hash codes
		assertEquals(pluginData1, pluginData2);

		assertEquals(pluginData1.hashCode(), pluginData2.hashCode());

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
							builder.setPersonPropertyValue(new PersonId(j), testPersonPropertyId,
									testPersonPropertyId.getRandomPropertyValue(randomGenerator));
						}
					}
					if (trackTimes) {
						for (int j = 0; j < 5; j++) {
							if (randomGenerator.nextBoolean()) {
								builder.setPersonPropertyTime(new PersonId(j), testPersonPropertyId,
										baseTime + randomGenerator.nextDouble());
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

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3694836073644636049L);

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			people.add(new PersonId(2 * i + 5));
		}

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		double defTime = 0;
		boolean track = false;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), defTime,
					track);

			for (PersonId personId : people) {
				Object propertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setPersonPropertyValue(personId, testPersonPropertyId, propertyValue);
				if (track) {
					builder.setPersonPropertyTime(personId, testPersonPropertyId,
							defTime + randomGenerator.nextDouble() * 10);
				}
			}
			defTime += 10;
			track = !track;
		}

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		String actualValue = personPropertiesPluginData.toString();
		
		
		
		String expectedValue ="PersonPropertiesPluginData [data=Data [personPropertyDefinitions="
				+ "{PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Boolean,"
				+ " propertyValuesAreMutable=true, defaultValue=false], PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=PropertyDefinition"
				+ " [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=0], "
				+ "PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true,"
				+ " defaultValue=0.0], PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK=PropertyDefinition [type=class java.lang.Boolean, "
				+ "propertyValuesAreMutable=true, defaultValue=false], PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK=PropertyDefinition "
				+ "[type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=0], PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK="
				+ "PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true, defaultValue=0.0], "
				+ "PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Boolean, "
				+ "propertyValuesAreMutable=false, defaultValue=false], PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK="
				+ "PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false, defaultValue=0], "
				+ "PERSON_PROPERTY_9_DOUBLE_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Double, "
				+ "propertyValuesAreMutable=true, defaultValue=null]}, propertyDefinitionTimes={"
				+ "PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=0.0, PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=10.0, "
				+ "PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK=20.0, PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK=30.0, "
				+ "PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK=40.0, PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK=50.0, "
				+ "PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK=60.0, PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK=70.0, "
				+ "PERSON_PROPERTY_9_DOUBLE_MUTABLE_NO_TRACK=80.0}, propertyTrackingPolicies={"
				+ "PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=false, PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=true, "
				+ "PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK=false, PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK=true, "
				+ "PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK=false, PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK=true, "
				+ "PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK=false, PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK=true, "
				+ "PERSON_PROPERTY_9_DOUBLE_MUTABLE_NO_TRACK=false}, personPropertyValues={"
				+ "PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=[null, null, null, null, null, "
				+ "false, null, true, null, true, null, true, null, true, null, true, null, false, "
				+ "null, false, null, true, null, false], PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK="
				+ "[null, null, null, null, null, -621108106, null, 2100066974, null, -2018035576, null, "
				+ "1968625103, null, 1374125534, null, 1531713167, null, 1582611905, null, -1007215429, null, "
				+ "-1746683312, null, 1270680106], PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK=[null, null, "
				+ "null, null, null, 0.768935287898453, null, 0.8364646544958321, null, 0.2558395651219538, "
				+ "null, 0.5191462165704828, null, 0.07007453695242538, null, 0.01464388215554413, null, "
				+ "0.6468884427978394, null, 0.14067662402164638, null, 0.9462070884899854, null, "
				+ "0.3387682391115401], PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK=[null, null, null, "
				+ "null, null, false, null, false, null, false, null, true, null, true, null, true, "
				+ "null, true, null, true, null, true, null, false], PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK="
				+ "[null, null, null, null, null, -1568693474, null, 186939033, null, 1168801853, null, "
				+ "-2105311462, null, -1179232898, null, 1301961038, null, 1500224149, null, 1046340576, null, "
				+ "1464613321, null, 313138634], PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK=[null, null, null, null, "
				+ "null, 0.09874678269064252, null, 0.8068629284994859, null, 0.34718027838481214, null, "
				+ "0.6468842925484919, null, 0.12348080023922603, null, 0.3445025787207139, null, "
				+ "0.6441936016018901, null, 0.22605544325326998, null, 0.6149961179850165, null, "
				+ "0.4370425154054456], PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK=[null, null, null, null, "
				+ "null, true, null, true, null, true, null, false, null, false, null, true, null, false, null, "
				+ "true, null, false, null, false], PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK=[null, null, "
				+ "null, null, null, -1061266563, null, 248923101, null, -1034787615, null, -2105458344, null, "
				+ "-2053555776, null, 734233623, null, -1788427753, null, -2147042288, null, 795856471, null,"
				+ " -1107524672], PERSON_PROPERTY_9_DOUBLE_MUTABLE_NO_TRACK=[null, null, null, null, null, "
				+ "0.04473211964365187, null, 0.35677440405445027, null, 0.7094328573866935, null, 0.4262998541569638, "
				+ "null, 0.6721812305884087, null, 0.06599533517359268, null, 0.5898411326259143, null, 0.2688893818375615, "
				+ "null, 0.7498770794369971, null, 0.3698063555641413]}, personPropertyTimes={"
				+ "PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=[null, null, null, null, null, "
				+ "15.01053068281431, null, 19.94981623136485, null, 14.153192884863667, null, 12.6440676490902, "
				+ "null, 15.598933330056575, null, 13.252151935445784, null, 16.63154860967876, null, "
				+ "10.493942026810101, null, 10.443786590357679, null, 18.660720681068277], "
				+ "PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK=[null, null, null, null, null, 32.79706714473025, "
				+ "null, 30.28243209635568, null, 39.401217348796465, null, 35.97150480992329, null, 36.26522497015469, "
				+ "null, 33.879624529240154, null, 36.925982440804454, null, 32.807367349614246, null, 32.398150127097296, "
				+ "null, 39.290644182925476], PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK=[null, null, null, null, null, "
				+ "55.08342661900296, null, 57.745554002438034, null, 56.506973180579166, null, 54.30056662223466, "
				+ "null, 59.04310482253601, null, 52.95886544676756, null, 54.049056592024826, null, 54.30100804888913, "
				+ "null, 52.27313116665432, null, 52.72849938412798], PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK=["
				+ "null, null, null, null, null, 79.52059465811621, null, 72.35392133239016, null, 75.13846509635269, "
				+ "null, 75.4774777727181, null, 77.77386975625731, null, 76.46205411237824, null, 70.54554388234095, "
				+ "null, 72.92650170630496, null, 78.75239401434217, null, 77.34177298897009]}]]";
		
		
		assertEquals(expectedValue, actualValue);

	}
}
