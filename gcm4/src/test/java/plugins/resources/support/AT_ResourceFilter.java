package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import plugins.stochastics.datamanagers.StochasticsDataManager;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

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
			ContractException contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.RESOURCE_1, null, 12L).validate(testPartitionsContext));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// ResourceError.NULL_RESOURCE_ID
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(null, Equality.GREATER_THAN, 12L).validate(testPartitionsContext));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// NucleusError.NULL_CONTEXT
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.RESOURCE_1, Equality.GREATER_THAN, 12L).validate(null));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			// ResourceError.UNKNOWN_RESOURCE_ID
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.getUnknownResourceId(), Equality.GREATER_THAN, 12L).validate(testPartitionsContext));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourceFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
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
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, personId);
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

}
