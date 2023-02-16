package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.TestRegionId;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionFilter {

	@Test
	@UnitTestConstructor(target = RegionFilter.class, args = { RegionId[].class })
	public void testConstructorWithArray() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(100, 4602637405159227338L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		}).getPlugins());

	}

	@Test
	@UnitTestConstructor(target = RegionFilter.class, args = { Set.class })
	public void testConstructorWithSet() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(100, 4602637405159227338L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(100, 2916119612012950359L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			Filter filter = new RegionFilter(TestRegionId.REGION_1);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonRegionUpdateEvent.class, filterSensitivity.getEventClass());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(100, 28072097989345652L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : peopleDataManager.getPeople()) {
				boolean expected = regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_1) || regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_2);
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));

		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "toString", args = {})
	public void testToString() {
		Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

		String expectedString = "RegionFilter [regionIds=[REGION_1, REGION_2]]";

		assertEquals(expectedString, filter.toString());
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "validate", args = { SimulationContext.class })
	public void testValidate() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(100, 28072097989345652L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			assertDoesNotThrow(() -> filter.validate(c));

			// precondition: null simulation context
			ContractException contractException = assertThrows(ContractException.class, () -> filter.validate(null));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			RegionId badRegion = null;
			// precondition: region id is null
			Filter badFilter1 = new RegionFilter(badRegion);
			contractException = assertThrows(ContractException.class, () -> badFilter1.validate(c));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// precondition: region id is unknown
			Filter badFilter2 = new RegionFilter(TestRegionId.getUnknownRegionId());
			contractException = assertThrows(ContractException.class, () -> badFilter2.validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());
	}
}
