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
import nucleus.Plugin;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = RegionLabeler.class)
public class AT_RegionLabeler {

	@Test
	@UnitTestConstructor(args = { Function.class })
	public void testConstructor() {
		assertNotNull(new RegionLabeler((c) -> null));
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		assertEquals(RegionId.class, new RegionLabeler((c) -> null).getDimension());
	}

	@Test
	@UnitTestMethod(name = "getLabel", args = { SimulationContext.class, PersonId.class })
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

		RegionLabeler regionLabeler = new RegionLabeler(function);

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
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's region and apply the function directly
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				Object expectedLabel = function.apply(regionId);

				// get the label from the person id
				Object actualLabel = regionLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionLabeler.getLabel(c, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> regionLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 4893773537497436066L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		RegionLabeler regionLabeler = new RegionLabeler((c) -> null);

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
	@UnitTestMethod(name = "getPastLabel", args = { SimulationContext.class, Event.class })
	public void testGetPastLabel() {

		RegionsActionSupport.testConsumer(30, 349819763474394472L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Function<RegionId, Object> func = (g) -> {
				TestRegionId testRegionId = (TestRegionId) g;
				return testRegionId.ordinal();
			};

			RegionLabeler regionLabeler = new RegionLabeler(func);
			List<RegionId> regions = new ArrayList<>(regionsDataManager.getRegionIds());

			// Person region update event
			for (PersonId personId : peopleDataManager.getPeople()) {
				RegionId personRegion = regionsDataManager.getPersonRegion(personId);
				RegionId nextRegion = regions.get(randomGenerator.nextInt(regions.size()));
				regionsDataManager.setPersonRegion(personId, nextRegion);
				PersonRegionUpdateEvent personRegionUpdateEvent = new PersonRegionUpdateEvent(personId,
						personRegion, nextRegion);
				Object expectedLabel = func.apply(personRegion);
				Object actualLabel = regionLabeler.getPastLabel(c, personRegionUpdateEvent);
				assertEquals(expectedLabel, actualLabel);
			}

		});
	}
}
