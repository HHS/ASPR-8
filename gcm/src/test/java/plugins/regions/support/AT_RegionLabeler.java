package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.testsupport.TestPartitionsContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import plugins.stochastics.datamanagers.StochasticsDataManager;
import plugins.regions.testsupport.TestRegionId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionLabeler {
	private static class LocalRegionLabeler extends RegionLabeler {
		private final Function<RegionId, Object> regionLabelingFunction;

		public LocalRegionLabeler( Function<RegionId, Object> regionLabelingFunction) {			
			this.regionLabelingFunction = regionLabelingFunction;
		}

		@Override
		protected Object getLabelFromRegionId(RegionId regionId) {
			return regionLabelingFunction.apply(regionId);
		}
	}
	@Test
	@UnitTestConstructor(target = RegionLabeler.class,args = {})
	public void testConstructor() {
		assertNotNull(new LocalRegionLabeler((c) -> null));
	}

	@Test
	@UnitTestMethod(target = RegionLabeler.class,name = "getId", args = {})
	public void testGetId() {
		assertEquals(RegionId.class, new LocalRegionLabeler((c) -> null).getId());
	}

	@Test
	@UnitTestMethod(target = RegionLabeler.class,name = "getCurrentLabel", args = { PartitionsContext.class, PersonId.class })
	public void testGetLabel() {

		/*
		 * Create a region labeler from a function. Have an agent apply the
		 * function directly to a person's region to get a label for that
		 * person. Get the label from the region labeler from the person id
		 * alone. Compare the two labels for equality.
		 */

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// build a region labeler with a function that can be tested
		Function<RegionId, Object> function = (c) -> {
			TestRegionId testRegionId = (TestRegionId) c;
			return testRegionId.ordinal();
		};

		RegionLabeler regionLabeler = new LocalRegionLabeler(function);

		// add a few people to the simulation spread across the various
		// regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			int numberOfPeople = 2 * TestRegionId.size();

			// show that there will be people
			assertTrue(numberOfPeople > 0);

			for (int i = 0; i < numberOfPeople; i++) {
				RegionId regionId = TestRegionId.values()[i % TestRegionId.size()];
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				peopleDataManager.addPerson(personConstructionData);
			}
		}));

		/*
		 * Have the agent show that the region labeler created above
		 * produces a label for each person that is consistent with the function
		 * passed to the region labeler.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's region and apply the function directly
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				Object expectedLabel = function.apply(regionId);

				// get the label from the person id
				Object actualLabel = regionLabeler.getCurrentLabel(testPartitionsContext, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionLabeler.getCurrentLabel(testPartitionsContext, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> regionLabeler.getCurrentLabel(testPartitionsContext, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = RegionsTestPluginFactory.factory(0, 4893773537497436066L, false, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = RegionLabeler.class,name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		RegionLabeler regionLabeler = new LocalRegionLabeler((c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = regionLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonRegionUpdateEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonRegionUpdateEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// PersonRegionUpdateEvent
		PersonId personId = new PersonId(56);
		PersonRegionUpdateEvent personRegionUpdateEvent = new PersonRegionUpdateEvent(personId, TestRegionId.REGION_1,
				TestRegionId.REGION_2);
		labelerSensitivity.getPersonId(personRegionUpdateEvent);

	}

	@Test
	@UnitTestMethod(target = RegionLabeler.class,name = "getPastLabel", args = { PartitionsContext.class, Event.class })
	public void testGetPastLabel() {

		Factory factory = RegionsTestPluginFactory.factory(30, 349819763474394472L, true, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Function<RegionId, Object> func = (g) -> {
				TestRegionId testRegionId = (TestRegionId) g;
				return testRegionId.ordinal();
			};

			RegionLabeler regionLabeler = new LocalRegionLabeler(func);
			List<RegionId> regions = new ArrayList<>(regionsDataManager.getRegionIds());

			// Person region update event
			for (PersonId personId : peopleDataManager.getPeople()) {
				RegionId personRegion = regionsDataManager.getPersonRegion(personId);
				RegionId nextRegion = regions.get(randomGenerator.nextInt(regions.size()));
				regionsDataManager.setPersonRegion(personId, nextRegion);
				PersonRegionUpdateEvent personRegionUpdateEvent = new PersonRegionUpdateEvent(personId,
						personRegion, nextRegion);
				Object expectedLabel = func.apply(personRegion);
				Object actualLabel = regionLabeler.getPastLabel(testPartitionsContext, personRegionUpdateEvent);
				assertEquals(expectedLabel, actualLabel);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
