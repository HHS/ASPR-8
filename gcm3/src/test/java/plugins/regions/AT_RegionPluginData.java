package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
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
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * Test unit for {@linkplain RegionsPluginData}. Tests for
 * RegionPluginData.Builder are limited to precondition tests and are otherwise
 * covered via the class level tests. Note that the builder does not impose any
 * ordering on the invocation of its methods and many validation tests are
 * deferred to the build invocation.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = RegionsPluginData.class)
public class AT_RegionPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that we can create a builder
		assertNotNull(RegionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
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
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionId.class, RegionPropertyId.class })
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
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = { RegionId.class })
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
	@UnitTestMethod(name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		/*
		 * Place the various region/property pairs defined in TestRegionId into
		 * the builder and associate them with distinct property values. Each
		 * property value will be unique.
		 */

		Map<RegionId, Map<RegionPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.addRegion(testRegionId);
			Map<RegionPropertyId, Object> map = new LinkedHashMap<>();
			expectedPropertyValues.put(testRegionId, map);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		int propertyValue = 0;
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedPropertyValues.get(testRegionId).put(testRegionPropertyId, propertyValue);
				builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, propertyValue);
				propertyValue++;
			}
		}

		// build the region initial data
		RegionsPluginData regionsPluginData = builder.build();

		/*
		 * Retrieve all of the property values in the region inital data and
		 * place them in a map for comparison.
		 */
		Map<RegionId, Map<RegionPropertyId, Object>> actualPropertyValues = new LinkedHashMap<>();
		for (RegionId regionId : regionsPluginData.getRegionIds()) {
			Map<RegionPropertyId, Object> map = new LinkedHashMap<>();
			actualPropertyValues.put(regionId, map);
			Set<RegionPropertyId> regionPropertyIds = regionsPluginData.getRegionPropertyIds();
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object regionPropertyValue = regionsPluginData.getRegionPropertyValue(regionId, regionPropertyId);
				map.put(regionPropertyId, regionPropertyValue);
			}
		}

		// show that the two maps are equal
		assertEquals(expectedPropertyValues, actualPropertyValues);

		// precondition tests

		// create some valid inputs to help with the precondition tests
		RegionId validRegionId = TestRegionId.REGION_1;
		RegionPropertyId validRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValue(null, validRegionPropertyId));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValue(TestRegionId.getUnknownRegionId(), validRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValue(validRegionId, null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionsPluginData.getRegionPropertyValue(validRegionId, TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {

		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			builder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			RegionsPluginData regionsPluginData = builder.build();
			assertEquals(timeTrackingPolicy, regionsPluginData.getPersonRegionArrivalTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		// show the builder does not return null
		assertNotNull(builder.build());

		// precondition tests

		/*
		 * if a region property value was associated with a region id that was
		 * not properly added with an initial agent behavior.
		 */
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5);
		ContractException contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region property id
		 * that was not defined
		 */
		builder.addRegion(TestRegionId.REGION_1);
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region and region
		 * property id that is incompatible with the corresponding property
		 * definition.
		 */
		builder.addRegion(TestRegionId.REGION_1);
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, "invalid value");
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a region property definition does not have a default value and
		 * there are no property values added to replace that default.
		 */
		builder.addRegion(TestRegionId.REGION_1);
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).build();
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.INSUFFICIENT_REGION_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "defineRegionProperty", args = { RegionId.class, RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(9).setType(Integer.class).build();

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.defineRegionProperty(null, propertyDefinition));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineRegionProperty(regionPropertyId, null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_DEFINITION, contractException.getErrorType());

		/*
		 * if a property definition for the given region id and property id was
		 * previously defined.
		 */
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.defineRegionProperty(regionPropertyId, propertyDefinition));
		assertEquals(RegionError.DUPLICATE_REGION_PROPERTY_DEFINITION_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "addRegion", args = { RegionId.class, RegionPropertyId.class, PropertyDefinition.class })
	public void testAddRegion() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addRegion(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
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

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyValue(null, regionPropertyId, validValue));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyValue(regionId, null, validValue));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property value was previously defined
		builder.setRegionPropertyValue(regionId, regionPropertyId, validValue);
		contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyValue(regionId, regionPropertyId, validValue));
		assertEquals(RegionError.DUPLICATE_REGION_PROPERTY_VALUE, contractException.getErrorType());

		// Note: Invalid values will not throw an exception and are caught
		// during the build invocation.
	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setPersonRegionArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonRegionArrivalTracking() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		// if the timeTrackingPolicy is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonRegionArrivalTracking(null));
		assertEquals(RegionError.NULL_TIME_TRACKING_POLICY, contractException.getErrorType());

		// if the timeTrackingPolicy was previously defined
		builder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		contractException = assertThrows(ContractException.class, () -> builder.setPersonRegionArrivalTracking(TimeTrackingPolicy.DO_NOT_TRACK_TIME));
		assertEquals(RegionError.DUPLICATE_TIME_TRACKING_POLICY, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsPluginData.Builder.class, name = "setPersonRegion", args = { PersonId.class, RegionId.class })
	public void testSetPersonRegion() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		PersonId personId = new PersonId(45);
		RegionId regionId = TestRegionId.REGION_1;

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonRegion(null, regionId));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the region id is null
		contractException = assertThrows(ContractException.class, () -> builder.setPersonRegion(personId, null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the person's region was previously defined
		builder.setPersonRegion(personId, regionId);
		contractException = assertThrows(ContractException.class, () -> builder.setPersonRegion(personId, regionId));
		assertEquals(RegionError.DUPLICATE_PERSON_REGION_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6712645837048772782L);
		RegionsPluginData.Builder regionPluginDataBuilder = RegionsPluginData.builder();
		regionPluginDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionPluginDataBuilder.addRegion(testRegionId);
		}
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginDataBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionPluginDataBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId, randomPropertyValue);
				}
			}
		}
		int personCount = 100;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionPluginDataBuilder.setPersonRegion(personId, randomRegionId);
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
			for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
				Object expectedPropertyValue = regionsPluginData.getRegionPropertyValue(regionId, regionPropertyId);
				Object actualPropertyValue = cloneRegionPluginData.getRegionPropertyValue(regionId, regionPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);
			}
		}

		// show that the two plugin datas have the same people
		Set<PersonId> expectedPersonIds = regionsPluginData.getPersonIds();
		Set<PersonId> actualPersonIds = cloneRegionPluginData.getPersonIds();
		assertEquals(expectedPersonIds, actualPersonIds);
		// show that the two plugin datas have assigned the people to the same
		// regions
		for (PersonId personId : expectedPersonIds) {
			RegionId expectedRegionId = regionsPluginData.getPersonRegion(personId);
			RegionId actualRegionId = cloneRegionPluginData.getPersonRegion(personId);
			assertEquals(expectedRegionId, actualRegionId);
		}

	}

}
