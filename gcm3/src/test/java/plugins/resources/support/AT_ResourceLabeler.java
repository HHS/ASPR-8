package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.Plugin;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ResourceLabeler.class)
public final class AT_ResourceLabeler {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, Function.class })
	public void testConstructor() {
		assertNotNull(new ResourceLabeler(TestResourceId.RESOURCE_3, (v) -> null));
	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {
		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */
		ResourceLabeler resourceLabeler = new ResourceLabeler(TestResourceId.RESOURCE_1, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = resourceLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonResourceUpdateEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonResourceUpdateEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// PersonResourceUpdateEvent
		PersonId personId = new PersonId(56);

		// show that an event that does not match the resource type will not
		// return a person id
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, TestResourceId.RESOURCE_4, 25L, 17L);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personResourceUpdateEvent);
		assertFalse(optional.isPresent());

		// show that an event that matches the resource type will return the
		// correct person id
		personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, TestResourceId.RESOURCE_1, 25L, 17L);
		optional = labelerSensitivity.getPersonId(personResourceUpdateEvent);
		assertTrue(optional.isPresent());

		assertEquals(personId, optional.get());
	}

	@Test
	@UnitTestMethod(name = "getLabel", args = { SimulationContext.class, PersonId.class })
	public void testGetLabel() {
		/*
		 * Create a resource labeler from a function. Have an agent apply the
		 * function directly to a person's resource to get a label for that
		 * person. Get the label from the resource labeler from the person id
		 * alone. Compare the two labels for equality.
		 */

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// build a resource labeler with a function that can be tested
		Function<Long, Object> function = (v) -> {
			return v % 2;
		};

		ResourceLabeler resourceLabeler = new ResourceLabeler(TestResourceId.RESOURCE_1, function);

		// distribute random resources across people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : peopleDataManager.getPeople()) {
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					long amount = randomGenerator.nextInt(100) + 1;
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, amount);
					resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, amount);
				}
			}
		}));

		/*
		 * Have the agent show that the resource labeler created above
		 * produces a label for each person that is consistent with the function
		 * passed to the resource labeler.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's resource and apply the function directly
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, personId);
				Object expectedLabel = function.apply(personResourceLevel);

				// get the label from the person id
				Object actualLabel = resourceLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> resourceLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> resourceLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(10, 7394122902151816457L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			assertEquals(testResourceId, new ResourceLabeler(testResourceId, (c) -> null).getDimension());
		}
	}

	@Test
	@UnitTestMethod(name = "getPastLabel", args = { Context.class, Event.class })
	public void testGetPastLabel() {
		ResourcesActionSupport.testConsumer(10, 6601261985382450295L, (c) -> {

			final PersonId personId = new PersonId(45);
			final ResourceId resourceId = TestResourceId.RESOURCE_4;
			long previousResourceLevel = 14L;
			final long currentResourceLevel = 99L;

			Function<Long, Object> function = (v) -> {
				return v % 2;
			};

			ResourceLabeler resourceLabeler = new ResourceLabeler(TestResourceId.RESOURCE_1, function);

			for (int i = 0; i < 10; i++) {
				previousResourceLevel = i;
				Object expectedLabel = function.apply(previousResourceLevel);
				PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
				Object actualLabel = resourceLabeler.getPastLabel(c, personResourceUpdateEvent);
				assertEquals(expectedLabel, actualLabel);
			}
		});

	}

}
