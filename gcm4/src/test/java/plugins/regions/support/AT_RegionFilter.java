package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = RegionFilter.class)
public class AT_RegionFilter {

	@Test
	@UnitTestConstructor(args = { RegionId[].class })
	public void testConstructorWithArray() {
		RegionsActionSupport.testConsumer(100, 4602637405159227338L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestConstructor(args = { Set.class })
	public void testConstructorWithSet() {
		RegionsActionSupport.testConsumer(100, 4602637405159227338L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		RegionsActionSupport.testConsumer(100, 2916119612012950359L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			Filter filter = new RegionFilter(TestRegionId.REGION_1);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonRegionUpdateEvent.class, filterSensitivity.getEventClass());
		});
	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		RegionsActionSupport.testConsumer(100, 28072097989345652L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : peopleDataManager.getPeople()) {
				boolean expected = regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_1)
						|| regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_2);
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
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

		String expectedString = "RegionFilter [regionIds=[REGION_1, REGION_2]]";

		assertEquals(expectedString, filter.toString());
	}

	@Test
	@UnitTestMethod(name = "validate", args = { SimulationContext.class })
	public void testValidate() {
		RegionsActionSupport.testConsumer(100, 28072097989345652L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
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
		});
	}
}
