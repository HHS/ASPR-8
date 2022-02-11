package plugins.regions.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import plugins.regions.initialdata.RegionInitialData.Builder;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test unit for {@linkplain RegionInitialData}. Tests for
 * RegionInitialData.Builder are limited to precondition tests and are otherwise
 * covered via the class level tests. Note that the builder does not impose any
 * ordering on the invocation of its methods and many validation tests are
 * deferred to the build invocation.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = RegionInitialData.class)
public class AT_RegionInitialData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that we can create a builder
		assertNotNull(RegionInitialData.builder());
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
		Builder builder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		RegionInitialData regionInitialData = builder.build();

		Set<RegionId> actualRegionIds = regionInitialData.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);
	}

	@Test
	@UnitTestMethod(name = "getRegionInitialBehavior", args = { RegionId.class })
	public void testGetRegionInitialBehavior() {

		Builder builder = RegionInitialData.builder();
		/*
		 * Add consumers associated with regions
		 */
		Map<RegionId, Consumer<AgentContext>> expectedConsumers = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			Consumer<AgentContext> consumer = (c) -> {
			};
			expectedConsumers.put(testRegionId, consumer);
			Supplier<Consumer<AgentContext>> supplier = () -> consumer;
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, supplier);
		}
		RegionInitialData regionInitialData = builder.build();

		/*
		 * Show that the consumers retrieved by region id were the ones that had
		 * been added to the builder
		 */

		for (TestRegionId testRegionId : TestRegionId.values()) {
			Consumer<AgentContext> actualConsumer = regionInitialData.getRegionComponentInitialBehavior(testRegionId);
			Consumer<AgentContext> expectedConsumer = expectedConsumers.get(testRegionId);
			assertEquals(expectedConsumer, actualConsumer);
		}

		// precondition tests

		// null region id
		ContractException contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionComponentInitialBehavior(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// unknown region id
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionComponentInitialBehavior(TestRegionId.getUnknownRegionId()));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		Builder builder = RegionInitialData.builder();
		/*
		 * Place the various properties defined in TestRegionPropertyId into the
		 * builder and associate them with distinct property definitions. Each
		 * property definition will differ by its initial value.
		 */
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});

		}

		int defaultValue = 0;
		Map<RegionPropertyId, PropertyDefinition> expectedDefinitions = new LinkedHashMap<>();
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			expectedDefinitions.put(testRegionPropertyId, propertyDefinition);
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		// build the region initial data
		RegionInitialData regionInitialData = builder.build();

		/*
		 * Retrieve all of the property definitions in the region initial data
		 * and place them in a map for comparison.
		 */
		Map<RegionPropertyId, PropertyDefinition> actualDefinitions = new LinkedHashMap<>();

		Set<RegionPropertyId> regionPropertyIds = regionInitialData.getRegionPropertyIds();
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			PropertyDefinition propertyDefinition = regionInitialData.getRegionPropertyDefinition(regionPropertyId);
			actualDefinitions.put(regionPropertyId, propertyDefinition);
		}

		// show that the two maps are equal
		assertEquals(expectedDefinitions, actualDefinitions);

		// precondition tests

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyDefinition(null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = { RegionId.class })
	public void testGetRegionPropertyIds() {
		Builder builder = RegionInitialData.builder();
		/*
		 * Place the various region/property pairs defined in TestRegionId into
		 * the builder and associate them with distinct property definitions.
		 */

		Set<RegionPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
			
		}
		
		for (TestRegionPropertyId  testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();			
			builder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
			expectedPropertyIds.add(testRegionPropertyId);
		}


		// build the region initial data
		RegionInitialData regionInitialData = builder.build();

		/*
		 * Retrieve all of the property defintions in the region inital data and
		 * place them in a map for comparison.
		 */
		Set<RegionPropertyId> actualPropertyIds = regionInitialData.getRegionPropertyIds();
		

		// show that the two sets are equal
		assertEquals(expectedPropertyIds, actualPropertyIds);

		// no precondition tests

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {
		Builder builder = RegionInitialData.builder();
		/*
		 * Place the various region/property pairs defined in TestRegionId into
		 * the builder and associate them with distinct property values. Each
		 * property value will be unique.
		 */
		
		Map<RegionId, Map<RegionPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
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
				expectedPropertyValues.get(testRegionId).put(testRegionPropertyId,propertyValue);
				builder.setRegionPropertyValue(testRegionId, testRegionPropertyId, propertyValue);
				propertyValue++;
			}
		}
		

		// build the region initial data
		RegionInitialData regionInitialData = builder.build();

		/*
		 * Retrieve all of the property values in the region inital data and
		 * place them in a map for comparison.
		 */
		Map<RegionId, Map<RegionPropertyId, Object>> actualPropertyValues = new LinkedHashMap<>();
		for (RegionId regionId : regionInitialData.getRegionIds()) {
			Map<RegionPropertyId, Object> map = new LinkedHashMap<>();
			actualPropertyValues.put(regionId, map);
			Set<RegionPropertyId> regionPropertyIds = regionInitialData.getRegionPropertyIds();
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object regionPropertyValue = regionInitialData.getRegionPropertyValue(regionId, regionPropertyId);
				map.put(regionPropertyId, regionPropertyValue);
			}
		}

		// show that the two maps are equal
		assertEquals(expectedPropertyValues, actualPropertyValues);

		// precondition tests

		// create some valid inputs to help with the precondition tests
		RegionId validRegionId = TestRegionId.REGION_1;
		RegionPropertyId validRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyValue(null, validRegionPropertyId));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyValue(TestRegionId.getUnknownRegionId(), validRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyValue(validRegionId, null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getRegionPropertyValue(validRegionId, TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonRegion", args = { PersonId.class })
	public void testGetPersonRegion() {
		Builder builder = RegionInitialData.builder();

		/*
		 * Add the regions
		 */
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}

		/*
		 * Place people in regions
		 */
		Map<PersonId, RegionId> expectedRegionAssignments = new LinkedHashMap<>();

		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i);
			testRegionId = testRegionId.next();
			expectedRegionAssignments.put(personId, testRegionId);
			builder.setPersonRegion(personId, testRegionId);
		}

		// build the region initial data
		RegionInitialData regionInitialData = builder.build();

		/*
		 * Retrieve the people and their regions for comparison
		 */
		Map<PersonId, RegionId> actualRegionAssignments = new LinkedHashMap<>();
		for (PersonId personId : regionInitialData.getPersonIds()) {
			RegionId region = regionInitialData.getPersonRegion(personId);
			actualRegionAssignments.put(personId, region);
		}

		// show that the two maps are equal
		assertEquals(expectedRegionAssignments, actualRegionAssignments);

		// precondition tests

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionInitialData.getPersonRegion(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> regionInitialData.getPersonRegion(new PersonId(10000)));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {

		Builder builder = RegionInitialData.builder();

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			builder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			RegionInitialData regionInitialData = builder.build();
			assertEquals(timeTrackingPolicy, regionInitialData.getPersonRegionArrivalTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonIds", args = {})
	public void testGetPersonIds() {
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i * i + 7);
			expectedPersonIds.add(personId);
		}

		Builder builder = RegionInitialData.builder();
		builder.setRegionComponentInitialBehaviorSupplier(TestRegionId.REGION_1, () -> (c) -> {
		});
		for (PersonId personId : expectedPersonIds) {
			builder.setPersonRegion(personId, TestRegionId.REGION_1);
		}
		RegionInitialData regionInitialData = builder.build();
		Set<PersonId> actualPersonIds = regionInitialData.getPersonIds();
		assertEquals(expectedPersonIds, actualPersonIds);

	}

	@Test
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		Builder builder = RegionInitialData.builder();
		// show the builder does not return null
		assertNotNull(builder.build());

		// precondition tests

		/*
		 * if a person was associated with a region id that was not properly
		 * added with an initial agent behavior.
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonRegion(new PersonId(8), TestRegionId.REGION_1).build());
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		

		/*
		 * if a region property value was associated with a region id that was
		 * not properly added with an initial agent behavior.
		 */
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();		
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region property id
		 * that was not defined
		 */
		builder.setRegionComponentInitialBehaviorSupplier(TestRegionId.REGION_1, () -> (c) -> {
		});
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, 5);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a region property value was associated with a region and region
		 * property id that is incompatible with the corresponding property
		 * definition.
		 */
		builder.setRegionComponentInitialBehaviorSupplier(TestRegionId.REGION_1, () -> (c) -> {
		});
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		builder.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyId, "invalid value");
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a region property definition does not have a default value and
		 * there are no property values added to replace that default.
		 */
		builder.setRegionComponentInitialBehaviorSupplier(TestRegionId.REGION_1, () -> (c) -> {
		});
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).build();
		builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(RegionError.INSUFFICIENT_REGION_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "defineRegionProperty", args = { RegionId.class, RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		Builder builder = RegionInitialData.builder();
		
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
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
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "setRegionInitialBehaviorSupplier", args = { RegionId.class, RegionPropertyId.class, PropertyDefinition.class })
	public void testSetRegionInitialBehaviorSupplier() {
		Builder builder = RegionInitialData.builder();
		RegionId regionId = TestRegionId.REGION_1;
		Supplier<Consumer<AgentContext>> supplier = () -> (c) -> {
		};

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setRegionComponentInitialBehaviorSupplier(null, supplier));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the supplier is null
		contractException = assertThrows(ContractException.class, () -> builder.setRegionComponentInitialBehaviorSupplier(regionId, null));
		assertEquals(RegionError.NULL_REGION_COMPONENT_INITIAL_BEHAVIOR_SUPPLIER, contractException.getErrorType());

		// if the region initial behavior was previously defined
		builder.setRegionComponentInitialBehaviorSupplier(regionId, () -> (c) -> {
		});
		contractException = assertThrows(ContractException.class, () -> builder.setRegionComponentInitialBehaviorSupplier(regionId, supplier));
		assertEquals(RegionError.DUPLICATE_REGION_INITIAL_BEHAVIOR_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		Builder builder = RegionInitialData.builder();

		RegionId regionId = TestRegionId.REGION_1;
		Supplier<Consumer<AgentContext>> supplier = () -> (c) -> {
		};
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object validValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();

		builder.setRegionComponentInitialBehaviorSupplier(regionId, supplier);
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
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "setPersonRegion", args = { PersonId.class, RegionId.class })
	public void testSetPersonRegion() {
		Builder builder = RegionInitialData.builder();

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
	@UnitTestMethod(target = RegionInitialData.Builder.class, name = "setPersonRegionArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonRegionArrivalTracking() {
		Builder builder = RegionInitialData.builder();

		// if the timeTrackingPolicy is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonRegionArrivalTracking(null));
		assertEquals(RegionError.NULL_TIME_TRACKING_POLICY, contractException.getErrorType());

		// if the timeTrackingPolicy was previously defined
		builder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		contractException = assertThrows(ContractException.class, () -> builder.setPersonRegionArrivalTracking(TimeTrackingPolicy.DO_NOT_TRACK_TIME));
		assertEquals(RegionError.DUPLICATE_TIME_TRACKING_POLICY, contractException.getErrorType());

	}

}
