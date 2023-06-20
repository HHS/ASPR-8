package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.testsupport.TestPartitionsContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import plugins.regions.testsupport.TestRegionId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionFilter {

	@Test
	@UnitTestConstructor(target = RegionFilter.class, args = { RegionId[].class })
	public void testConstructorWithArray() {
		Factory factory = RegionsTestPluginFactory.factory(100, 4602637405159227338L, false, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(testPartitionsContext));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(testPartitionsContext));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestConstructor(target = RegionFilter.class, args = { Set.class })
	public void testConstructorWithSet() {
		Factory factory = RegionsTestPluginFactory.factory(100, 4602637405159227338L, false, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(testPartitionsContext));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(testPartitionsContext));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		Factory factory = RegionsTestPluginFactory.factory(100, 2916119612012950359L, false, (c) -> {

			Filter filter = new RegionFilter(TestRegionId.REGION_1);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonRegionUpdateEvent.class, filterSensitivity.getEventClass());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		Factory factory = RegionsTestPluginFactory.factory(100, 28072097989345652L, false, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : peopleDataManager.getPeople()) {
				boolean expected = regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_1) || regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_2);
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
	@UnitTestMethod(target = RegionFilter.class, name = "toString", args = {})
	public void testToString() {
		Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

		String expectedString = "RegionFilter [regionIds=[REGION_1, REGION_2]]";

		assertEquals(expectedString, filter.toString());
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		Factory factory = RegionsTestPluginFactory.factory(100, 28072097989345652L, false, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			assertDoesNotThrow(() -> filter.validate(testPartitionsContext));

			// precondition: null simulation context
			ContractException contractException = assertThrows(ContractException.class, () -> filter.validate(null));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			RegionId badRegion = null;
			// precondition: region id is null
			Filter badFilter1 = new RegionFilter(badRegion);
			contractException = assertThrows(ContractException.class, () -> badFilter1.validate(testPartitionsContext));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// precondition: region id is unknown
			Filter badFilter2 = new RegionFilter(TestRegionId.getUnknownRegionId());
			contractException = assertThrows(ContractException.class, () -> badFilter2.validate(testPartitionsContext));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
