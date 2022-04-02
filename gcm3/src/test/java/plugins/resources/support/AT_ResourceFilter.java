package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.NucleusError;
import nucleus.util.ContractException;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.events.PersonResourceChangeObservationEvent;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;

/**
 * Test unit for {@link ResourceFilter}.
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ResourceFilter.class)
public class AT_ResourceFilter {
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		ResourcesActionSupport.testConsumer(12, 5802033011343021047L, (c) -> {
			Filter filter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.EQUAL, 12L);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonResourceChangeObservationEvent.class, filterSensitivity.getEventClass());

		});

	}
	
	@Test
	@UnitTestMethod(name = "validate", args = { Context.class })
	public void testValidate() {

		ResourcesActionSupport.testConsumer(12, 6989281647149803633L, (c) -> {
			// if the equality operator is null
			ContractException contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.RESOURCE_1, null, 12L).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// ResourceError.NULL_RESOURCE_ID
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(null, Equality.GREATER_THAN, 12L).validate(c));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// NucleusError.NULL_CONTEXT
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.RESOURCE_1, Equality.GREATER_THAN, 12L).validate(null));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			// ResourceError.UNKNOWN_RESOURCE_ID
			contractException = assertThrows(ContractException.class, () -> new ResourceFilter(TestResourceId.getUnknownResourceId(), Equality.GREATER_THAN, 12L).validate(c));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {

		ResourcesActionSupport.testConsumer(100, 5313696152098995059L, (c) -> {
			ResourceDataManager resourceDataManager = c.getDataManager(ResourceDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			Filter filter = new ResourceFilter(TestResourceId.RESOURCE_1, Equality.GREATER_THAN, 12L);

			for (PersonId personId : personDataManager.getPeople()) {
				long amount = randomGenerator.nextInt(10) + 7;
				RegionId regionId = regionDataManager.getPersonRegion(personId);
				resourceDataManager.addResourceToRegion(TestResourceId.RESOURCE_1, regionId, amount);
				resourceDataManager.transferResourceToPersonFromRegion(TestResourceId.RESOURCE_1, personId, amount);				
			}

			for (PersonId personId : personDataManager.getPeople()) {
				long personResourceLevel = resourceDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, personId);
				boolean expected = personResourceLevel > 12L;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));
		});

	}

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

}
