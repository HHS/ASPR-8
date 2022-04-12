package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.PersonRegionChangeObservationEvent;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

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
	@UnitTestMethod(name = "getLabel", args = {})
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			int numberOfPeople = 2 * TestRegionId.size();

			// show that there will be people
			assertTrue(numberOfPeople > 0);

			for (int i = 0; i < numberOfPeople; i++) {
				RegionId regionId = TestRegionId.values()[i % TestRegionId.size()];
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				personDataManager.addPerson(personConstructionData);				
			}
		}));
		


		/*
		 * Have the agent show that the region labeler created above
		 * produces a label for each person that is consistent with the function
		 * passed to the region labeler.
		 */
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			
			List<PersonId> people = personDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's region and apply the function directly
				RegionId regionId = regionDataManager.getPersonRegion(personId);
				Object expectedLabel = function.apply(regionId);

				// get the label from the person id
				Object actualLabel = regionLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		//test preconditions
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			
			//if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, ()->	regionLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			
			//if the person id is null
			contractException = assertThrows(ContractException.class, ()->	regionLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			
		}));
		

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 343017070904588574L,TimeTrackingPolicy.DO_NOT_TRACK_TIME, testPlugin);
		
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
		// PersonRegionChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonRegionChangeObservationEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// PersonRegionChangeObservationEvent
		PersonId personId = new PersonId(56);
		PersonRegionChangeObservationEvent personRegionChangeObservationEvent = new PersonRegionChangeObservationEvent(personId, TestRegionId.REGION_1,
				TestRegionId.REGION_2);
		labelerSensitivity.getPersonId(personRegionChangeObservationEvent);

	}
	
}
