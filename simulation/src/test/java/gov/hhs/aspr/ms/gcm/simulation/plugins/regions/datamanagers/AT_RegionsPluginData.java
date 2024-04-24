package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

/**
 * Test unit for {@linkplain RegionsPluginData}. Tests for
 * RegionPluginData.Builder are limited to precondition tests and are otherwise
 * covered via the class level tests. Note that the builder does not impose any
 * ordering on the invocation of its methods and many validation tests are
 * deferred to the build invocation.
 * 
 *
 */
public class AT_RegionsPluginData {

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		// show that we can create a builder
		assertNotNull(RegionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		// use the test region ids
		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedRegionIds.add(testRegionId);
		}

		// show that the regions added are present
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
		}
		RegionsPluginData regionsPluginData = builder.build();

		Set<RegionId> actualRegionIds = regionsPluginData.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyDefinition", args = {
			RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		/*
		 * Place the various properties defined in TestRegionPropertyId into the builder
		 * and associate them with distinct property definitions. Each property
		 * definition will differ by its initial value.
		 */
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);

		}

		int defaultValue = 0;
		Map<RegionPropertyId, PropertyDefinition> expectedDefinitions = new LinkedHashMap<>();
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(defaultValue++).build();
			expectedDefinitions.put(testRegionPropertyId, propertyDefinition);
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property definitions in the region initial data and place
		 * them in a map for comparison.
		 */
		Map<RegionPropertyId, PropertyDefinition> actualDefinitions = new LinkedHashMap<>();

		Set<RegionPropertyId> regionPropertyIds = regionsPluginData.getRegionPropertyIds();
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			PropertyDefinition propertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
			actualDefinitions.put(regionPropertyId, propertyDefinition);
		}

		// show that the two maps are equal
		assertEquals(expectedDefinitions, actualDefinitions);

		// precondition tests

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> regionsPluginData.getRegionPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> regionsPluginData.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		/*
		 * Place the various region/property pairs defined in TestRegionId into the
		 * builder and associate them with distinct property definitions.
		 */

		Set<RegionPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);

		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
			expectedPropertyIds.add(testRegionPropertyId);
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property defintions in the region inital data and place
		 * them in a map for comparison.
		 */
		Set<RegionPropertyId> actualPropertyIds = regionsPluginData.getRegionPropertyIds();

		// show that the two sets are equal
		assertEquals(expectedPropertyIds, actualPropertyIds);

		// no precondition tests

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyValues", args = {})
	public void testGetRegionPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1348577218631439602L);
		Map<RegionId, Map<RegionPropertyId, Object>> expectedValues = new LinkedHashMap<>();
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		/*
		 * set about half of the properties for each region for those properties that
		 * have a default value
		 */
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithDefaultValues()) {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				if (randomGenerator.nextBoolean()) {
					Object value = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, value);
					Map<RegionPropertyId, Object> map = expectedValues.get(testRegionId);
					if (map == null) {
						map = new LinkedHashMap<>();
						expectedValues.put(testRegionId, map);
					}
					map.put(testRegionPropertyId, value);

				}
			}
		}

		/*
		 * set all of the properties for each region for those properties that do not
		 * have a default value
		 */
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Object value = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, value);
				Map<RegionPropertyId, Object> map = expectedValues.get(testRegionId);
				if (map == null) {
					map = new LinkedHashMap<>();
					expectedValues.put(testRegionId, map);
				}
				map.put(testRegionPropertyId, value);

			}
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		Map<RegionId, Map<RegionPropertyId, Object>> actualValues = regionsPluginData.getRegionPropertyValues();
		assertEquals(expectedValues, actualValues);

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyValues", args = { RegionId.class })
	public void testGetRegionPropertyValues_RegionId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(887285678478260177L);

		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		Map<RegionId, Map<RegionPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedPropertyValues.put(testRegionId, new LinkedHashMap<>());
		}

		/*
		 * set about half of the properties for each region for those properties that
		 * have a default value
		 */
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithDefaultValues()) {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				if (randomGenerator.nextBoolean()) {
					Object value = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, value);
					expectedPropertyValues.get(testRegionId).put(testRegionPropertyId, value);
				}
			}
		}

		/*
		 * set all of the properties for each region for those properties that do not
		 * have a default value
		 */
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Object value = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, value);
				expectedPropertyValues.get(testRegionId).put(testRegionPropertyId, value);
			}
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property values in the region inital data and place them
		 * in a map for comparison.
		 */
		for (RegionId regionId : regionsPluginData.getRegionIds()) {
			Map<RegionPropertyId, Object> expectedMap = expectedPropertyValues.get(regionId);
			Map<RegionPropertyId, Object> actualMap = regionsPluginData.getRegionPropertyValues(regionId);
			// show that the two maps are equal
			assertEquals(expectedMap, actualMap);
		}

		// precondition test: if the region id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> regionsPluginData.getRegionPropertyValues(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the region id is unknown
		contractException = assertThrows(ContractException.class,
				() -> regionsPluginData.getRegionPropertyValues(TestRegionId.getUnknownRegionId()));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {

		RegionsPluginData regionsPluginData = RegionsPluginData.builder()//
				.setPersonRegionArrivalTracking(true)//
				.build();//
		assertEquals(true, regionsPluginData.getPersonRegionArrivalTrackingPolicy());

		regionsPluginData = RegionsPluginData.builder()//
				.setPersonRegionArrivalTracking(false)//
				.build();//
		assertEquals(false, regionsPluginData.getPersonRegionArrivalTrackingPolicy());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		// show the builder does not return null
		assertNotNull(RegionsPluginData.builder().build());

		// precondition tests

		/*
		 * if a region property value was associated with a region id that was not
		 * properly added with an initial agent behavior.
		 */
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class)
				.build();
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData.builder().defineRegionProperty(regionPropertyId, propertyDefinition)//
					.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5)//
					.build();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region property id that was
		 * not defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData.builder()//
					.addRegion(TestRegionId.REGION_1)//
					.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5)//
					.build();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region and region property
		 * id that is incompatible with the corresponding property definition.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData.builder()//
					.addRegion(TestRegionId.REGION_1)//
					.defineRegionProperty(regionPropertyId, propertyDefinition)//
					.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, "invalid value")//
					.build();//
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a region property definition does not have a default value and there are
		 * no property values added to replace that default.
		 */

		contractException = assertThrows(ContractException.class, () -> {
			PropertyDefinition def = PropertyDefinition.builder().setType(Double.class).build();

			RegionsPluginData.builder()//
					.addRegion(TestRegionId.REGION_1)//
					.defineRegionProperty(regionPropertyId, def)//
					.build();//
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * if a person region arrival data was collected, but the policy for region
		 * arrival tracking is false
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData.builder()//
					.addRegion(TestRegionId.REGION_1)//
					.setPersonRegionArrivalTracking(false)//
					.addPerson(new PersonId(0), TestRegionId.REGION_1, 0.0)//
					.build();//
		});
		assertEquals(RegionError.PERSON_ARRIVAL_DATA_PRESENT, contractException.getErrorType());

		/*
		 * if the policy for region arrival tracking is set to true, but region arrival
		 * times are missing
		 */

		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData.builder()//
					.addRegion(TestRegionId.REGION_1)//
					.setPersonRegionArrivalTracking(true)//
					.addPerson(new PersonId(0), TestRegionId.REGION_1)//
					.build();
		});
		assertEquals(RegionError.MISSING_PERSON_ARRIVAL_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "defineRegionProperty", args = {
			RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(9).setType(Integer.class)
				.build();

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.defineRegionProperty(null, propertyDefinition));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class,
				() -> builder.defineRegionProperty(regionPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addRegion", args = { RegionId.class })
	public void testAddRegion() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addRegion(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getPersonRegionArrivalTime", args = { PersonId.class })
	public void testGetPersonRegionArrivalTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8722606929396924838L);

		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		builder.setPersonRegionArrivalTracking(true);
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
		}

		Map<PersonId, Double> expectedRegionArrivalTimes = new LinkedHashMap<>();
		int maxId = Integer.MIN_VALUE;
		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(3 * i + 5);
			maxId = FastMath.max(maxId, personId.getValue());
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			double time = randomGenerator.nextDouble();
			builder.addPerson(personId, regionId, time);
			expectedRegionArrivalTimes.put(personId, time);
		}
		maxId++;

		RegionsPluginData regionsPluginData = builder.build();

		for (int i = 0; i < maxId; i++) {
			PersonId personId = new PersonId(i);
			Optional<Double> optional = regionsPluginData.getPersonRegionArrivalTime(personId);
			Double expectedTime = expectedRegionArrivalTimes.get(personId);
			if (expectedTime != null) {
				assertTrue(optional.isPresent());
				assertEquals(expectedTime, optional.get());
			} else {
				assertTrue(optional.isEmpty());
			}
		}

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionsPluginData.builder().build().getPersonRegionArrivalTime(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getPersonRegion", args = { PersonId.class })
	public void testGetPersonRegion() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8722606929396924838L);

		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
		}

		Map<PersonId, RegionId> expectedRegionAssignments = new LinkedHashMap<>();
		int maxId = Integer.MIN_VALUE;
		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(3 * i + 5);
			maxId = FastMath.max(maxId, personId.getValue());
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			builder.addPerson(personId, regionId);
			expectedRegionAssignments.put(personId, regionId);
		}
		maxId++;

		RegionsPluginData regionsPluginData = builder.build();

		for (int i = 0; i < maxId; i++) {
			PersonId personId = new PersonId(i);
			Optional<RegionId> optional = regionsPluginData.getPersonRegion(personId);
			RegionId regionId = expectedRegionAssignments.get(personId);
			if (regionId != null) {
				assertTrue(optional.isPresent());
				assertEquals(regionId, optional.get());
			} else {
				assertTrue(optional.isEmpty());
			}
		}

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionsPluginData.builder().build().getPersonRegion(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setRegionPropertyValue", args = { RegionId.class,
			RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		RegionId regionId = TestRegionId.REGION_1;

		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		Object validValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class)
				.build();

		builder.addRegion(regionId);
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);

		// non-precondition tests covered by testGetRegionPropertyValue

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.setRegionPropertyValue(null, regionPropertyId, validValue));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class,
				() -> builder.setRegionPropertyValue(regionId, null, validValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// Note: Invalid values will not throw an exception and are caught
		// during the build invocation.
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addPerson", args = { PersonId.class,
			RegionId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddPerson_Region() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		PersonId personId = new PersonId(45);
		RegionId regionId = TestRegionId.REGION_1;

		// non-precondition tests covered by testGetPersonRegion

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.addPerson(null, regionId));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the region id is null
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if other people have been added using region
		// arrival times
		contractException = assertThrows(ContractException.class, () -> {
			builder.addPerson(personId, regionId, 2.6);
			builder.addPerson(personId, regionId);
		});
		assertEquals(RegionError.REGION_ARRIVAL_TIMES_MISMATCHED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addPerson", args = { PersonId.class,
			RegionId.class, Double.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddPerson_Region_Time() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		PersonId personId = new PersonId(45);
		RegionId regionId = TestRegionId.REGION_1;
		Double time = 2.6;

		// non-precondition tests covered by testGetPersonRegion

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.addPerson(null, regionId, time));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the region id is null
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, null, time));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the time is null
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, regionId, null));
		assertEquals(RegionError.NULL_TIME, contractException.getErrorType());

		// precondition test: if the time is not finite
		contractException = assertThrows(ContractException.class,
				() -> builder.addPerson(personId, regionId, Double.NaN));
		assertEquals(RegionError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> builder.addPerson(personId, regionId, Double.POSITIVE_INFINITY));
		assertEquals(RegionError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> builder.addPerson(personId, regionId, Double.NEGATIVE_INFINITY));
		assertEquals(RegionError.NON_FINITE_TIME, contractException.getErrorType());

		// precondition test: if other people have been added without using
		// region
		// arrival times
		contractException = assertThrows(ContractException.class, () -> {
			builder.addPerson(personId, regionId);
			builder.addPerson(personId, regionId, 2.6);
		});
		assertEquals(RegionError.REGION_ARRIVAL_TIMES_MISMATCHED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setPersonRegionArrivalTracking", args = {
			boolean.class })
	public void testSetPersonRegionArrivalTracking() {
		assertTrue(RegionsPluginData.builder().setPersonRegionArrivalTracking(true).build()
				.getPersonRegionArrivalTrackingPolicy());
		assertFalse(RegionsPluginData.builder().setPersonRegionArrivalTracking(false).build()
				.getPersonRegionArrivalTrackingPolicy());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		/*
		 * The tracking of person arrival times and the existence of non-defalut region
		 * property definition is sensitive. In order to fully test the cloneBuilder
		 * mechanisms we must create four distinct tests that allow each mutation on the
		 * clone to be tested properly.
		 */

		testGetCloneBuilder_subTest1();
		testGetCloneBuilder_subTest2();
		testGetCloneBuilder_subTest3();
		testGetCloneBuilder_subTest4();
	}

	private RegionsPluginData getRegionsPluginData(boolean containsPeople, boolean useArrivalTracking,
			boolean useEmptyDefaultProperties, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		RegionsPluginData.Builder regionPluginDataBuilder = RegionsPluginData.builder();
		regionPluginDataBuilder.setPersonRegionArrivalTracking(useArrivalTracking);
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionPluginDataBuilder.addRegion(testRegionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			boolean defaultPresent = testRegionPropertyId.getPropertyDefinition().getDefaultValue().isPresent();

			if (defaultPresent || useEmptyDefaultProperties) {
				// this is a valid property
				regionPluginDataBuilder.defineRegionProperty(testRegionPropertyId,
						testRegionPropertyId.getPropertyDefinition());
			}
		}
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				boolean defaultPresent = testRegionPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
				if (defaultPresent || useEmptyDefaultProperties) {
					// this is a valid property

					if (!defaultPresent || randomGenerator.nextBoolean()) {
						Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
						regionPluginDataBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId,
								randomPropertyValue);
					}

				}
			}
		}

		int personCount = 0;
		if (containsPeople) {
			personCount = 100;
		}
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i * 2 + 5);
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			if (useArrivalTracking) {
				regionPluginDataBuilder.addPerson(personId, randomRegionId, 0.0);
			} else {
				regionPluginDataBuilder.addPerson(personId, randomRegionId);
			}
		}

		return regionPluginDataBuilder.build();
	}

	private void testGetCloneBuilder_subTest1() {
		
		boolean containsPeople = true;
		boolean useArrivalTracking = true;
		boolean useEmptyDefaultProperties = true;
		long seed = 6712645837048772782L;
		
		RegionsPluginData regionsPluginData = getRegionsPluginData(containsPeople, useArrivalTracking,
				useEmptyDefaultProperties, seed);

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		RegionsPluginData.Builder cloneBuilder = regionsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(regionsPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// addPerson(PersonId, RegionId,Double)
		cloneBuilder = regionsPluginData.getCloneBuilder();
		cloneBuilder.addPerson(new PersonId(1000), TestRegionId.REGION_1, 123.7);
		assertNotEquals(regionsPluginData, cloneBuilder.build());

		// defineRegionProperty
		cloneBuilder = regionsPluginData.getCloneBuilder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setDefaultValue(4)//
				.setType(Integer.class)//
				.setPropertyValueMutability(true)//
				.build();
		cloneBuilder.defineRegionProperty(TestRegionPropertyId.getUnknownRegionPropertyId(), propertyDefinition);
		assertNotEquals(regionsPluginData, cloneBuilder.build());

		// setRegionPropertyValue
		cloneBuilder = regionsPluginData.getCloneBuilder();
		cloneBuilder.setRegionPropertyValue(TestRegionId.REGION_1,
				TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE, 34.6);
		assertNotEquals(regionsPluginData, cloneBuilder.build());

	}

	private void testGetCloneBuilder_subTest2() {
		boolean containsPeople = true;
		boolean useArrivalTracking = false;
		boolean useEmptyDefaultProperties = true;
		long seed = 927079288013081717L;
		RegionsPluginData regionsPluginData = getRegionsPluginData(containsPeople, useArrivalTracking,
				useEmptyDefaultProperties, seed);

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		RegionsPluginData.Builder cloneBuilder = regionsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(regionsPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// addPerson(PersonId, RegionId)
		cloneBuilder = regionsPluginData.getCloneBuilder();
		cloneBuilder.addPerson(new PersonId(1000), TestRegionId.REGION_1);
		assertNotEquals(regionsPluginData, cloneBuilder.build());

	}

	private void testGetCloneBuilder_subTest3() {
		boolean containsPeople = true;
		boolean useArrivalTracking = true;
		boolean useEmptyDefaultProperties = false;
		long seed = 514836872882449614L;
		RegionsPluginData regionsPluginData = getRegionsPluginData(containsPeople, useArrivalTracking,
				useEmptyDefaultProperties, seed);

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		RegionsPluginData.Builder cloneBuilder = regionsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(regionsPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// addRegion
		cloneBuilder = regionsPluginData.getCloneBuilder();
		cloneBuilder.addRegion(TestRegionId.getUnknownRegionId());
		assertNotEquals(regionsPluginData, cloneBuilder.build());
	}

	private void testGetCloneBuilder_subTest4() {
		boolean containsPeople = false;
		boolean useArrivalTracking = true;
		boolean useEmptyDefaultProperties = true;
		long seed = 5969645744439416482L;
		RegionsPluginData regionsPluginData = getRegionsPluginData(containsPeople, useArrivalTracking,
				useEmptyDefaultProperties, seed);

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		RegionsPluginData.Builder cloneBuilder = regionsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(regionsPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// setPersonRegionArrivalTracking
		cloneBuilder = regionsPluginData.getCloneBuilder();
		cloneBuilder.setPersonRegionArrivalTracking(!regionsPluginData.getPersonRegionArrivalTrackingPolicy());
		assertNotEquals(regionsPluginData, cloneBuilder.build());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getPersonCount", args = {})
	public void testGetPersonCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(101704379866671191L);
		for (int j = 0; j < 10; j++) {
			RegionsPluginData.Builder regionPluginDataBuilder = RegionsPluginData.builder();
			regionPluginDataBuilder.setPersonRegionArrivalTracking(true);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				regionPluginDataBuilder.addRegion(testRegionId);
			}
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				regionPluginDataBuilder.defineRegionProperty(testRegionPropertyId,
						testRegionPropertyId.getPropertyDefinition());
			}
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
					if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
							|| randomGenerator.nextBoolean()) {
						Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
						regionPluginDataBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId,
								randomPropertyValue);
					}
				}
			}
			int personCount = randomGenerator.nextInt(100);
			int offset = j;
			for (int i = 0; i < personCount; i++) {
				/*
				 * the offset matters in this case because the setPersonRegion method skips all
				 * indexes of people based on the PersonId So if you had PersonId 1 and PersonId
				 * 3 The internal logic will place a blank value in index 2 (where PersonId 2
				 * would have been) So to acurately test this functionality, the offset must be
				 * added here and subtracted in the assert clause Because the offset is tied to
				 * the value of j, the offset will increase from 0 to 9 The effect of this is
				 * that the internal list will start at the offset index value instead of index
				 * 0
				 */
				PersonId personId = new PersonId(i + offset);
				TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				regionPluginDataBuilder.addPerson(personId, randomRegionId, 0.0);
			}

			RegionsPluginData regionsPluginData = regionPluginDataBuilder.build();
			assertNotNull(regionsPluginData);
			assertEquals(personCount, regionsPluginData.getPersonCount() - offset);

		}
	}

	private RegionsPluginData getRandomRegionsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Random random = new Random(randomGenerator.nextLong());
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		// define some region properties
		List<TestRegionPropertyId> regionPropertyIds = Arrays.asList(TestRegionPropertyId.values());
		List<TestRegionPropertyId> selectedRegionPropertyIds = new ArrayList<>();
		Collections.shuffle(regionPropertyIds, random);
		for (TestRegionPropertyId testRegionPropertyId : regionPropertyIds) {
			selectedRegionPropertyIds.add(testRegionPropertyId);
			builder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
			if (random.nextDouble() < 0.25) {
				break;
			}
		}

		// add a few regions
		List<TestRegionId> regionIds = Arrays.asList(TestRegionId.values());
		List<TestRegionId> selectedRegionIds = new ArrayList<>();
		Collections.shuffle(regionIds, random);
		for (TestRegionId testRegionId : regionIds) {
			selectedRegionIds.add(testRegionId);
			builder.addRegion(testRegionId);
			if (random.nextDouble() < 0.25) {
				break;
			}
		}

		Collections.shuffle(selectedRegionPropertyIds, random);

		// set some region property values
		for (TestRegionPropertyId testRegionPropertyId : selectedRegionPropertyIds) {
			Collections.shuffle(selectedRegionIds, random);
			for (TestRegionId testRegionId : selectedRegionIds) {
				PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
				Optional<Object> optional = propertyDefinition.getDefaultValue();
				if (optional.isEmpty() || randomGenerator.nextBoolean()) {
					Object propertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, propertyValue);
				}
			}
		}

		List<PersonId> people = new ArrayList<>();
		int personCount = random.nextInt(10);
		for (int i = 0; i < personCount; i++) {
			people.add(new PersonId(2 * i + 1));
		}
		boolean track = randomGenerator.nextBoolean();
		builder.setPersonRegionArrivalTracking(track);

		if (track) {
			for (PersonId personId : people) {
				TestRegionId testRegionId = selectedRegionIds.get(randomGenerator.nextInt(selectedRegionIds.size()));
				builder.addPerson(personId, testRegionId, randomGenerator.nextDouble());
			}
		} else {
			for (PersonId personId : people) {
				TestRegionId testRegionId = selectedRegionIds.get(randomGenerator.nextInt(selectedRegionIds.size()));
				builder.addPerson(personId, testRegionId);
			}
		}

		return builder.build();
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		RegionsPluginData pluginData = getRandomRegionsPluginData(0);
		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList("", "4.0.0", "4.1.0", StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(RegionsPluginData.checkVersionSupported(version));
			assertFalse(RegionsPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(RegionsPluginData.checkVersionSupported("badVersion"));
			assertFalse(RegionsPluginData.checkVersionSupported(version + "0"));
			assertFalse(RegionsPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(685858669518256073L);

		// is never equal to null
		for (int i = 0; i < 30; i++) {
			RegionsPluginData regionsPluginData = getRandomRegionsPluginData(randomGenerator.nextLong());
			assertFalse(regionsPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RegionsPluginData regionsPluginData = getRandomRegionsPluginData(randomGenerator.nextLong());
			assertTrue(regionsPluginData.equals(regionsPluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionsPluginData regionsPluginData1 = getRandomRegionsPluginData(seed);
			RegionsPluginData regionsPluginData2 = getRandomRegionsPluginData(seed);

			for (int j = 0; j < 5; j++) {
				assertTrue(regionsPluginData1.equals(regionsPluginData2));
				assertTrue(regionsPluginData2.equals(regionsPluginData1));
			}
		}

		// different inputs yield unequal objects
		Set<RegionsPluginData> regionsPluginDatas = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionsPluginData regionsPluginData = getRandomRegionsPluginData(randomGenerator.nextLong());
			regionsPluginDatas.add(regionsPluginData);
		}
		assertEquals(100, regionsPluginDatas.size());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(586211957860853353L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionsPluginData regionsPluginData1 = getRandomRegionsPluginData(seed);
			RegionsPluginData regionsPluginData2 = getRandomRegionsPluginData(seed);
			assertEquals(regionsPluginData1, regionsPluginData2);
			assertEquals(regionsPluginData1.hashCode(), regionsPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionsPluginData regionsPluginData = getRandomRegionsPluginData(randomGenerator.nextLong());
			hashCodes.add(regionsPluginData.hashCode());
		}
		assertEquals(100, hashCodes.size());

	}

//	RegionsPluginData	public java.lang.String plugins.regions.datamanagers.RegionsPluginData.toString() 

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "toString", args = {})
	public void testToString() {
		RegionsPluginData regionsPluginData = getRandomRegionsPluginData(6728844980805060979L);

		String actualValue = regionsPluginData.toString();

		// expected value manually verified
		String expectedValue = "RegionsPluginData [data=Data [" + "regionPropertyDefinitions={"
				+ "REGION_PROPERTY_3_DOUBLE_MUTABLE=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true, defaultValue=0.0], "
				+ "REGION_PROPERTY_5_INTEGER_IMMUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false, defaultValue=0]}, "

				+ "regionIds=[REGION_3, REGION_1, REGION_4], "

				+ "trackRegionArrivalTimes=true, "

				+ "regionPropertyValues={" + "REGION_1={REGION_PROPERTY_5_INTEGER_IMMUTABLE=1769994519}, "
				+ "REGION_3={REGION_PROPERTY_5_INTEGER_IMMUTABLE=706454702}}, "

				+ "personRegions=[null, REGION_4, null, REGION_3, null, REGION_1, null, REGION_3, null, REGION_1], "

				+ "personArrivalTimes=[null, 0.008078132587675535, null, 0.7027533190641975, null, 0.38173962849044774, null, 0.7955082969867588, null, 0.9457602126490658], "

				+ "locked=true]]";

		assertEquals(expectedValue, actualValue);
	}
}
