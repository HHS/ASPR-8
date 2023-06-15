package plugins.partitions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.SimulationState;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.Equality;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionSampler;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.AttributesPluginId;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public final class AT_PartitionsDataManager_Continuity {

	// create a key value for the partition
	private final static Object key = "key";

	private static enum B1_Label {
		A, B
	}

	private static enum I0_Label {
		YES, NO
	}

	private static enum I1_Label {
		LOW, MEDIUM, HIGH
	}

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of
	 * the data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 * 
	 * This test is somewhat indirect. The partitions data manager does
	 * serialize its partitions at the end of the simulation and so we use the
	 * state of the Attributes data manager as a proxy to show that repeatedly
	 * starting and stopping a simulation that is actively using the partition
	 * sampling mechanism will result in a run-continuous simulation.*
	 */
	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		// We will run a simulation that sample from a partition to select
		// people to mutate.

		Set<String> attributeStates = new LinkedHashSet<>();
		attributeStates.add(testStateContinuity(1));
		attributeStates.add(testStateContinuity(5));
		attributeStates.add(testStateContinuity(10));

		assertEquals(1, attributeStates.size());

		String state = attributeStates.iterator().next();
		assertNotNull(state);
		assertTrue(state.length() > 0);

	}

	/*
	 * Partitions are not explicitly serialized. We have to recreate the
	 * partition each time the simulation executes on the first use of the
	 * partition in each simulation instance.
	 * 
	 * The partition consists of:
	 * 
	 * A filter on TestAttributeId.BOOLEAN_0
	 * 
	 * A labeler TestAttributeId.BOOLEAN_1 that labels people with B1_Label.A or
	 * B1_Label.B
	 * 
	 * A labeler TestAttributeId.INT_0 that labels people with I0_Label.YES or
	 * I0_Label.NO
	 * 
	 * A labeler TestAttributeId.INT_1 that labels people with I1_Label.LOW,
	 * I1_Label.MEDIUM, or I1_Label.HIGH
	 * 
	 */
	private static void makePartition(ActorContext c) {
		PartitionsDataManager partitionsDataManager = c.getDataManager(PartitionsDataManager.class);

		AttributeFilter attributeFilter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);

		AttributeLabeler attributeLabeler1 = new AttributeLabeler(TestAttributeId.BOOLEAN_1) {
			protected Object getLabelFromValue(Object value) {
				Boolean b = (Boolean) value;
				if (b) {
					return B1_Label.A;
				} else {
					return B1_Label.B;
				}
			}
		};

		AttributeLabeler attributeLabeler2 = new AttributeLabeler(TestAttributeId.INT_0) {
			protected Object getLabelFromValue(Object value) {
				Integer i = (Integer) value;
				if (i < 3) {
					return I0_Label.YES;
				}
				return I0_Label.NO;
			}
		};

		AttributeLabeler attributeLabeler3 = new AttributeLabeler(TestAttributeId.INT_1) {
			protected Object getLabelFromValue(Object value) {
				Integer i = (Integer) value;
				if (i < 30) {
					return I1_Label.LOW;
				} else if (i < 70) {
					return I1_Label.MEDIUM;
				} else {
					return I1_Label.HIGH;
				}

			}
		};

		Partition partition = Partition	.builder()//
										.setFilter(attributeFilter)//
										.addLabeler(attributeLabeler1)//
										.addLabeler(attributeLabeler2)//
										.addLabeler(attributeLabeler3)//
										.build();

		partitionsDataManager.addPartition(partition, key);

	}

	/*
	 * Returns the run continuity plugin data. This consists of:
	 * 
	 * 1)An initial task at time zero to create 1000 people and assign them
	 * randomized attribute values
	 * 
	 * 2)100 tasks to A) select a person via partition sampling and randomly
	 * mutate their attributes, and B) select a person at random from the
	 * general population and randomly mutate their attributes
	 * 
	 * 3)A final task to release the attribute data manager's string
	 * representation to output. This ouput will be collected and compared for
	 * each incrementing of the simulation.
	 */
	private static RunContinuityPluginData getRunContinuityPluginData() {
		/*
		 * Build the RunContinuityPluginData
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();
		double actionTime = 0;

		/*
		 *
		 * create some people with their attributes
		 */
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (int i = 0; i < 1000; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean());
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean());
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, randomGenerator.nextInt(5));
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, randomGenerator.nextInt(100));
			}

		});

		/*
		 * Use the partition to select people to mutate, also select people at
		 * random to mutate
		 */
		for (int i = 0; i < 100; i++) {
			continuityBuilder.addContextConsumer(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PartitionsDataManager partitionsDataManager = c.getDataManager(PartitionsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

				if (!partitionsDataManager.partitionExists(key)) {
					makePartition(c);
				}

				// Build a random label set
				LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, B1_Label.values()[randomGenerator.nextInt(2)])//
											.setLabel(TestAttributeId.INT_0, I0_Label.values()[randomGenerator.nextInt(2)])//
											.setLabel(TestAttributeId.INT_1, I1_Label.values()[randomGenerator.nextInt(3)])//
											.build();

				// find a person in the corresponding partition cell

				PartitionSampler partitionSampler = PartitionSampler.builder().setLabelSet(labelSet).build();
				Optional<PersonId> optional = partitionsDataManager.samplePartition(key, partitionSampler);
				if (optional.isPresent()) {
					PersonId personId = optional.get();
					// mutate this person
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean());
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean());
					attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, randomGenerator.nextInt(5));
					attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, randomGenerator.nextInt(100));
				}

				/*
				 * The logic above will tend to reduce the number of people in
				 * the partition, so we will also select random people from the
				 * whole population and mutate them as well
				 */

				List<PersonId> people = peopleDataManager.getPeople();
				if (!people.isEmpty()) {
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					// mutate this person
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean());
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean());
					attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, randomGenerator.nextInt(5));
					attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, randomGenerator.nextInt(100));
				}

			});
		}

		/*
		 * release the attributes data manager as a string
		 */
		continuityBuilder.addContextConsumer(actionTime++, (c) -> {

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			c.releaseOutput(attributesDataManager.toString());
		});
		return continuityBuilder.build();
	}

	/*
	 * Returns the starting AttributesPluginData containing just the fixed
	 * TestAttributeId associated attribute definitions.
	 */
	private static AttributesPluginData getAttributesPluginData() {
		AttributesPluginData.Builder attributeBuilder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		return attributeBuilder.build();
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
	private static StochasticsPluginData getStochasticsPluginData(RandomGenerator randomGenerator) {

		WellState wellState = WellState	.builder()//
										.setSeed(randomGenerator.nextLong())//
										.build();

		return StochasticsPluginData.builder().setMainRNGState(wellState).build();
	}

	/*
	 * Returns a partitions plugin data that has been set to support run
	 * continuity.
	 */
	private static PartitionsPluginData getPartitionsPluginData() {
		return PartitionsPluginData	.builder()//
									.setRunContinuitySupport(true)//
									.build();
	}

	/*
	 * Returns the default Simulation state -- time starts at zero synchronized
	 * to the beginning of the epoch.
	 */
	private static SimulationState getSimulationState() {
		return SimulationState.builder().build();
	}

	private static class StateData {
		private RunContinuityPluginData runContinuityPluginData;
		private AttributesPluginData attributesPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private PartitionsPluginData partitionsPluginData;
		private SimulationState simulationState;
		private double haltTime;
		private String output;
	}

	/*
	 * Returns the initial state of the StateData
	 */
	private static StateData getInitialState(RandomGenerator randomGenerator) {
		StateData result = new StateData();

		result.runContinuityPluginData = getRunContinuityPluginData();
		result.attributesPluginData = getAttributesPluginData();
		result.peoplePluginData = getPeoplePluginData();
		result.stochasticsPluginData = getStochasticsPluginData(randomGenerator);
		result.partitionsPluginData = getPartitionsPluginData();
		result.simulationState = getSimulationState();

		return result;
	}

	/*
	 * Executes the simulation using the state data, scheduling the simulation
	 * to halt at a prescribed time and gathers plugin data information from the
	 * simulation. This information is used to update the state data.
	 */
	private static void runSimulation(StateData stateData) {
		// build the run continuity plugin
		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
														.setRunContinuityPluginData(stateData.runContinuityPluginData)//
														.build();

		// build the attributes plugin
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(stateData.attributesPluginData);

		// build the people plugin
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(stateData.peoplePluginData);

		// build the stochastics plugin
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stateData.stochasticsPluginData);

		// build the partitions plugin
		Plugin partitionsPlugin = PartitionsPlugin	.builder()//
													.addPluginDependency(AttributesPluginId.PLUGIN_ID)//
													.setPartitionsPluginData(stateData.partitionsPluginData)//
													.getPartitionsPlugin();

		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		// execute the simulation so that it produces a people plugin data
		Simulation simulation = Simulation	.builder()//
											.addPlugin(runContinuityPlugin)//
											.addPlugin(attributesPlugin)//
											.addPlugin(peoplePlugin)//
											.addPlugin(stochasticsPlugin)//
											.addPlugin(partitionsPlugin)//
											.setSimulationHaltTime(stateData.haltTime)//
											.setRecordState(true)//
											.setOutputConsumer(outputConsumer)//
											.setSimulationState(stateData.simulationState)//
											.build();//
		simulation.execute();

		// retrieve the run continuity plugin data
		stateData.runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

		// retrieve the attributes plugin data
		stateData.attributesPluginData = outputConsumer.getOutputItem(AttributesPluginData.class).get();

		// retrieve the people plugin data
		stateData.peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

		// retrieve the stochastics plugin data
		stateData.stochasticsPluginData = outputConsumer.getOutputItem(StochasticsPluginData.class).get();

		// retrieve the partitions plugin data
		stateData.partitionsPluginData = outputConsumer.getOutputItem(PartitionsPluginData.class).get();

		// retrieve the simulation state
		stateData.simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

		Optional<String> optional = outputConsumer.getOutputItem(String.class);
		if (optional.isPresent()) {
			stateData.output = optional.get();
		}

	}

	/*
	 * Returns the duration for a single incremented run of the simulation. This
	 * is determined by finding the last scheduled task in the run continuity
	 * plugin data and dividing that by the number of increments.
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
	 * Returns the string representation of the attributes data manager after
	 * breaking up the simulation's execution into several sub-executions.
	 */
	private String testStateContinuity(int incrementCount) {

		/*
		 * Each simulation will start at time zero. Seed initialization for the
		 * stochastics data manager starts with a seed derived from this fixed
		 * random generator.
		 */
		RandomGenerator rng = RandomGeneratorProvider.getRandomGenerator(6684803818366629112L);

		/*
		 * We initialize the various plugin datas needed for the simulation
		 */
		StateData stateData = getInitialState(rng);

		// We will break up the simulation run into several runs, each lasting a
		// fixed duration
		double timeIncrement = getSimulationTimeIncrement(stateData, incrementCount);

		while (!stateData.runContinuityPluginData.allPlansComplete()) {
			stateData.haltTime += timeIncrement;
			runSimulation(stateData);
		}

		/*
		 * When the simulation has finished -- the plans contained in the run
		 * continuity plugin data have been completed, the string state of the
		 * attributes data manager is returned
		 */
		return stateData.output;
	}

}