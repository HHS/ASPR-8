package gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.plugins.groups.GroupsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupSampler;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.annotations.UnitTestMethod;

public class AT_GroupsDataManager_Continuity {

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of the
	 * data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		/*
		 * The returned string is the ordered state of the groups data manager. We
		 * generate this state at the end of each batch of simulation runs.
		 */

		Set<String> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1));
		pluginDatas.add(testStateContinuity(5));
		pluginDatas.add(testStateContinuity(10));

		/*
		 * Show that breaking up a simulation run into multiple increments has no effect
		 * on the final state of the groups data manager.
		 */
		assertEquals(1, pluginDatas.size());

	}

	/*
	 * Contains the current plugin data state of the simulation
	 *
	 */
	private static class StateData {
		private RunContinuityPluginData runContinuityPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private GroupsPluginData groupsPluginData;
		private SimulationState simulationState;
		private double haltTime;
		private String output;
	}

	/*
	 * Returns the default Simulation state -- time starts at zero synchronized to
	 * the beginning of the epoch.
	 */
	private static SimulationState getSimulationState() {
		return SimulationState.builder().build();
	}

	/*
	 * Returns an empty people plugin data
	 */
	private static PeoplePluginData getPeoplePluginData() {
		return PeoplePluginData.builder().build();
	}

	/*
	 * Returns the stochastics plugin data with only the main random generator.
	 */
	private static StochasticsPluginData getStochasticsPluginData(long seed) {

		WellState wellState = WellState.builder()//
				.setSeed(seed)//
				.build();

		return StochasticsPluginData.builder().setMainRNGState(wellState).build();
	}

	private static GroupsPluginData getGroupsPluginData() {
		return GroupsPluginData.builder().build();
	}

	private static RunContinuityPluginData getRunContinuityPluginData() {
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();
		double actionTime = 0;

		// define group types
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<TestGroupTypeId> groupTypeIds = Arrays.asList(TestGroupTypeId.values());
			Collections.reverse(groupTypeIds);
			for (TestGroupTypeId testGroupTypeId : groupTypeIds) {			
				groupsDataManager.addGroupType(testGroupTypeId);
			}
		});

		// add some groups
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			// addGroup(GroupConstructionInfo)
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			List<TestGroupTypeId> testGroupTypeIds = new ArrayList<>();
			testGroupTypeIds.add(TestGroupTypeId.GROUP_TYPE_2);
			testGroupTypeIds.add(TestGroupTypeId.GROUP_TYPE_1);
			testGroupTypeIds.add(TestGroupTypeId.GROUP_TYPE_3);
			for (TestGroupTypeId testGroupTypeId : testGroupTypeIds) {
				int n = randomGenerator.nextInt(10) + 1;
				for (int i = 0; i < n; i++) {
					GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder()
							.setGroupTypeId(testGroupTypeId).build();
					groupsDataManager.addGroup(groupConstructionInfo);
				}
			}
		});

		// define the group properties
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			
			List<TestGroupPropertyId> testGroupPropertyIds = new ArrayList<>();
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK);
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);			
			testGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK);
			
			for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				TestGroupTypeId testGroupTypeId = testGroupPropertyId.getTestGroupTypeId();

				GroupPropertyDefinitionInitialization.Builder defBuilder = GroupPropertyDefinitionInitialization
						.builder();
				defBuilder.setGroupTypeId(testGroupTypeId)//
						.setPropertyDefinition(testGroupPropertyId.getPropertyDefinition())//
						.setPropertyId(testGroupPropertyId);//
				boolean defaultValuePresent = testGroupPropertyId.getPropertyDefinition().getDefaultValue().isPresent();

				if (!defaultValuePresent) {
					List<GroupId> groupIds = groupsDataManager.getGroupsForGroupType(testGroupTypeId);
					for (GroupId groupId : groupIds) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						defBuilder.addPropertyValue(groupId, propertyValue);
					}
				}

				groupsDataManager.defineGroupProperty(defBuilder.build());
			}

		});

		// add people to groups
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			int n = 100;
			for (int i = 0; i < n; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			List<PersonId> people = peopleDataManager.getPeople();
			Collections.shuffle(people,new Random(randomGenerator.nextLong()));
			
			for (PersonId personId : people) {				
				int groupCount = randomGenerator.nextInt(FastMath.min(3, groupIds.size()));
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				for (int j = 0; j < groupCount; j++) {
					GroupId groupId = groupIds.get(j);
					groupsDataManager.addPersonToGroup(personId, groupId);
				}
			}
		});

		// add more groups
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			// addGroup(GroupTypeId)

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			Set<GroupTypeId> groupTypeIds = groupsDataManager.getGroupTypeIds();
			Set<GroupTypeId> groupTypesWithFullDefaults = new LinkedHashSet<>();
			for (GroupTypeId groupTypeId : groupTypeIds) {
				Set<GroupPropertyId> groupPropertyIds = groupsDataManager.getGroupPropertyIds(groupTypeId);
				boolean hasDefaults = true;
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					PropertyDefinition propertyDefinition = groupsDataManager.getGroupPropertyDefinition(groupTypeId,
							groupPropertyId);
					hasDefaults &= propertyDefinition.getDefaultValue().isPresent();
				}
				if (hasDefaults) {
					groupTypesWithFullDefaults.add(groupTypeId);
				}
			}

			assertFalse(groupTypesWithFullDefaults.isEmpty());

			for (GroupTypeId groupTypeId : groupTypesWithFullDefaults) {
				int count = randomGenerator.nextInt(5) + 1;
				for (int i = 0; i < count; i++) {
					groupsDataManager.addGroup(groupTypeId);
				}
			}

		});

		// remove some people from groups
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				int groupCount = groupsDataManager.getGroupCountForPerson(personId);
				if (groupCount > 0) {
					int remainingRemovals = randomGenerator.nextInt(groupCount);
					while (remainingRemovals > 0) {
						List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
						GroupId groupId = groupsForPerson.get(randomGenerator.nextInt(groupsForPerson.size()));
						groupsDataManager.removePersonFromGroup(personId, groupId);
						remainingRemovals--;
					}
				}
			}
		});

		// remove some groups
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			// removeGroup(GroupId)
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			Random random = new Random(randomGenerator.nextLong());

			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			Collections.shuffle(groupIds, random);
			int removalCount = groupIds.size() / 5;
			for (int i = 0; i < removalCount; i++) {
				groupsDataManager.removeGroup(groupIds.get(i));
			}

		});

		// set some group properties
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			// setGroupPropertyValue(GroupId, GroupPropertyId, Object)
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			Random random = new Random(randomGenerator.nextLong());
			List<GroupTypeId> groupTypeIds = new ArrayList<>(groupsDataManager.getGroupTypeIds());
			Collections.shuffle(groupTypeIds,random);
			
			for (GroupTypeId groupTypeId : groupTypeIds) {
				List<GroupId> groupIds = groupsDataManager.getGroupsForGroupType(groupTypeId);
				
				
				List<TestGroupPropertyId> groupPropertyIds = new ArrayList<>(groupsDataManager.getGroupPropertyIds(groupTypeId));
				Collections.shuffle(groupPropertyIds,random);				
				for (TestGroupPropertyId groupPropertyId : groupPropertyIds) {
					PropertyDefinition propertyDefinition = groupsDataManager.getGroupPropertyDefinition(groupTypeId,
							groupPropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						Collections.shuffle(groupIds,random);
						for (GroupId groupId : groupIds) {
							Object propertyValue = groupPropertyId.getRandomPropertyValue(randomGenerator);
							groupsDataManager.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
						}
					}
				}
			}
		});

		// set some groups and remove a few people
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			// sampleGroup(GroupId, GroupSampler)
			
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			Collections.sort(groupIds);
			
			for (GroupId groupId : groupIds) {
				GroupSampler groupSampler = GroupSampler.builder().build();
				Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
				if(optional.isPresent()) {
					PersonId personId = optional.get();
					groupsDataManager.removePersonFromGroup(personId, groupId);
				}
				
			}
		});

		// release the string state of the groups data manager as output
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			c.releaseOutput(groupsDataManager.toString());
		});

		return continuityBuilder.build();

	}

	private static StateData getInitialState(long seed) {
		StateData result = new StateData();
		result.runContinuityPluginData = getRunContinuityPluginData();
		result.peoplePluginData = getPeoplePluginData();
		result.stochasticsPluginData = getStochasticsPluginData(seed);
		result.groupsPluginData = getGroupsPluginData();
		result.simulationState = getSimulationState();

		return result;
	}

	/*
	 * Returns the duration for a single incremented run of the simulation. This is
	 * determined by finding the last scheduled task in the run continuity plugin
	 * data and dividing that by the number of increments.
	 */
	private static double getSimulationTimeIncrement(StateData stateData, int incrementCount) {
		double maxTime = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Consumer<ActorContext>> pair : stateData.runContinuityPluginData.getConsumers()) {
			Double time = pair.getFirst();
			maxTime = FastMath.max(maxTime, time);
		}

		return maxTime / incrementCount;
	}

	/*
	 * Returns the ordered state of the Groups Data Manager as a string
	 */
	private String testStateContinuity(int incrementCount) {

		long seed = 6160901456257930728L;

		/*
		 * We initialize the various plugin datas needed for the simulation
		 */
		StateData stateData = getInitialState(seed);

		// We will break up the simulation run into several runs, each lasting a
		// fixed duration
		double timeIncrement = getSimulationTimeIncrement(stateData, incrementCount);

		while (!stateData.runContinuityPluginData.allPlansComplete()) {
			stateData.haltTime += timeIncrement;
			runSimulation(stateData);
		}

		/*
		 * When the simulation has finished -- the plans contained in the run continuity
		 * plugin data have been completed, the string state of the attributes data
		 * manager is returned
		 */

		// show that the groups data manager toString() is returning something
		// reasonable
		assertNotNull(stateData.output);
		assertTrue(stateData.output.length() > 100);
		
		return stateData.output;
	}

	private static void runSimulation(StateData stateData) {
		// build the run continuity plugin
		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(stateData.runContinuityPluginData)//
				.build();

		// build the people plugin
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(stateData.peoplePluginData);

		// build the stochastics plugin
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stateData.stochasticsPluginData);

		// build the groups plugin
		Plugin groupsPlugin = GroupsPlugin.builder().setGroupsPluginData(stateData.groupsPluginData).getGroupsPlugin();

		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		// execute the simulation so that it produces a people plugin data
		Simulation simulation = Simulation.builder()//
				.addPlugin(peoplePlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(runContinuityPlugin)//
				.addPlugin(groupsPlugin)//
				.setSimulationHaltTime(stateData.haltTime)//
				.setRecordState(true)//
				.setOutputConsumer(outputConsumer)//
				.setSimulationState(stateData.simulationState)//
				.build();//
		simulation.execute();

		// retrieve the people plugin data
		stateData.peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

		// retrieve the groups plugin data
		stateData.groupsPluginData = outputConsumer.getOutputItem(GroupsPluginData.class).get();

		// retrieve the stochastics plugin data
		stateData.stochasticsPluginData = outputConsumer.getOutputItem(StochasticsPluginData.class).get();

		// retrieve the simulation state
		stateData.simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

		// retrieve the run continuity plugin data
		stateData.runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

		Optional<String> optional = outputConsumer.getOutputItem(String.class);
		if (optional.isPresent()) {
			stateData.output = optional.get();
		}

	}

}
