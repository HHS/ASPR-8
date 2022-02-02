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

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.Event;
import nucleus.testsupport.MockContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataView;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

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
		// PersonResourceChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonResourceChangeObservationEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// PersonCompartmentChangeObservationEvent
		PersonId personId = new PersonId(56);

		// show that an event that does not match the resource type will not
		// return a person id
		PersonResourceChangeObservationEvent personResourceChangeObservationEvent = new PersonResourceChangeObservationEvent(personId, TestResourceId.RESOURCE_4, 25L, 17L);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personResourceChangeObservationEvent);
		assertFalse(optional.isPresent());

		// show that an event that matches the resource type will return the
		// correct person id
		personResourceChangeObservationEvent = new PersonResourceChangeObservationEvent(personId, TestResourceId.RESOURCE_1, 25L, 17L);
		optional = labelerSensitivity.getPersonId(personResourceChangeObservationEvent);
		assertTrue(optional.isPresent());

		assertEquals(personId, optional.get());
	}

	@Test
	@UnitTestMethod(name = "getLabel", args = { Context.class, PersonId.class })
	public void testGetLabel() {
		/*
		 * Create a resource labeler from a function. Have an agent apply the
		 * function directly to a person's compartment to get a label for that
		 * person. Get the label from the compartment labeler from the person id
		 * alone. Compare the two labels for equality.
		 */

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// build a resource labeler with a function that can be tested
		Function<Long, Object> function = (v) -> {
			return v % 2;
		};

		ResourceLabeler resourceLabeler = new ResourceLabeler(TestResourceId.RESOURCE_1, function);

		pluginBuilder.addAgent("agent");

		// distribute random resources across people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					long amount = randomGenerator.nextInt(100) + 1;
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, amount));
					c.resolveEvent(new ResourceTransferToPersonEvent(testResourceId, personId, amount));
				}
			}
		}));

		/*
		 * Have the agent show that the compartment labeler created above
		 * produces a label for each person that is consistent with the function
		 * passed to the compartment labeler.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {

				// get the person's compartment and apply the function directly
				long personResourceLevel = resourceDataView.getPersonResourceLevel(TestResourceId.RESOURCE_1,personId);
				Object expectedLabel = function.apply(personResourceLevel);

				// get the label from the person id
				Object actualLabel = resourceLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> resourceLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> resourceLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		ResourcesActionSupport.testConsumers(10, 7394122902151816457L, actionPlugin);

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
		
		final PersonId personId = new PersonId(45);
		final ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 14L;
		final long currentResourceLevel = 99L;
		
		Function<Long, Object> function = (v) -> {
			return v % 2;
		};

		ResourceLabeler resourceLabeler = new ResourceLabeler(TestResourceId.RESOURCE_1, function);

		MockContext mockContext = MockContext.builder().build();
		
		for(int i= 0; i<10;i++) {
			previousResourceLevel = i;
			Object expectedLabel = function.apply(previousResourceLevel);
			PersonResourceChangeObservationEvent personResourceChangeObservationEvent = new PersonResourceChangeObservationEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
			Object actualLabel = resourceLabeler.getPastLabel(mockContext, personResourceChangeObservationEvent);
			assertEquals(expectedLabel, actualLabel);
		}
		
		
	}

}
