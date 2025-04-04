package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events.PersonRegionUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.RegionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

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
			ContractException contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(testPartitionsContext));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(null, TestRegionId.REGION_1).validate(testPartitionsContext));
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
			ContractException contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(testPartitionsContext));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// precondition: null region id
			contractException = assertThrows(ContractException.class,
					() -> new RegionFilter(null, TestRegionId.REGION_1).validate(testPartitionsContext));
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
				boolean expected = regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_1)
						|| regionsDataManager.getPersonRegion(personId).equals(TestRegionId.REGION_2);
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

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "getRegionIds", args = {})
	public void testGetRegionIds() {

		// test against both constructors
		RegionFilter regionFilter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_3,
				TestRegionId.REGION_6);
		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		expectedRegionIds.add(TestRegionId.REGION_1);
		expectedRegionIds.add(TestRegionId.REGION_3);
		expectedRegionIds.add(TestRegionId.REGION_6);
		Set<RegionId> actualRegionIds = regionFilter.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);

		regionFilter = new RegionFilter(expectedRegionIds);
		actualRegionIds = regionFilter.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);
	}

	private RegionFilter getRandomRegionFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		int count = randomGenerator.nextInt(10) + 1;
		Set<RegionId> selectedRegions = new LinkedHashSet<>();

		while (selectedRegions.size() < count) {
			SimpleRegionId simpleRegionId = new SimpleRegionId(randomGenerator.nextInt());
			selectedRegions.add(simpleRegionId);
		}

		return new RegionFilter(selectedRegions);
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8665861319201143941L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			RegionFilter regionFilter = getRandomRegionFilter(randomGenerator.nextLong());
			assertFalse(regionFilter.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			RegionFilter regionFilter = getRandomRegionFilter(randomGenerator.nextLong());
			assertFalse(regionFilter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RegionFilter regionFilter = getRandomRegionFilter(randomGenerator.nextLong());
			assertTrue(regionFilter.equals(regionFilter));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionFilter regionFilter1 = getRandomRegionFilter(seed);
			RegionFilter regionFilter2 = getRandomRegionFilter(seed);
			assertFalse(regionFilter1 == regionFilter2);
			for (int j = 0; j < 10; j++) {
				assertTrue(regionFilter1.equals(regionFilter2));
				assertTrue(regionFilter2.equals(regionFilter1));
			}
		}

		// different inputs yield non-equal objects
		Set<RegionFilter> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionFilter regionFilter = getRandomRegionFilter(randomGenerator.nextLong());
			set.add(regionFilter);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = RegionFilter.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7083246786855409948L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionFilter regionFilter1 = getRandomRegionFilter(seed);
			RegionFilter regionFilter2 = getRandomRegionFilter(seed);

			assertEquals(regionFilter1, regionFilter2);
			assertEquals(regionFilter1.hashCode(), regionFilter2.hashCode());

		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionFilter regionFilter = getRandomRegionFilter(randomGenerator.nextLong());
			hashCodes.add(regionFilter.hashCode());
		}

		assertEquals(100, hashCodes.size());
		
	}

}
