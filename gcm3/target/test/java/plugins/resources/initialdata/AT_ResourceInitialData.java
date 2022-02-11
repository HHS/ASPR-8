package plugins.resources.initialdata;

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

import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.initialdata.ResourceInitialData.Builder;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import util.ContractException;
import util.MultiKey;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ResourceInitialData.class)
public final class AT_ResourceInitialData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ResourceInitialData.builder());
	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		Builder builder = ResourceInitialData.builder();
		assertNotNull(builder.build());

		// precondition tests

		/*
		 * if a resource tracking policy was collected for a resource that was
		 * not added
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> //
		builder	.setResourceTimeTracking(TestResourceId.RESOURCE_1, TestResourceId.RESOURCE_1.getTimeTrackingPolicy())//
				.build());//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource property definition was collected for a resource that
		 * was not added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE.getPropertyDefinition();
			builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource that was
		 * not added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			Boolean value = false;
			builder.setResourcePropertyValue(resourceId, resourcePropertyId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource property
		 * that is not associated with the given resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			Boolean value = false;
			builder.addResource(resourceId);
			builder.setResourcePropertyValue(resourceId, resourcePropertyId, value);
			builder.build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource property
		 * that is not compatible with the associated resource property
		 * definition
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE.getPropertyDefinition();
			Integer value = 5;
			builder.addResource(resourceId);
			builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			builder.setResourcePropertyValue(resourceId, resourcePropertyId, value);
			builder.build();
		});//
		assertEquals(ResourceError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a resource property definition has a null default value and there
		 * is no assigned resource property value for that resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
			builder.addResource(resourceId);
			builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			builder.build();
		});//
		assertEquals(ResourceError.INSUFFICIENT_RESOURCE_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
		/*
		 * if a resource level was collected for a person that is an unknown
		 * resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			Long value = 566L;
			builder.setPersonResourceLevel(new PersonId(0), resourceId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource level was collected for a region that is an unknown
		 * resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			Long value = 566L;
			builder.setRegionResourceLevel(TestRegionId.REGION_1, resourceId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "", args = {})
	public void testAddResource() {

		Builder builder = ResourceInitialData.builder();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			expectedResourceIds.add(testResourceId);
		}
		ResourceInitialData resourceInitialData = builder.build();
		assertEquals(expectedResourceIds, resourceInitialData.getResourceIds());

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> ResourceInitialData.builder().addResource(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id was previously added
		contractException = assertThrows(ContractException.class, () -> ResourceInitialData.builder().addResource(TestResourceId.RESOURCE_1).addResource(TestResourceId.RESOURCE_1));
		assertEquals(ResourceError.DUPLICATE_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "defineResourceProperty", args = { ResourceId.class, ResourcePropertyId.class, PropertyDefinition.class })
	public void testDefineResourceProperty() {

		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceInitialData.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		// precondition tests

		ResourceId resourceId = TestResourceId.RESOURCE_2;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;
		PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE.getPropertyDefinition();

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().defineResourceProperty(null, resourcePropertyId, propertyDefinition);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().defineResourceProperty(resourceId, null, propertyDefinition);
		});
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().defineResourceProperty(resourceId, resourcePropertyId, null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_DEFINITION, contractException.getErrorType());

		// if a resource property definition for the given resource id and
		// property id was previously defined.
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition).defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		});
		assertEquals(ResourceError.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "setPersonResourceLevel", args = { PersonId.class, ResourceId.class, long.class })
	public void testSetPersonResourceLevel() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6539895160899665826L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			people.add(new PersonId(randomGenerator.nextInt()));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(personId, testResourceId);
				MutableInteger mutableInteger = new MutableInteger();
				expectedValues.put(multiKey, mutableInteger);
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					mutableInteger.setValue(amount);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(personId, testResourceId);
				MutableInteger mutableInteger = expectedValues.get(multiKey);
				int expectedAmount = mutableInteger.getValue();
				Long personResourceLevel = resourceInitialData.getPersonResourceLevel(personId, testResourceId);
				assertEquals(expectedAmount, personResourceLevel);
			}
		}

		// precondition tests
		PersonId personId = new PersonId(0);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 678;

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonResourceLevel(null, resourceId, amount));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> builder.setPersonResourceLevel(personId, null, amount));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class, () -> builder.setPersonResourceLevel(personId, resourceId, -5L));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		// if the person's resource level was previously assigned
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().setPersonResourceLevel(personId, resourceId, amount).setPersonResourceLevel(personId, resourceId, amount);
		});
		assertEquals(ResourceError.DUPLICATE_PERSON_RESOURCE_LEVEL_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "setRegionResourceLevel", args = { RegionId.class, ResourceId.class, long.class })
	public void testSetRegionResourceLevel() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8877834706249831995L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
				MutableInteger mutableInteger = new MutableInteger();
				expectedValues.put(multiKey, mutableInteger);
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);
					mutableInteger.setValue(amount);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
				MutableInteger mutableInteger = expectedValues.get(multiKey);
				int expectedAmount = mutableInteger.getValue();
				Long regionResourceLevel = resourceInitialData.getRegionResourceLevel(testRegionId, testResourceId);
				assertEquals(expectedAmount, regionResourceLevel);
			}
		}

		// precondition tests
		RegionId regionId = TestRegionId.REGION_3;
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 678;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setRegionResourceLevel(null, resourceId, amount));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> builder.setRegionResourceLevel(regionId, null, amount));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class, () -> builder.setRegionResourceLevel(regionId, resourceId, -5L));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		// if the person's resource level was previously assigned
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().setRegionResourceLevel(regionId, resourceId, amount).setRegionResourceLevel(regionId, resourceId, amount);
		});
		assertEquals(ResourceError.DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7516798209205913252L);

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		ResourceInitialData.Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				expectedValues.put(multiKey, propertyDefinition.getDefaultValue().get());
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedValues.put(multiKey, value);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourceInitialData.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// precondition tests
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(null, resourcePropertyId, 5));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null</li>
		contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property value is null
		contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property value was previously assigned
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().setResourcePropertyValue(resourceId, resourcePropertyId, 5).setResourcePropertyValue(resourceId, resourcePropertyId, 5);
		});
		assertEquals(ResourceError.DUPLICATE_RESOURCE_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialData.Builder.class, name = "setResourceTimeTracking", args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testSetResourceTimeTracking() {
		// 6539895160899665826L

		int i = 0;
		Map<ResourceId, TimeTrackingPolicy> expectedValues = new LinkedHashMap<>();

		ResourceInitialData.Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			if (testResourceId != TestResourceId.RESOURCE_5) {
				builder.addResource(testResourceId);
				int index = i % TimeTrackingPolicy.values().length;
				TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[index];
				builder.setResourceTimeTracking(testResourceId, timeTrackingPolicy);
				expectedValues.put(testResourceId, timeTrackingPolicy);
			}
		}
		builder.addResource(TestResourceId.RESOURCE_5);
		expectedValues.put(TestResourceId.RESOURCE_5, TimeTrackingPolicy.DO_NOT_TRACK_TIME);

		ResourceInitialData resourceInitialData = builder.build();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			TimeTrackingPolicy expectedPolicy = expectedValues.get(testResourceId);
			TimeTrackingPolicy actualPolicy = resourceInitialData.getPersonResourceTimeTrackingPolicy(testResourceId);
			assertEquals(expectedPolicy, actualPolicy);
		}

		// precondition tests
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;

		// if the resource id is null
		// ResourceError#NULL_RESOURCE_ID
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setResourceTimeTracking(null, timeTrackingPolicy));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the tracking policy is null
		// ResourceError.NULL_TIME_TRACKING_POLICY
		contractException = assertThrows(ContractException.class, () -> builder.setResourceTimeTracking(resourceId, null));
		assertEquals(ResourceError.NULL_TIME_TRACKING_POLICY, contractException.getErrorType());

		// if the resource tracking policy was previously assigned
		// ResourceError#DUPLICATE_TIME_TRACKING_POLICY_ASSIGNMENT
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().setResourceTimeTracking(resourceId, timeTrackingPolicy).setResourceTimeTracking(resourceId, timeTrackingPolicy);
		});
		assertEquals(ResourceError.DUPLICATE_TIME_TRACKING_POLICY_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyDefinition", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {
		// 1866861448895276970L

		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceInitialData.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId());
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE);
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {
		// 7475098698397765251L
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {

			Set<TestResourcePropertyId> expectedResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			Set<TestResourcePropertyId> actualResourcePropertyIds = resourceInitialData.getResourcePropertyIds(testResourceId);
			assertEquals(expectedResourcePropertyIds, actualResourcePropertyIds);
		}

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyIds(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyIds(TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1876340540126853882L);

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		ResourceInitialData.Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				expectedValues.put(multiKey, propertyDefinition.getDefaultValue().get());
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedValues.put(multiKey, value);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourceInitialData.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// precondition tests

		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyValue(null, resourcePropertyId));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyValue(resourceId, null));
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyValue(resourceId, TestResourcePropertyId.getUnknownResourcePropertyId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getResourcePropertyValue(resourceId, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonResourceLevel", args = { PersonId.class, ResourceId.class })
	public void testGetPersonResourceLevel() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2902745806851600371L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			people.add(new PersonId(randomGenerator.nextInt()));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(personId, testResourceId);
				MutableInteger mutableInteger = new MutableInteger();
				expectedValues.put(multiKey, mutableInteger);
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					mutableInteger.setValue(amount);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(personId, testResourceId);
				MutableInteger mutableInteger = expectedValues.get(multiKey);
				int expectedAmount = mutableInteger.getValue();
				Long personResourceLevel = resourceInitialData.getPersonResourceLevel(personId, testResourceId);
				assertEquals(expectedAmount, personResourceLevel);
			}
		}

		// precondition tests
		PersonId personId = new PersonId(0);
		ResourceId resourceId = TestResourceId.RESOURCE_5;

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> resourceInitialData.getPersonResourceLevel(null, resourceId));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getPersonResourceLevel(personId, null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getPersonResourceLevel(personId, TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getResourceIds", args = {})
	public void testGetResourceIds() {
		
		Builder builder = ResourceInitialData.builder();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			expectedResourceIds.add(testResourceId);
		}
		ResourceInitialData resourceInitialData = builder.build();
		assertEquals(expectedResourceIds, resourceInitialData.getResourceIds());

	}

	@Test
	@UnitTestMethod(name = "getRegionResourceLevel", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceLevel() {
	

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6794457915874374469L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
				MutableInteger mutableInteger = new MutableInteger();
				expectedValues.put(multiKey, mutableInteger);
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);
					mutableInteger.setValue(amount);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
				MutableInteger mutableInteger = expectedValues.get(multiKey);
				int expectedAmount = mutableInteger.getValue();
				Long regionResourceLevel = resourceInitialData.getRegionResourceLevel(testRegionId, testResourceId);
				assertEquals(expectedAmount, regionResourceLevel);
			}
		}

		// precondition tests
		RegionId regionId = TestRegionId.REGION_3;
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> resourceInitialData.getRegionResourceLevel(null, resourceId));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());


		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getRegionResourceLevel(regionId, null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData.getRegionResourceLevel(regionId, TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());


	
	}

	@Test
	@UnitTestMethod(name = "getPersonResourceTimeTrackingPolicy", args = { ResourceId.class })
	public void testGetPersonResourceTimeTrackingPolicy() {
		

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2435473102993121457L);

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		ResourceInitialData.Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				expectedValues.put(multiKey, propertyDefinition.getDefaultValue().get());
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedValues.put(multiKey, value);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourceInitialData.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// precondition tests
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(null, resourcePropertyId, 5));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null</li>
		contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property value is null
		contractException = assertThrows(ContractException.class, () -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		// if the resource property value was previously assigned
		contractException = assertThrows(ContractException.class, () -> {
			ResourceInitialData.builder().setResourcePropertyValue(resourceId, resourcePropertyId, 5).setResourcePropertyValue(resourceId, resourcePropertyId, 5);
		});
		assertEquals(ResourceError.DUPLICATE_RESOURCE_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonIds", args = {})
	public void testGetPersonIds() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1188005474782684784L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			people.add(new PersonId(randomGenerator.nextInt()));
		}
		assertTrue(people.size() > 20);
		
		Set<PersonId> expectedPeople = new LinkedHashSet<>();
		
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(personId, testResourceId);
				MutableInteger mutableInteger = new MutableInteger();
				expectedValues.put(multiKey, mutableInteger);
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					expectedPeople.add(personId);
					mutableInteger.setValue(amount);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		assertEquals(expectedPeople, resourceInitialData.getPersonIds());

	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7912521358724932418L);

		// add the resources
		Builder builder = ResourceInitialData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId);
		}

		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);
					expectedRegionIds.add(testRegionId);
				}
			}
		}

		ResourceInitialData resourceInitialData = builder.build();

		assertEquals(expectedRegionIds, resourceInitialData.getRegionIds());
	}
}
