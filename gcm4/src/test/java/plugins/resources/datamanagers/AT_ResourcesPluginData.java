package plugins.resources.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public final class AT_ResourcesPluginData {

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getPersonResourceTimes", args = {})
	public void testGetPersonResourceTimes() {
		Map<ResourceId, List<Double>> expectedResourceTimes = new LinkedHashMap<>();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2121375123528875466L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, true);
		}

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		int id = 0;
		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					double time = randomGenerator.nextDouble();
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					builder.setPersonResourceTime(personId, testResourceId, time);
					List<Double> list = expectedResourceTimes.get(testResourceId);
					if (list == null) {
						list = new ArrayList<>();
						expectedResourceTimes.put(testResourceId, list);
					}
					while (list.size() < personId.getValue()) {
						list.add(null);
					}
					list.add(time);
				}
			}
		}

		ResourcesPluginData resourcesPluginData = builder.build();

		Map<ResourceId, List<Double>> actualResourceTimes = resourcesPluginData.getPersonResourceTimes();

		assertEquals(expectedResourceTimes, actualResourceTimes);

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getPersonResourceTimes", args = { ResourceId.class })
	public void testGetPersonResourceTimes_ResourceId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2121375123528875466L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, true);
		}

		Set<MultiKey> expectedValues = new LinkedHashSet<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		int id = 0;
		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					double time = randomGenerator.nextDouble();
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					builder.setPersonResourceTime(personId, testResourceId, time);
					MultiKey multiKey = new MultiKey(personId, testResourceId, time);
					expectedValues.add(multiKey);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			List<Double> personResourceTimes = resourceInitialData.getPersonResourceTimes(testResourceId);
			for (int i = 0; i < personResourceTimes.size(); i++) {
				Double time = personResourceTimes.get(i);
				if (time != null) {
					MultiKey multiKey = new MultiKey(new PersonId(i), testResourceId, time);
					actualValues.add(multiKey);
				}
			}
		}

		assertEquals(expectedValues, actualValues);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getPersonResourceTimes(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourceDefaultTimes", args = {})
	public void testGetResourceDefaultTimes() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9133618222677631125L);

		Map<ResourceId, Double> expectedResourceDefaultTimes = new LinkedHashMap<>();

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			double time = randomGenerator.nextDouble();
			builder.addResource(testResourceId, time, false);
			expectedResourceDefaultTimes.put(testResourceId, time);
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
			Double time = resourceInitialData.getResourceDefaultTime(resourceId);
			actualValues.add(new MultiKey(resourceId, time));
		}

		Map<ResourceId, Double> actualResourceDefaultTimes = resourceInitialData.getResourceDefaultTimes();
		assertEquals(expectedResourceDefaultTimes, actualResourceDefaultTimes);
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourceDefaultTime", args = { ResourceId.class })
	public void testGetResourceDefaultTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9133618222677631125L);
		Set<MultiKey> expectedValues = new LinkedHashSet<>();
		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			double time = randomGenerator.nextDouble();
			builder.addResource(testResourceId, time, false);
			expectedValues.add(new MultiKey(testResourceId, time));
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
			Double time = resourceInitialData.getResourceDefaultTime(resourceId);
			actualValues.add(new MultiKey(resourceId, time));
		}

		assertEquals(expectedValues, actualValues);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourceDefaultTime(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7567353570953948981L);

		// equal objects have equal hash codes
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			ResourcesPluginData rpd1 = getRandomResourcesPluginData(seed);
			ResourcesPluginData rpd2 = getRandomResourcesPluginData(seed);

			assertEquals(rpd1, rpd2);
			assertEquals(rpd1.hashCode(), rpd2.hashCode());
		}
		int count = 100;
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < count; i++) {
			long seed = randomGenerator.nextLong();
			ResourcesPluginData rpd = getRandomResourcesPluginData(seed);
			hashCodes.add(rpd.hashCode());
		}
		int minimumCount = count * 9 / 10;
		assertTrue(hashCodes.size() > minimumCount);
	}

	/*
	 * Returns a randomly generated resources plugin data
	 */
	private ResourcesPluginData getRandomResourcesPluginData(long seed) {
		// equal objects have equal hashCodes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		ResourcesPluginData.Builder pluginDataBuilder = ResourcesPluginData.builder();

		Set<TestResourceId> selectedTestResourceIds = new LinkedHashSet<>();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			if (randomGenerator.nextDouble() < 0.8) {
				selectedTestResourceIds.add(testResourceId);
			}
		}

		Set<TestResourceId> timeTrackedResourceIds = new LinkedHashSet<>();

		for (TestResourceId testResourceId : selectedTestResourceIds) {
			double time = randomGenerator.nextDouble();
			boolean timeTrackingPolicy = randomGenerator.nextBoolean();
			if (timeTrackingPolicy) {
				timeTrackedResourceIds.add(testResourceId);
			}
			pluginDataBuilder.addResource(testResourceId, time, timeTrackingPolicy);
		}

		Set<TestResourcePropertyId> selectedTestResourcePropertyIds = new LinkedHashSet<>();

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			if (selectedTestResourceIds.contains(testResourceId)) {
				if (randomGenerator.nextDouble() < 0.8) {
					selectedTestResourcePropertyIds.add(testResourcePropertyId);
				}
			}
		}

		for (TestResourcePropertyId testResourcePropertyId : selectedTestResourcePropertyIds) {
			pluginDataBuilder.defineResourceProperty(testResourcePropertyId.getTestResourceId(), testResourcePropertyId,
					testResourcePropertyId.getPropertyDefinition());
		}

		for (TestResourcePropertyId testResourcePropertyId : selectedTestResourcePropertyIds) {
			if (randomGenerator.nextBoolean()) {
				pluginDataBuilder.setResourcePropertyValue(testResourcePropertyId.getTestResourceId(),
						testResourcePropertyId, testResourcePropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		Set<TestRegionId> selectedRegionIds = new LinkedHashSet<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			if (randomGenerator.nextDouble() < 0.8) {
				selectedRegionIds.add(testRegionId);
			}
		}

		for (TestRegionId testRegionId : selectedRegionIds) {
			for (TestResourceId testResourceId : selectedTestResourceIds) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(1000);
					pluginDataBuilder.setRegionResourceLevel(testRegionId, testResourceId, value);
				}
			}
		}

		int personCount = randomGenerator.nextInt(5) + 5;

		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i * i);
			for (TestResourceId testResourceId : selectedTestResourceIds) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(5);
					pluginDataBuilder.setPersonResourceLevel(personId, testResourceId, value);
				}
				if (timeTrackedResourceIds.contains(testResourceId) && randomGenerator.nextBoolean()) {
					double time = randomGenerator.nextDouble() + 1.0;
					pluginDataBuilder.setPersonResourceTime(personId, testResourceId, time);
				}
			}
		}

		return pluginDataBuilder.build();
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1110078478105073449L);

		// null equality
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			ResourcesPluginData rpd = getRandomResourcesPluginData(seed);
			assertFalse(rpd.equals(null));
		}

		// reflexivity
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			ResourcesPluginData rpd = getRandomResourcesPluginData(seed);
			assertEquals(rpd, rpd);
		}

		// symmetry
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			ResourcesPluginData rpd1 = getRandomResourcesPluginData(seed);
			ResourcesPluginData rpd2 = getRandomResourcesPluginData(seed);
			assertEquals(rpd1, rpd2);
			assertEquals(rpd2, rpd1);
		}

		// transitivity -- implied by symmetry

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "setPersonResourceTime", args = { PersonId.class,
			ResourceId.class, Double.class })
	public void testSetPersonResourceTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2121375123528875466L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, true);
		}

		Set<MultiKey> expectedValues = new LinkedHashSet<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		int id = 0;
		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					double time = randomGenerator.nextDouble();
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					builder.setPersonResourceTime(personId, testResourceId, time);
					MultiKey multiKey = new MultiKey(personId, testResourceId, time);
					expectedValues.add(multiKey);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			List<Double> personResourceTimes = resourceInitialData.getPersonResourceTimes(testResourceId);
			for (int i = 0; i < personResourceTimes.size(); i++) {
				Double time = personResourceTimes.get(i);
				if (time != null) {
					MultiKey multiKey = new MultiKey(new PersonId(i), testResourceId, time);
					actualValues.add(multiKey);
				}
			}
		}

		assertEquals(expectedValues, actualValues);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> ResourcesPluginData.builder().setPersonResourceTime(null, TestResourceId.RESOURCE_1, 0.0));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the resource id is null
		contractException = assertThrows(ContractException.class,
				() -> ResourcesPluginData.builder().setPersonResourceTime(new PersonId(0), null, 0.0));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// precondition test: if the time is null
		contractException = assertThrows(ContractException.class, () -> ResourcesPluginData.builder()
				.setPersonResourceTime(new PersonId(0), TestResourceId.RESOURCE_1, null));
		assertEquals(ResourceError.NULL_TIME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ResourcesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		assertNotNull(ResourcesPluginData.builder().build());

		// precondition tests

		/*
		 * if a resource property definition was collected for a resource that was not
		 * added
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE
					.getPropertyDefinition();
			ResourcesPluginData.builder().defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition)
					.build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource that was not added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			Boolean value = false;
			ResourcesPluginData.builder().setResourcePropertyValue(resourceId, resourcePropertyId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource property that is
		 * not associated with the given resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			Boolean value = false;
			ResourcesPluginData.builder()//
					.addResource(resourceId, 0.0, false)//
					.setResourcePropertyValue(resourceId, resourcePropertyId, value)//
					.build();//
		});//
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a resource property value was collected for a resource property that is
		 * not compatible with the associated resource property definition
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE
					.getPropertyDefinition();
			Integer value = 5;
			ResourcesPluginData.builder()//
					.addResource(resourceId, 0.0, false)//
					.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition)//
					.setResourcePropertyValue(resourceId, resourcePropertyId, value)//
					.build();
		});//
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a resource property definition has a null default value and there is no
		 * assigned resource property value for that resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
			ResourcesPluginData.builder()//
					.addResource(resourceId, 0.0, false)//
					.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition)//
					.build();
		});//
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
		/*
		 * if a resource level was collected for a person that is an unknown resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			Long value = 566L;
			ResourcesPluginData.builder().setPersonResourceLevel(new PersonId(0), resourceId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/*
		 * if a resource level was collected for a region that is an unknown resource id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			Long value = 566L;
			ResourcesPluginData.builder().setRegionResourceLevel(TestRegionId.REGION_1, resourceId, value).build();
		});//
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "addResource", args = { ResourceId.class,
			Double.class, boolean.class })
	public void testAddResource() {

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {

			// replacing data to show that the value persists
			builder.addResource(testResourceId, 0.0, false);
			// adding duplicate data to show that the value persists
			builder.addResource(testResourceId, 0.0, false);
			expectedResourceIds.add(testResourceId);
		}
		ResourcesPluginData resourceInitialData = builder.build();
		assertEquals(expectedResourceIds, resourceInitialData.getResourceIds());

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> ResourcesPluginData.builder().addResource(null, 0.0, false));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "defineResourceProperty", args = {
			ResourceId.class, ResourcePropertyId.class, PropertyDefinition.class })
	public void testDefineResourceProperty() {

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition propertyDefinition2 = testResourcePropertyId.next().getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition2);
				// replacing data to show that the value persists
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				// adding duplicate data to show that values persist
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceInitialData
						.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		// precondition tests

		ResourceId resourceId = TestResourceId.RESOURCE_2;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;
		PropertyDefinition propertyDefinition = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE
				.getPropertyDefinition();

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourcesPluginData.builder().defineResourceProperty(null, resourcePropertyId, propertyDefinition);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class, () -> {
			ResourcesPluginData.builder().defineResourceProperty(resourceId, null, propertyDefinition);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> {
			ResourcesPluginData.builder().defineResourceProperty(resourceId, resourcePropertyId, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "setPersonResourceLevel", args = {
			PersonId.class, ResourceId.class, long.class })
	public void testSetPersonResourceLevel() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6539895160899665826L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		Set<MultiKey> expectedValues = new LinkedHashSet<>();

		// add up to 30 people
		int id = 0;
		Set<PersonId> people = new LinkedHashSet<>();

		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount + 1);
					// replacing data to show that the value persists
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					// adding duplicate data to show that the value persists
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					MultiKey multiKey = new MultiKey(personId, testResourceId, amount);
					expectedValues.add(multiKey);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			List<Long> personResourceLevels = resourceInitialData.getPersonResourceLevels(testResourceId);
			for (int i = 0; i < personResourceLevels.size(); i++) {
				Long amount = personResourceLevels.get(i);
				if (amount != null) {
					MultiKey multiKey = new MultiKey(new PersonId(i), testResourceId, amount);
					actualValues.add(multiKey);
				}
			}
		}

		assertEquals(expectedValues, actualValues);

		// precondition tests
		PersonId personId = new PersonId(0);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 678;

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.setPersonResourceLevel(null, resourceId, amount));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class,
				() -> builder.setPersonResourceLevel(personId, null, amount));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class,
				() -> builder.setPersonResourceLevel(personId, resourceId, -5L));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "setRegionResourceLevel", args = {
			RegionId.class, ResourceId.class, long.class })
	public void testSetRegionResourceLevel() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8877834706249831995L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		Map<MultiKey, Long> expectedValues = new LinkedHashMap<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);
					MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
					expectedValues.put(multiKey, amount);
				}
			}
		}

		Map<MultiKey, Long> actualValues = new LinkedHashMap<>();

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestRegionId testRegionId : TestRegionId.values()) {

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Optional<Long> optional = resourceInitialData.getRegionResourceLevel(testRegionId, testResourceId);
				if (optional.isPresent()) {
					Long amount = optional.get();
					MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
					actualValues.put(multiKey, amount);
				}
			}

		}

		assertEquals(expectedValues, actualValues);

		// precondition tests
		RegionId regionId = TestRegionId.REGION_3;
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 678;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.setRegionResourceLevel(null, resourceId, amount));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class,
				() -> builder.setRegionResourceLevel(regionId, null, amount));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class,
				() -> builder.setRegionResourceLevel(regionId, resourceId, -5L));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.Builder.class, name = "setResourcePropertyValue", args = {
			ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7516798209205913252L);

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					Object value2 = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					if (value instanceof Boolean) {
						value2 = !(Boolean) value;
					}
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value2);
					// replacing data to show that the value persists
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					// adding duplicate data to show that the value persists
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedValues.put(multiKey, value);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				Optional<Object> optional = resourceInitialData.getResourcePropertyValue(testResourceId,
						testResourcePropertyId);
				if (expectedValue == null) {
					assertTrue(optional.isEmpty());
				} else {
					assertEquals(expectedValue, optional.get());
				}

			}
		}

		// precondition tests
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.setResourcePropertyValue(null, resourcePropertyId, 5));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null</li>
		contractException = assertThrows(ContractException.class,
				() -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the resource property value is null
		contractException = assertThrows(ContractException.class,
				() -> builder.setResourcePropertyValue(resourceId, null, 5));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourcePropertyDefinitions", args = {})
	public void testGetResourcePropertyDefinitions() {

		Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> expectedDefinitions = new LinkedHashMap<>();

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				Map<ResourcePropertyId, PropertyDefinition> map = expectedDefinitions.get(testResourceId);
				if (map == null) {
					map = new LinkedHashMap<>();
					expectedDefinitions.put(testResourceId, map);
				}
				map.put(testResourcePropertyId, propertyDefinition);
			}
		}
		ResourcesPluginData resourcesPluginData = builder.build();

		Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> actualDefinitions = resourcesPluginData
				.getResourcePropertyDefinitions();

		assertEquals(expectedDefinitions, actualDefinitions);
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourcePropertyDefinition", args = {
			ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceInitialData
						.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(null,
					TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.getUnknownResourceId(),
					TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the resource property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.getUnknownResourcePropertyId());
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			resourceInitialData.getResourcePropertyDefinition(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE);
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {
		// 7475098698397765251L
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {

			Set<TestResourcePropertyId> expectedResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			Set<TestResourcePropertyId> actualResourcePropertyIds = resourceInitialData
					.getResourcePropertyIds(testResourceId);
			assertEquals(expectedResourcePropertyIds, actualResourcePropertyIds);
		}

		// precondition tests

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourcePropertyIds(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourcePropertyIds(TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourcePropertyValues", args = {})
	public void testGetResourcePropertyValues() {

		Map<ResourceId, Map<ResourcePropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6566464377962300906L);

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					Map<ResourcePropertyId, Object> map = expectedPropertyValues.get(testResourceId);
					if (map == null) {
						map = new LinkedHashMap<>();
						expectedPropertyValues.put(testResourceId, map);
					}
					map.put(testResourcePropertyId, value);
				}
			}
		}

		ResourcesPluginData resourcesPluginData = builder.build();
		Map<ResourceId, Map<ResourcePropertyId, Object>> actualPropertyValues = resourcesPluginData
				.getResourcePropertyValues();

		assertEquals(expectedPropertyValues, actualPropertyValues);
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourcePropertyValue", args = { ResourceId.class,
			ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1876340540126853882L);

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
				builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
				if (randomGenerator.nextBoolean()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedValues.put(multiKey, value);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
					.getTestResourcePropertyIds(testResourceId);
			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				MultiKey multiKey = new MultiKey(testResourceId, testResourcePropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				Optional<Object> optional = resourceInitialData.getResourcePropertyValue(testResourceId,
						testResourcePropertyId);
				if (expectedValue == null) {
					assertTrue(optional.isEmpty());
				} else {
					assertEquals(expectedValue, optional.get());
				}
			}
		}

		// precondition tests

		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE;

		// if the resource id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourcePropertyValue(null, resourcePropertyId));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData
				.getResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		// if the resource property id is null
		contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourcePropertyValue(resourceId, null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the resource property id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData
				.getResourcePropertyValue(resourceId, TestResourcePropertyId.getUnknownResourcePropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> resourceInitialData
				.getResourcePropertyValue(resourceId, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getPersonResourceLevels", args = {})
	public void testGetPersonResourceLevels() {
		Map<ResourceId, List<Long>> expectedResourceLevels = new LinkedHashMap<>();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(579338638644505479L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		int id = 0;
		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {

				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					List<Long> list = expectedResourceLevels.get(testResourceId);
					if (list == null) {
						list = new ArrayList<>();
						expectedResourceLevels.put(testResourceId, list);
					}
					while (list.size() < personId.getValue()) {
						list.add(null);
					}
					list.add(amount);
				}
			}
		}

		ResourcesPluginData resourcesPluginData = builder.build();

		Map<ResourceId, List<Long>> actualResourceLevels = resourcesPluginData.getPersonResourceLevels();
		assertEquals(expectedResourceLevels, actualResourceLevels);
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getPersonResourceLevels", args = { ResourceId.class })
	public void testGetPersonResourceLevels_ResourceId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2902745806851600371L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		Set<MultiKey> expectedValues = new LinkedHashSet<>();

		// add up to 30 people
		Set<PersonId> people = new LinkedHashSet<>();
		int id = 0;
		for (int i = 0; i < 30; i++) {
			id += randomGenerator.nextInt(3) + 1;
			people.add(new PersonId(id));
		}
		assertTrue(people.size() > 20);
		for (PersonId personId : people) {
			for (TestResourceId testResourceId : TestResourceId.values()) {

				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setPersonResourceLevel(personId, testResourceId, amount);
					MultiKey multiKey = new MultiKey(personId, testResourceId, amount);
					expectedValues.add(multiKey);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Set<MultiKey> actualValues = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			List<Long> personResourceLevels = resourceInitialData.getPersonResourceLevels(testResourceId);
			for (int i = 0; i < personResourceLevels.size(); i++) {
				Long amount = personResourceLevels.get(i);
				if (amount != null) {
					MultiKey multiKey = new MultiKey(new PersonId(i), testResourceId, amount);
					actualValues.add(multiKey);
				}
			}
		}

		assertEquals(expectedValues, actualValues);

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getPersonResourceLevels(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
			expectedResourceIds.add(testResourceId);
		}
		ResourcesPluginData resourceInitialData = builder.build();
		assertEquals(expectedResourceIds, resourceInitialData.getResourceIds());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getRegionResourceLevels", args = {})
	public void testGetRegionResourceLevels() {
		Map<RegionId, Map<ResourceId, Long>> expectedResourceLevels = new LinkedHashMap<>();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6794457915874374469L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);

					Map<ResourceId, Long> map = expectedResourceLevels.get(testRegionId);
					if (map == null) {
						map = new LinkedHashMap<>();
						expectedResourceLevels.put(testRegionId, map);
					}
					map.put(testResourceId, amount);
				}
			}
		}

		ResourcesPluginData resourceInitialData = builder.build();

		Map<RegionId, Map<ResourceId, Long>> actualResourceLevels = resourceInitialData.getRegionResourceLevels();

		assertEquals(expectedResourceLevels, actualResourceLevels);
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getRegionResourceLevel", args = { RegionId.class,
			ResourceId.class })
	public void testGetRegionResourceLevel() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6794457915874374469L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
		}

		Map<MultiKey, Long> expectedValues = new LinkedHashMap<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(10);
					builder.setRegionResourceLevel(testRegionId, testResourceId, amount);
					MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
					expectedValues.put(multiKey, amount);
				}
			}
		}

		Map<MultiKey, Long> actualValues = new LinkedHashMap<>();

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestRegionId testRegionId : TestRegionId.values()) {

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Optional<Long> optional = resourceInitialData.getRegionResourceLevel(testRegionId, testResourceId);
				if (optional.isPresent()) {
					Long amount = optional.get();
					MultiKey multiKey = new MultiKey(testRegionId, testResourceId);
					actualValues.put(multiKey, amount);
				}
			}

		}

		assertEquals(expectedValues, actualValues);

		// precondition test: if the region id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getRegionResourceLevel(null, TestResourceId.RESOURCE_1));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the resource id is null
		contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getRegionResourceLevel(TestRegionId.REGION_1, null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// precondition test: if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> resourceInitialData
				.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourceTimeTrackingPolicies", args = {})
	public void testGetResourceTimeTrackingPolicies() {

		Map<ResourceId, Boolean> expectedPolicies = new LinkedHashMap<>();

		boolean track = true;
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, track);
			expectedPolicies.put(testResourceId, track);
			track = !track;
		}

		ResourcesPluginData resourcesPluginData = builder.build();

		Map<ResourceId, Boolean> actualPolicies = resourcesPluginData.getResourceTimeTrackingPolicies();
		assertEquals(expectedPolicies, actualPolicies);

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getResourceTimeTrackingPolicy", args = {
			ResourceId.class })
	public void testGetResourceTimeTrackingPolicy() {

		Map<TestResourceId, Boolean> expectedValues = new LinkedHashMap<>();

		boolean track = true;
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, track);
			expectedValues.put(testResourceId, track);
			track = !track;
		}

		ResourcesPluginData resourceInitialData = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			boolean expectectedValue = expectedValues.get(testResourceId);
			boolean actualValue = resourceInitialData.getResourceTimeTrackingPolicy(testResourceId);
			assertEquals(expectectedValue, actualValue);

		}

		// precondition test: if the resource id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourceTimeTrackingPolicy(null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// precondition test: if the resource id is unknown
		contractException = assertThrows(ContractException.class,
				() -> resourceInitialData.getResourceTimeTrackingPolicy(TestResourceId.getUnknownResourceId()));
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getRegionIds", args = {})
	public void testGetRegionIds() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7912521358724932418L);

		// add the resources
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.addResource(testResourceId, 0.0, false);
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

		ResourcesPluginData resourceInitialData = builder.build();

		assertEquals(expectedRegionIds, resourceInitialData.getRegionIds());
	}

	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7644775230297230691L);
		ResourcesPluginData.Builder pluginDataBuilder = ResourcesPluginData.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			double time = randomGenerator.nextDouble();
			boolean timeTrackingPolicy = testResourceId.getTimeTrackingPolicy();
			pluginDataBuilder.addResource(testResourceId, time, timeTrackingPolicy);
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			pluginDataBuilder.defineResourceProperty(testResourcePropertyId.getTestResourceId(), testResourcePropertyId,
					testResourcePropertyId.getPropertyDefinition());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			if (randomGenerator.nextBoolean()) {
				pluginDataBuilder.setResourcePropertyValue(testResourcePropertyId.getTestResourceId(),
						testResourcePropertyId, testResourcePropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(1000);
					pluginDataBuilder.setRegionResourceLevel(testRegionId, testResourceId, value);
				}
			}
		}

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i * i);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(5);
					double time = randomGenerator.nextDouble() + 1.0;
					pluginDataBuilder.setPersonResourceLevel(personId, testResourceId, value);
					if (testResourceId.getTimeTrackingPolicy()) {
						pluginDataBuilder.setPersonResourceTime(personId, testResourceId, time);
					}
				}
			}
		}

		ResourcesPluginData resourcesPluginData = pluginDataBuilder.build();

		PluginData pluginData = resourcesPluginData.getCloneBuilder().build();

		// show that the plugin data is of the expected type
		assertTrue(pluginData instanceof ResourcesPluginData);

		ResourcesPluginData cloneResourcesPluginData = (ResourcesPluginData) pluginData;

		assertEquals(resourcesPluginData, cloneResourcesPluginData);

	}


	@Test
	@UnitTestMethod(target = ResourcesPluginData.class, name = "toString", args = {})
	public void testToString() {
		
		ResourcesPluginData randomResourcesPluginData = getRandomResourcesPluginData(3613301633594044660L);
		
		String actualValue = randomResourcesPluginData.toString();
		
		//The expected value was manually verified
		String expectedValue = "ResourcesPluginData [data=Data ["
				+ "resourceDefaultTimes={"
				+ "RESOURCE_1=0.12069246251184063, "
				+ "RESOURCE_3=0.5181842946303716, "
				+ "RESOURCE_4=0.9749466517405754, "
				+ "RESOURCE_5=0.7470658222016602}, "
				+ "resourceTimeTrackingPolicies={"
				+ "RESOURCE_1=false, "
				+ "RESOURCE_3=false, "
				+ "RESOURCE_4=true, "
				+ "RESOURCE_5=false}, "
				+ "resourcePropertyDefinitions={"
				+ "RESOURCE_1={"
				+ "ResourceProperty_1_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false], "
				+ "ResourceProperty_1_2_INTEGER_MUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=0]}, "
				+ "RESOURCE_3={"
				+ "ResourceProperty_3_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false]}, "
				+ "RESOURCE_4={"
				+ "ResourceProperty_4_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=true]}, "
				+ "RESOURCE_5={"
				+ "ResourceProperty_5_1_INTEGER_IMMUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false, defaultValue=7], "
				+ "ResourceProperty_5_2_DOUBLE_IMMUTABLE=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=false, defaultValue=2.7]}}, "
				+ "resourcePropertyValues={"
				+ "RESOURCE_3={ResourceProperty_3_1_BOOLEAN_MUTABLE=false}, "
				+ "RESOURCE_5={ResourceProperty_5_1_INTEGER_IMMUTABLE=-1328557948}}, "
				+ "personResourceLevels={"
				+ "RESOURCE_4=[1, 2, null, null, 0, null, null, null, null, 3, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1], "
				+ "RESOURCE_5=[4, 0, null, null, null, null, null, null, null, 2, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0], "
				+ "RESOURCE_1=[null, 0, null, null, 4, null, null, null, null, 2, null, null, null, null, null, null, 4, null, null, null, null, null, null, null, null, 4], "
				+ "RESOURCE_3=[null, null, null, null, null, null, null, null, null, 1, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 2]}, "
				+ "personResourceTimes={"
				+ "RESOURCE_4=[1.6021497784639966, null, null, null, 1.769887399263266, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1.8351982268522948, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1.913996434647729]}, "
				+ "regionResourceLevels={"
				+ "REGION_2={RESOURCE_3=7}, "
				+ "REGION_3={RESOURCE_3=404, RESOURCE_5=247}, "
				+ "REGION_4={RESOURCE_1=971, RESOURCE_5=565}, "
				+ "REGION_5={RESOURCE_1=930, RESOURCE_3=653}, "
				+ "REGION_6={RESOURCE_4=402}}]]";
		
		assertEquals(expectedValue, actualValue);
		
	}
}
