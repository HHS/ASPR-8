package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionFilter.class)
public class AT_RegionFilter {


	@Test
	@UnitTestConstructor(args = { Set.class })
	public void testConstructor() {
		RegionsActionSupport.testConsumer(100, 7513298944605144297L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			assertThrows(RuntimeException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));

		});

	}

	
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		RegionsActionSupport.testConsumer(100, 4278456048187470819L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

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
		RegionsActionSupport.testConsumer(100, 8908124836418429909L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : personDataManager.getPeople()) {
				boolean expected = regionDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_1) || regionDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_2);
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
}
