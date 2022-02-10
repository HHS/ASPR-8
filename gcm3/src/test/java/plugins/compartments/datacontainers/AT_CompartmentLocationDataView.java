package plugins.compartments.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.Simulation;
import nucleus.testsupport.MockSimulationContext;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentLocationDataView.class)
public class AT_CompartmentLocationDataView {
	@Test
	@UnitTestConstructor(args = { SimulationContext.class, CompartmentLocationDataManager.class })
	public void testConstructor() {
		SimulationContext simulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = new CompartmentLocationDataManager(simulationContext, CompartmentInitialData.builder().build());
		assertThrows(RuntimeException.class, () -> new CompartmentLocationDataView(simulationContext, null));
		assertThrows(RuntimeException.class, () -> new CompartmentLocationDataView(null, compartmentLocationDataManager));
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPopulationCount", args = { CompartmentId.class })
	public void testGetCompartmentPopulationCount() {

		Simulation.Builder builder = Simulation.builder();
		CompartmentInitialData.Builder compartmentInitialDataBuilder = CompartmentInitialData.builder();

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentInitialDataBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
		}
		compartmentInitialDataBuilder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(3607759084703047258L).build()::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgent("agent");

		// show that each compartment has no people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertEquals(0, compartmentLocationDataView.getCompartmentPopulationCount(testCompartmentId));
			}
		}));

		// show that adding people results in the correct population counts

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			int n = TestCompartmentId.values().length;
			for (int i = 0; i < 3 * n; i++) {
				TestCompartmentId compartmentId = TestCompartmentId.values()[i % n];
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertEquals(3, compartmentLocationDataView.getCompartmentPopulationCount(testCompartmentId));
			}

		}));

		// precondition tests

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if the compartment id is null
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getCompartmentPopulationCount(null));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment id is unknown
			contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getCompartmentPopulationCount(TestCompartmentId.getUnknownCompartmentId()));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPopulationTime", args = { CompartmentId.class })
	public void testGetCompartmentPopulationTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgent("agent");

		// show that each compartment has a zero population time
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertEquals(0, compartmentLocationDataView.getCompartmentPopulationTime(testCompartmentId));
			}

		}));

		Map<CompartmentId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedAssignmentTimes.put(testCompartmentId, new MutableDouble());
		}

		int numberOfPeople = 100;

		// show that adding people over time results in the correct population
		// times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			for (int i = 0; i < numberOfPeople; i++) {
				double planTime = i;
				c.addPlan((c2) -> {
					CompartmentLocationDataView compartmentLocationDataView = c2.getDataView(CompartmentLocationDataView.class).get();
					StochasticsDataManager stochasticsDataManager = c2.getDataView(StochasticsDataManager.class).get();
					TestCompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataManager.getRandomGenerator());
					PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
					c2.resolveEvent(new PersonCreationEvent(personContructionData));
					assertEquals(c2.getTime(), compartmentLocationDataView.getCompartmentPopulationTime(compartmentId), 0);
					expectedAssignmentTimes.get(compartmentId).setValue(c2.getTime());
				}, planTime);
			}

		}));

		// show that the proper compartment population times are maintained
		// after all the person additions are complete.
		double postPersonAdditionTime = numberOfPeople;

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonAdditionTime, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				double expectedCompartmentPopulationTime = expectedAssignmentTimes.get(testCompartmentId).getValue();
				double actualCompartmentPopulationTime = compartmentLocationDataView.getCompartmentPopulationTime(testCompartmentId);
				assertEquals(expectedCompartmentPopulationTime, actualCompartmentPopulationTime);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if the compartment id is null
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getCompartmentPopulationTime(null));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment id is unknown
			contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getCompartmentPopulationTime(TestCompartmentId.getUnknownCompartmentId()));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		CompartmentsActionSupport.testConsumers(0, 2430955549982485988L, TimeTrackingPolicy.TRACK_TIME, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getPeopleInCompartment", args = { CompartmentId.class })
	public void testGetPeopleInCompartment() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgent("agent");

		// show that each compartment is empty at time zero
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertEquals(0, compartmentLocationDataView.getPeopleInCompartment(testCompartmentId).size());
			}
		}));

		// create a container to hold expectations
		Map<CompartmentId, Set<PersonId>> expectedPeopelInCompartments = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedPeopelInCompartments.put(testCompartmentId, new LinkedHashSet<>());
		}

		// add some people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (int i = 0; i < 100; i++) {
				TestCompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataManager.getRandomGenerator());
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				expectedPeopelInCompartments.get(compartmentId).add(personId);
			}

		}));

		// show that the people in the compartments match expectations
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				Set<PersonId> expectedPeople = expectedPeopelInCompartments.get(testCompartmentId);
				LinkedHashSet<PersonId> actualPeople = compartmentLocationDataView.getPeopleInCompartment(testCompartmentId).stream().collect(Collectors.toCollection(LinkedHashSet::new));
				assertEquals(expectedPeople, actualPeople);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if the compartment id is null
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPeopleInCompartment(null));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment id is unknown
			contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPeopleInCompartment(TestCompartmentId.getUnknownCompartmentId()));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartmentArrivalTime", args = { PersonId.class })
	public void testGetPersonCompartmentArrivalTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgent("agent");

		// create a container to hold expectations
		Map<PersonId, MutableDouble> expectedPersonCompartmentArrivalTimes = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their compartment arrival times are
		 * zero.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a compartment at random
				TestCompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				// show that the person has a compartment arrival time of zero
				assertEquals(0.0, compartmentLocationDataView.getPersonCompartmentArrivalTime(personId));
				// add the person to the expectations
				expectedPersonCompartmentArrivalTimes.put(personId, new MutableDouble());
			}
		}));

		// move people over time and show that each time they are moved the
		// their arrival time is correct
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			double planTime = 0;
			for (PersonId personId : personDataView.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current compartment
					CompartmentLocationDataView compartmentLocationDataView = c2.getDataView(CompartmentLocationDataView.class).get();
					TestCompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
					// select the next compartment for the person
					compartmentId = compartmentId.next();
					// move the person
					c2.resolveEvent(new PersonCompartmentAssignmentEvent(personId, compartmentId));
					/*
					 * show that the compartment arrival time for the person is
					 * the current time in the simulation
					 */
					assertEquals(c2.getTime(), compartmentLocationDataView.getPersonCompartmentArrivalTime(personId));
					// update the expectations
					expectedPersonCompartmentArrivalTimes.get(personId).setValue(c2.getTime());
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people compartment arrival times are maintained over
		 * time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonMovementTime, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				double expectedArrivalTime = expectedPersonCompartmentArrivalTimes.get(personId).getValue();
				double actualArrivalTime = compartmentLocationDataView.getPersonCompartmentArrivalTime(personId);
				assertEquals(expectedArrivalTime, actualArrivalTime);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartmentArrivalTime(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartmentArrivalTime(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 2278422620232176214L, TimeTrackingPolicy.TRACK_TIME, actionPluginInitializer);
		/////////////////////////////////////////////////
		// precondition test that requires rebuild of engine
		/////////////////////////////////////////////////

		pluginBuilder.addAgent("agent");

		/*
		 * Add some people
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a compartment at random
				TestCompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if compartment arrival tracking times are not tracked
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartmentArrivalTime(new PersonId(0)));
			assertEquals(CompartmentError.COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());

		}));

		// build the action plugin
		actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		CompartmentsActionSupport.testConsumers(0, 3338965305284292260L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartment", args = { PersonId.class })
	public void testGetPersonCompartment() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgent("agent");

		// create a container to hold expectations
		Map<PersonId, CompartmentId> expectedPersonCompartments = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their compartments are correctly
		 * assigned.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a compartment at random
				TestCompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(compartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				// show that the person has the correct compartment
				assertEquals(compartmentId, compartmentLocationDataView.getPersonCompartment(personId));
				// add the person to the expectations
				expectedPersonCompartments.put(personId, compartmentId);
			}
		}));

		// move people over time and show that each time they are moved the
		// correct compartment is reported
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			double planTime = 0;
			for (PersonId personId : personDataView.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current compartment
					CompartmentLocationDataView compartmentLocationDataView = c2.getDataView(CompartmentLocationDataView.class).get();
					TestCompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
					// select the next compartment for the person
					compartmentId = compartmentId.next();
					// move the person
					c2.resolveEvent(new PersonCompartmentAssignmentEvent(personId, compartmentId));
					/*
					 * show that the compartment arrival time for the person is
					 * the current time in the simulation
					 */
					assertEquals(compartmentId, compartmentLocationDataView.getPersonCompartment(personId));
					// update the expectations
					expectedPersonCompartments.put(personId, compartmentId);
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people compartment arrival times are maintained over
		 * time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonMovementTime, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				CompartmentId expectedCompartmentId = expectedPersonCompartments.get(personId);
				CompartmentId actualCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
				assertEquals(expectedCompartmentId, actualCompartmentId);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartment(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartment(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		CompartmentsActionSupport.testConsumers(0, 442744021729694111L, TimeTrackingPolicy.TRACK_TIME, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartmentArrivalTrackingPolicy", args = {})
	public void testGetPersonCompartmentArrivalTrackingPolicy() {
		/*
		 * Show that the correct time tracking policy is present
		 */		
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			CompartmentsActionSupport.testConsumer(0, 2934280155665825436L, timeTrackingPolicy, (c)->{
				CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
				assertEquals(timeTrackingPolicy, compartmentLocationDataView.getPersonCompartmentArrivalTrackingPolicy());

			});
		}
	}

}
