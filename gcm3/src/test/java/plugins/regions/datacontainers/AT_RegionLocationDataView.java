package plugins.regions.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.Simulation;
import nucleus.testsupport.MockContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
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
import plugins.regions.RegionPlugin;
import plugins.regions.events.mutation.PersonRegionAssignmentEvent;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionLocationDataView.class)
public class AT_RegionLocationDataView {

	@Test
	@UnitTestConstructor(args = { Context.class, RegionLocationDataManager.class })
	public void testConstructor() {
		Context context  = MockContext.builder().build();
		RegionLocationDataManager regionLocationDataManager = new RegionLocationDataManager(context,RegionInitialData.builder().build());
		assertThrows(RuntimeException.class,()-> new RegionLocationDataView(context, null));
		assertThrows(RuntimeException.class,()-> new RegionLocationDataView(null, regionLocationDataManager));
	}

	@Test
	@UnitTestMethod(name = "getRegionPopulationCount", args = { RegionId.class })
	public void testGetRegionPopulationCount() {

		Simulation.Builder builder = Simulation.builder();
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(1525815460460902517L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		// show that each region has no people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionLocationDataView.getRegionPopulationCount(testRegionId));
			}
		}));

		// show that adding people results in the correct population counts

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			int n = TestRegionId.values().length;
			for (int i = 0; i < 3 * n; i++) {
				TestRegionId regionId = TestRegionId.values()[i % n];
				PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(3, regionLocationDataView.getRegionPopulationCount(testRegionId));
			}

		}));

		// precondition tests

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getRegionPopulationCount(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getRegionPopulationCount(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getRegionPopulationTime", args = { RegionId.class })
	public void testGetRegionPopulationTime() {

		Simulation.Builder builder = Simulation.builder();
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(2430955549982485988L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		// show that each region has a zero population time
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionLocationDataView.getRegionPopulationTime(testRegionId));
			}

		}));

		Map<RegionId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedAssignmentTimes.put(testRegionId, new MutableDouble());
		}

		int numberOfPeople = 100;

		// show that adding people over time results in the correct population
		// times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			for (int i = 0; i < numberOfPeople; i++) {
				double planTime = i;
				c.addPlan((c2) -> {
					RegionLocationDataView regionLocationDataView = c2.getDataView(RegionLocationDataView.class).get();
					StochasticsDataView stochasticsDataView = c2.getDataView(StochasticsDataView.class).get();
					TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
					PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
					c2.resolveEvent(new PersonCreationEvent(personContructionData));
					assertEquals(c2.getTime(), regionLocationDataView.getRegionPopulationTime(regionId), 0);
					expectedAssignmentTimes.get(regionId).setValue(c2.getTime());
				}, planTime);
			}

		}));

		// show that the proper region population times are maintained
		// after all the person additions are complete.
		double postPersonAdditionTime = numberOfPeople;

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonAdditionTime, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				double expectedRegionPopulationTime = expectedAssignmentTimes.get(testRegionId).getValue();
				double actualRegionPopulationTime = regionLocationDataView.getRegionPopulationTime(testRegionId);
				assertEquals(expectedRegionPopulationTime, actualRegionPopulationTime);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getRegionPopulationTime(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getRegionPopulationTime(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPeopleInRegion", args = { RegionId.class })
	public void testGetPeopleInRegion() {

		Simulation.Builder builder = Simulation.builder();
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(-3347423560010833899L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		// show that each region is empty at time zero
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionLocationDataView.getPeopleInRegion(testRegionId).size());
			}
		}));

		// create a container to hold expectations
		Map<RegionId, Set<PersonId>> expectedPeopelInRegions = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedPeopelInRegions.put(testRegionId, new LinkedHashSet<>());
		}

		// add some people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (int i = 0; i < 100; i++) {
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
				PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				expectedPeopelInRegions.get(regionId).add(personId);
			}

		}));

		// show that the people in the regions match expectations
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Set<PersonId> expectedPeople = expectedPeopelInRegions.get(testRegionId);
				LinkedHashSet<PersonId> actualPeople = regionLocationDataView.getPeopleInRegion(testRegionId).stream().collect(Collectors.toCollection(LinkedHashSet::new));
				assertEquals(expectedPeople, actualPeople);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPeopleInRegion(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPeopleInRegion(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTime", args = { PersonId.class })
	public void testGetPersonRegionArrivalTime() {

		Simulation.Builder builder = Simulation.builder();
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(-2278422620232176214L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		// create a container to hold expectations
		Map<PersonId, MutableDouble> expectedPersonRegionArrivalTimes = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their region arrival times are zero.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				// show that the person has a region arrival time of zero
				assertEquals(0.0, regionLocationDataView.getPersonRegionArrivalTime(personId));
				// add the person to the expectations
				expectedPersonRegionArrivalTimes.put(personId, new MutableDouble());
			}
		}));

		// move people over time and show that each time they are moved the
		// their arrival time is correct
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			double planTime = 0;
			for (PersonId personId : personDataView.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionLocationDataView regionLocationDataView = c2.getDataView(RegionLocationDataView.class).get();
					TestRegionId regionId = regionLocationDataView.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					c2.resolveEvent(new PersonRegionAssignmentEvent(personId, regionId));
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(c2.getTime(), regionLocationDataView.getPersonRegionArrivalTime(personId));
					// update the expectations
					expectedPersonRegionArrivalTimes.get(personId).setValue(c2.getTime());
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people region arrival times are maintained over time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonMovementTime, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				double expectedArrivalTime = expectedPersonRegionArrivalTimes.get(personId).getValue();
				double actualArrivalTime = regionLocationDataView.getPersonRegionArrivalTime(personId);
				assertEquals(expectedArrivalTime, actualArrivalTime);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegionArrivalTime(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegionArrivalTime(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
		
		/////////////////////////////////////////////////
		// precondition test that requires rebuild of engine
		/////////////////////////////////////////////////
		
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.DO_NOT_TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(4584106512728037850L).build())::init);		
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		

		pluginBuilder.addAgent("agent");
		
		/*
		 * Add some people
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}
		}));

		
		
		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if region arrival times are not being tracked
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegionArrivalTime(new PersonId(0)));
			assertEquals(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());

			

		}));

		// build and add the action plugin
		actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
		
	}

	@Test
	@UnitTestMethod(name = "getPersonRegion", args = { PersonId.class })
	public void testGetPersonRegion() {

		Simulation.Builder builder = Simulation.builder();
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(5151111920517015649L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		// create a container to hold expectations
		Map<PersonId, RegionId> expectedPersonRegions = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their regions are correctly assigned.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
				// create the person
				PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				// show that the person has the correct region
				assertEquals(regionId, regionLocationDataView.getPersonRegion(personId));
				// add the person to the expectations
				expectedPersonRegions.put(personId, regionId);
			}
		}));

		// move people over time and show that each time they are moved the
		// correct region is reported
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			double planTime = 0;
			for (PersonId personId : personDataView.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionLocationDataView regionLocationDataView = c2.getDataView(RegionLocationDataView.class).get();
					TestRegionId regionId = regionLocationDataView.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					c2.resolveEvent(new PersonRegionAssignmentEvent(personId, regionId));
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(regionId, regionLocationDataView.getPersonRegion(personId));
					// update the expectations
					expectedPersonRegions.put(personId, regionId);
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people region arrival times are maintained over time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(postPersonMovementTime, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				RegionId expectedRegionId = expectedPersonRegions.get(personId);
				RegionId actualRegionId = regionLocationDataView.getPersonRegion(personId);
				assertEquals(expectedRegionId, actualRegionId);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegion(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegion(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		// 2934280155665825436
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			Simulation.Builder builder = Simulation.builder();
			RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();

			for (TestRegionId testRegionId : TestRegionId.values()) {
				regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
				});
			}
			regionInitialDataBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionInitialDataBuilder.build())::init);

			builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
			builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(8833508541323194123L).build())::init);
			builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
			builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
			builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
			builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

			ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

			pluginBuilder.addAgent("agent");
			/*
			 * Show that the correct time tracking policy is present
			 */
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
				assertEquals(timeTrackingPolicy, regionLocationDataView.getPersonRegionArrivalTrackingPolicy());

			}));

			// there are no precondition tests

			// build and add the action plugin
			ActionPlugin actionPlugin = pluginBuilder.build();
			builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

			// build and execute the engine
			builder.build().execute();

			// show that all actions were executed
			assertTrue(actionPlugin.allActionsExecuted());
		}
	}

}
