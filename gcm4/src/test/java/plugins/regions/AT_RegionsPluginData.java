package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

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
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		/*
		 * Place the various properties defined in TestRegionPropertyId into the
		 * builder and associate them with distinct property definitions. Each
		 * property definition will differ by its initial value.
		 */
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);

		}

		int defaultValue = 0;
		Map<RegionPropertyId, PropertyDefinition> expectedDefinitions = new LinkedHashMap<>();
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			expectedDefinitions.put(testRegionPropertyId, propertyDefinition);
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property definitions in the region initial data
		 * and place them in a map for comparison.
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
		ContractException contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		/*
		 * Place the various region/property pairs defined in TestRegionId into
		 * the builder and associate them with distinct property definitions.
		 */

		Set<RegionPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);

		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
			expectedPropertyIds.add(testRegionPropertyId);
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property defintions in the region inital data and
		 * place them in a map for comparison.
		 */
		Set<RegionPropertyId> actualPropertyIds = regionsPluginData.getRegionPropertyIds();

		// show that the two sets are equal
		assertEquals(expectedPropertyIds, actualPropertyIds);

		// no precondition tests

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getRegionPropertyValues", args = { RegionId.class })
	public void testGetRegionPropertyValues() {
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
		 * set about half of the properties for each region for those properties
		 * that have a default value
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
		 * set all of the properties for each region for those properties that
		 * do not have a default value
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
		 * Retrieve all of the property values in the region inital data and
		 * place them in a map for comparison.
		 */
		for (RegionId regionId : regionsPluginData.getRegionIds()) {
			Map<RegionPropertyId, Object> expectedMap = expectedPropertyValues.get(regionId);
			Map<RegionPropertyId, Object> actualMap = regionsPluginData.getRegionPropertyValues(regionId);
			// show that the two maps are equal
			assertEquals(expectedMap, actualMap);
		}

		// precondition test: if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValues(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValues(TestRegionId.getUnknownRegionId()));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {

		RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																.setPersonRegionArrivalTracking(true)//
																.build();//
		assertEquals(true, regionsPluginData.getPersonRegionArrivalTrackingPolicy());

		regionsPluginData = RegionsPluginData	.builder()//
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
		 * if a region property value was associated with a region id that was
		 * not properly added with an initial agent behavior.
		 */
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData	.builder().defineRegionProperty(regionPropertyId, propertyDefinition)//
								.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5)//
								.build();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region property id
		 * that was not defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData	.builder()//
								.addRegion(TestRegionId.REGION_1)//
								.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5)//
								.build();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region and region
		 * property id that is incompatible with the corresponding property
		 * definition.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData	.builder()//
								.addRegion(TestRegionId.REGION_1)//
								.defineRegionProperty(regionPropertyId, propertyDefinition)//
								.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, "invalid value")//
								.build();//
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a region property definition does not have a default value and
		 * there are no property values added to replace that default.
		 */

		contractException = assertThrows(ContractException.class, () -> {
			PropertyDefinition def = PropertyDefinition.builder().setType(Double.class).build();

			RegionsPluginData	.builder()//
								.addRegion(TestRegionId.REGION_1)//
								.defineRegionProperty(regionPropertyId, def)//
								.build();//
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * if a person region arrival data was collected, but the policy for
		 * region arrival tracking is false
		 */
		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData	.builder()//
								.addRegion(TestRegionId.REGION_1)//
								.setPersonRegionArrivalTracking(false)//
								.addPerson(new PersonId(0), TestRegionId.REGION_1, 0.0)//
								.build();//
		});
		assertEquals(RegionError.PERSON_ARRIVAL_DATA_PRESENT, contractException.getErrorType());

		/*
		 * if the policy for region arrival tracking is set to true, but region
		 * arrival times are missing
		 */

		contractException = assertThrows(ContractException.class, () -> {
			RegionsPluginData	.builder()//
								.addRegion(TestRegionId.REGION_1)//
								.setPersonRegionArrivalTracking(true)//
								.addPerson(new PersonId(0), TestRegionId.REGION_1)//
								.build();
		});
		assertEquals(RegionError.MISSING_PERSON_ARRIVAL_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "defineRegionProperty", args = { RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(9).setType(Integer.class).build();

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.defineRegionProperty(null, propertyDefinition));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineRegionProperty(regionPropertyId, null));
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
		ContractException contractException = assertThrows(ContractException.class, () -> RegionsPluginData.builder().build().getPersonRegionArrivalTime(null));
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
		ContractException contractException = assertThrows(ContractException.class, () -> RegionsPluginData.builder().build().getPersonRegion(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		RegionId regionId = TestRegionId.REGION_1;

		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		Object validValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();

		builder.addRegion(regionId);
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);

		// non-precondition tests covered by testGetRegionPropertyValue

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyValue(null, regionPropertyId, validValue));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyValue(regionId, null, validValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// Note: Invalid values will not throw an exception and are caught
		// during the build invocation.
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addPerson", args = { PersonId.class, RegionId.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddPerson_Region() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		PersonId personId = new PersonId(45);
		RegionId regionId = TestRegionId.REGION_1;

		// non-precondition tests covered by testGetPersonRegion

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addPerson(null, regionId));
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
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addPerson", args = { PersonId.class, RegionId.class, Double.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddPerson_Region_Time() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		PersonId personId = new PersonId(45);
		RegionId regionId = TestRegionId.REGION_1;
		Double time = 2.6;

		// non-precondition tests covered by testGetPersonRegion

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addPerson(null, regionId, time));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the region id is null
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, null, time));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the time is null
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, regionId, null));
		assertEquals(RegionError.NULL_TIME, contractException.getErrorType());

		// precondition test: if the time is not finite
		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, regionId, Double.NaN));
		assertEquals(RegionError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, regionId, Double.POSITIVE_INFINITY));
		assertEquals(RegionError.NON_FINITE_TIME, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> builder.addPerson(personId, regionId, Double.NEGATIVE_INFINITY));
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
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setPersonRegionArrivalTracking", args = { boolean.class })
	public void testSetPersonRegionArrivalTracking() {
		assertTrue(RegionsPluginData.builder().setPersonRegionArrivalTracking(true).build().getPersonRegionArrivalTrackingPolicy());
		assertFalse(RegionsPluginData.builder().setPersonRegionArrivalTracking(false).build().getPersonRegionArrivalTrackingPolicy());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6712645837048772782L);
		RegionsPluginData.Builder regionPluginDataBuilder = RegionsPluginData.builder();
		regionPluginDataBuilder.setPersonRegionArrivalTracking(true);
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionPluginDataBuilder.addRegion(testRegionId);
		}
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginDataBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionPluginDataBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId, randomPropertyValue);
				}
			}
		}
		int personCount = 100;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i * 2 + 5);
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionPluginDataBuilder.addPerson(personId, randomRegionId, 0.0);
		}

		RegionsPluginData regionsPluginData = regionPluginDataBuilder.build();

		PluginData pluginData = regionsPluginData.getCloneBuilder().build();

		// show that the clone plugin data has the correct type
		assertTrue(pluginData instanceof RegionsPluginData);
		RegionsPluginData cloneRegionPluginData = (RegionsPluginData) pluginData;

		// show that the two plugin datas have the same arrival tracking policy
		assertEquals(regionsPluginData.getPersonRegionArrivalTrackingPolicy(), cloneRegionPluginData.getPersonRegionArrivalTrackingPolicy());

		// show that the two plugin datas have the same region ids
		assertEquals(regionsPluginData.getRegionIds(), cloneRegionPluginData.getRegionIds());

		// show that the two plugin datas have the same region property ids
		assertEquals(regionsPluginData.getRegionPropertyIds(), cloneRegionPluginData.getRegionPropertyIds());

		// show that the two plugin datas have the same region property
		// definitions
		for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
			PropertyDefinition expectedPropertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
			PropertyDefinition actualPropertyDefinition = cloneRegionPluginData.getRegionPropertyDefinition(regionPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that the two plugin datas have the same region property values
		for (RegionId regionId : regionsPluginData.getRegionIds()) {
			Map<RegionPropertyId, Object> expectedRegionPropertyValues = regionsPluginData.getRegionPropertyValues(regionId);
			Map<RegionPropertyId, Object> actualRegionPropertyValues = cloneRegionPluginData.getRegionPropertyValues(regionId);
			assertEquals(expectedRegionPropertyValues, actualRegionPropertyValues);
		}

		// show that the two plugin datas have the same people and region
		// assignments

		int pluginPersonCount = regionsPluginData.getPersonCount();
		int clonePluginPersonCount = cloneRegionPluginData.getPersonCount();
		assertEquals(pluginPersonCount, clonePluginPersonCount);

		for (int i = 0; i < pluginPersonCount; i++) {
			PersonId personId = new PersonId(i);
			boolean isPresentInPluginData = regionsPluginData.getPersonRegion(personId).isPresent();
			boolean isPresentInClonePluginData = cloneRegionPluginData.getPersonRegion(personId).isPresent();
			assertEquals(isPresentInPluginData, isPresentInClonePluginData);
			if (isPresentInPluginData) {
				RegionId expectedRegionId = regionsPluginData.getPersonRegion(personId).get();
				RegionId actualRegionId = cloneRegionPluginData.getPersonRegion(personId).get();
				assertEquals(expectedRegionId, actualRegionId);
			}
		}

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
				regionPluginDataBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
			}
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
					if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
						Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
						regionPluginDataBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId, randomPropertyValue);
					}
				}
			}
			int personCount = randomGenerator.nextInt(100);
			int offset = j;
			for (int i = 0; i < personCount; i++) {
				/*
				 * the offset matters in this case because the setPersonRegion
				 * method skips all indexes of people based on the PersonId So
				 * if you had PersonId 1 and PersonId 3 The internal logic will
				 * place a blank value in index 2 (where PersonId 2 would have
				 * been) So to acurately test this functionality, the offset
				 * must be added here and subtracted in the assert clause
				 * Because the offset is tied to the value of j, the offset will
				 * increase from 0 to 9 The effect of this is that the internal
				 * list will start at the offset index value instead of index 0
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

}
