package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import plugins.compartments.support.CompartmentFilter;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionFilter.class)
public class AT_RegionFilter {


	@Test
	@UnitTestConstructor(args = { SimulationContext.class, Set.class })
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

	/**
	 * Tests {@link CompartmentFilter#getFilterSensitivities()}
	 */
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		RegionsActionSupport.testConsumer(100, 4278456048187470819L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			Filter filter = new RegionFilter(TestRegionId.REGION_1);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonRegionChangeObservationEvent.class, filterSensitivity.getEventClass());
		});
	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		RegionsActionSupport.testConsumer(100, 8908124836418429909L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : personDataView.getPeople()) {
				boolean expected = regionLocationDataView.getPersonRegion(personId).equals(TestRegionId.REGION_1) || regionLocationDataView.getPersonRegion(personId).equals(TestRegionId.REGION_2);
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
