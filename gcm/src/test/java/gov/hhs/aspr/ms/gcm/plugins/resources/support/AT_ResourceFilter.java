package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.support.Equality;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.testsupport.TestPartitionsContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.testsupport.ResourcesTestPluginFactory;
import plugins.resources.testsupport.ResourcesTestPluginFactory.Factory;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.datamanagers.StochasticsDataManager;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_ResourceFilter {
	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		Factory factory = ResourcesTestPluginFactory.factory(12, 5802033011343021047L, (c) -> {
			Filter filter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.EQUAL, 12L);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonResourceUpdateEvent.class, filterSensitivity.getEventClass());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {

		Factory factory = ResourcesTestPluginFactory.factory(12, 6989281647149803633L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// if the equality operator is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> new ResourceFilter(TestResourceId.RESOURCE_1, null, 12L).validate(testPartitionsContext));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// ResourceError.NULL_RESOURCE_ID
			contractException = assertThrows(ContractException.class,
					() -> new ResourceFilter(null, Equality.GREATER_THAN, 12L).validate(testPartitionsContext));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// NucleusError.NULL_CONTEXT
			contractException = assertThrows(ContractException.class,
					() -> new ResourceFilter(TestResourceId.RESOURCE_1, Equality.GREATER_THAN, 12L).validate(null));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			// ResourceError.UNKNOWN_RESOURCE_ID
			contractException = assertThrows(ContractException.class,
					() -> new ResourceFilter(TestResourceId.getUnknownResourceId(), Equality.GREATER_THAN, 12L)
							.validate(testPartitionsContext));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "evaluate", args = { PartitionsContext.class,
			PersonId.class })
	public void testEvaluate() {

		Factory factory = ResourcesTestPluginFactory.factory(100, 5313696152098995059L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			Filter filter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.GREATER_THAN, 12L);

			for (PersonId personId : peopleDataManager.getPeople()) {
				long amount = randomGenerator.nextInt(10) + 7;
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_1, regionId, amount);
				resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.RESOURCE_1, personId, amount);
			}

			for (PersonId personId : peopleDataManager.getPeople()) {
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1,
						personId);
				boolean expected = personResourceLevel > 12L;
				boolean actual = filter.evaluate(testPartitionsContext, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(testPartitionsContext, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(testPartitionsContext, new PersonId(123412342)));
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestConstructor(target = ResourceFilter.class, args = { ResourceId.class, Equality.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "getResourceValue", args = {})
	public void testGetResourceValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6760936828235979053L);

		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextLong();
			ResourceFilter resourceFilter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.EQUAL, value);
			assertEquals(value, resourceFilter.getResourceValue());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "getResourceId", args = {})
	public void testGetResourceId() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			ResourceFilter resourceFilter = new ResourceFilter(testResourceId, Equality.EQUAL, 12L);
			assertEquals(testResourceId, resourceFilter.getResourceId());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "getEquality", args = {})
	public void testGetEquality() {
		for (Equality equality : Equality.values()) {
			ResourceFilter resourceFilter = new ResourceFilter(TestResourceId.RESOURCE_1, equality, 12L);
			assertEquals(equality, resourceFilter.getEquality());
		}
	}

	private ResourceFilter getRandomResourceFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		TestResourceId randomResourceId = TestResourceId.getRandomResourceId(randomGenerator);
		Equality randomEquality = Equality.getRandomEquality(randomGenerator);
		long value = randomGenerator.nextLong();
		ResourceFilter result = new ResourceFilter(randomResourceId, randomEquality, value);
		return result;
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7631979699053748572L);

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ResourceFilter resourceFilter = getRandomResourceFilter(randomGenerator.nextLong());
			assertFalse(resourceFilter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ResourceFilter resourceFilter = getRandomResourceFilter(randomGenerator.nextLong());
			assertTrue(resourceFilter.equals(resourceFilter));
		}
		
		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceFilter resourceFilter1 = getRandomResourceFilter(seed);
			ResourceFilter resourceFilter2 = getRandomResourceFilter(seed);
			assertTrue(resourceFilter1.equals(resourceFilter2));
			assertTrue(resourceFilter2.equals(resourceFilter1));
		}
		
		//different inputs yield non-equal objects
		for (int i = 0; i < 30; i++) {			
			ResourceFilter resourceFilter1 = getRandomResourceFilter(randomGenerator.nextLong());
			ResourceFilter resourceFilter2 = getRandomResourceFilter(randomGenerator.nextLong());
			assertNotEquals(resourceFilter1, resourceFilter2);			
		}		

	}
	
	
	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(428701786790006362L);
		
		//equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceFilter resourceFilter1 = getRandomResourceFilter(seed);
			ResourceFilter resourceFilter2 = getRandomResourceFilter(seed);
			assertEquals(resourceFilter1,resourceFilter2);
			assertEquals(resourceFilter1.hashCode(),resourceFilter2.hashCode());
		}
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ResourceFilter resourceFilter = getRandomResourceFilter(randomGenerator.nextLong());
			hashCodes.add(resourceFilter.hashCode());
		}
		
		assertTrue(hashCodes.size()>90);
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "toString", args = {})
	public void testToString() {
		ResourceFilter resourceFilter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.EQUAL, 15L);
		String expectedValue = "ResourceFilter [resourceId=RESOURCE_1, resourceValue=15, equality=EQUAL]";
		String actualValue = resourceFilter.toString();
		assertEquals(expectedValue, actualValue);
	}

}
