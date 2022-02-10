package plugins.compartments.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test unit for {@link CompartmentFilter}.
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = CompartmentFilter.class)
public class AT_CompartmentFilter {

	@Test
	@UnitTestMethod(name = "validate", args = { SimulationContext.class })
	public void testValidate() {
		CompartmentsActionSupport.testConsumer(0, 162474236345345L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that a null compartment id causes validate() throws a
			// contract exception
			ContractException contractException = assertThrows(ContractException.class, () -> new CompartmentFilter(null).validate(c));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// show that an unknown compartment id causes validate() throws a
			// contract exception
			contractException = assertThrows(ContractException.class, () -> new CompartmentFilter(TestCompartmentId.getUnknownCompartmentId()).validate(c));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());
		});
	}

	/**
	 * Tests {@link CompartmentFilter#getFilterSensitivities()}
	 */
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		CompartmentsActionSupport.testConsumer(0, 54345345345345345L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// add a single person to the simulation
			PersonContructionData personContructionData = PersonContructionData.builder().add(TestCompartmentId.COMPARTMENT_1).build();
			c.resolveEvent(new PersonCreationEvent(personContructionData));
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			// create a compartment filter
			Filter filter = new CompartmentFilter(TestCompartmentId.COMPARTMENT_1);

			/*
			 * show the filter has a single sensitivity
			 */
			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			/*
			 * show that this sensitivity is associated with
			 * PersonCompartmentChangeObservationEvent events.
			 */
			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonCompartmentChangeObservationEvent.class, filterSensitivity.getEventClass());

			/*
			 * Show that the sensitivity requires refresh for
			 * PersonCompartmentChangeObservationEvent events if and only if one
			 * of the compartments in the event matches the compartment of the
			 * filter.
			 */
			PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent = new PersonCompartmentChangeObservationEvent(personId, TestCompartmentId.COMPARTMENT_1,
					TestCompartmentId.COMPARTMENT_2);
			assertTrue(filterSensitivity.requiresRefresh(c, personCompartmentChangeObservationEvent).isPresent());

			personCompartmentChangeObservationEvent = new PersonCompartmentChangeObservationEvent(personId, TestCompartmentId.COMPARTMENT_2, TestCompartmentId.COMPARTMENT_1);
			assertTrue(filterSensitivity.requiresRefresh(c, personCompartmentChangeObservationEvent).isPresent());

			personCompartmentChangeObservationEvent = new PersonCompartmentChangeObservationEvent(personId, TestCompartmentId.COMPARTMENT_2, TestCompartmentId.COMPARTMENT_3);
			assertFalse(filterSensitivity.requiresRefresh(c, personCompartmentChangeObservationEvent).isPresent());

		});

	}

	/**
	 * Tests {@link CompartmentFilter#evaluate(SimulationContext, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add the test agent
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent add a few people
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			int numberOfPeople = TestCompartmentId.size() * 3;
			assertTrue(numberOfPeople > 0);
			for (int i = 0; i < numberOfPeople; i++) {
				CompartmentId compartmentId = TestCompartmentId.values()[i % TestCompartmentId.size()];
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}
		}));

		/*
		 * Have the agent show that filter evaluates a person based on their
		 * compartment matching the compartment used to form the filter.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			Filter filter = new CompartmentFilter(TestCompartmentId.COMPARTMENT_1);
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			for (PersonId personId : personDataView.getPeople()) {
				boolean expected = compartmentLocationDataView.getPersonCompartment(personId).equals(TestCompartmentId.COMPARTMENT_1);
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

		}));

		/*
		 * Have the agent test preconditions
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			Filter filter = new CompartmentFilter(TestCompartmentId.COMPARTMENT_1);

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 3457455345388988L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			String expectedValue = "CompartmentFilter [compartmentId=" + testCompartmentId + "]";
			String actualValue = new CompartmentFilter(testCompartmentId).toString();
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

}
