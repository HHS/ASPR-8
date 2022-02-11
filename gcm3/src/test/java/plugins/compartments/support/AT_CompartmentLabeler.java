package plugins.compartments.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentLabeler.class)
public class AT_CompartmentLabeler {

	@Test
	@UnitTestConstructor(args = { Function.class })
	public void testConstructor() {
		assertNotNull(new CompartmentLabeler((c) -> null));
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		assertEquals(CompartmentId.class, new CompartmentLabeler((c) -> null).getDimension());
	}

	@Test
	@UnitTestMethod(name = "getLabel", args = {})
	public void testGetLabel() {

		/*
		 * Create a compartment labeler from a function. Have an agent apply the
		 * function directly to a person's compartment to get a label for that
		 * person. Get the label from the compartment labeler from the person id
		 * alone. Compare the two labels for equality.
		 */

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// build a compartment labeler with a function that can be tested
		Function<CompartmentId, Object> function = (c) -> {
			TestCompartmentId testCompartmentId = (TestCompartmentId) c;
			return testCompartmentId.ordinal();
		};

		CompartmentLabeler compartmentLabeler = new CompartmentLabeler(function);

		pluginBuilder.addAgent("agent");

		// add a few people to the simulation spread across the various
		// compartments
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			int numberOfPeople = 2 * TestCompartmentId.size();

			// show that there will be people
			assertTrue(numberOfPeople > 0);

			for (int i = 0; i < numberOfPeople; i++) {
				CompartmentId compartmentId = TestCompartmentId.values()[i % TestCompartmentId.size()];
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}
		}));

		/*
		 * Have the agent show that the compartment labeler created above
		 * produces a label for each person that is consistent with the function
		 * passed to the compartment labeler.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {

				// get the person's compartment and apply the function directly
				CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
				Object expectedLabel = function.apply(compartmentId);

				// get the label from the person id
				Object actualLabel = compartmentLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> compartmentLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		CompartmentsActionSupport.testConsumers(0, 7284994762664646917L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		CompartmentLabeler compartmentLabeler = new CompartmentLabeler((c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = compartmentLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonCompartmentChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonCompartmentChangeObservationEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// PersonCompartmentChangeObservationEvent
		PersonId personId = new PersonId(56);
		PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent = new PersonCompartmentChangeObservationEvent(personId, TestCompartmentId.COMPARTMENT_1,
				TestCompartmentId.COMPARTMENT_2);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personCompartmentChangeObservationEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

	}

}
