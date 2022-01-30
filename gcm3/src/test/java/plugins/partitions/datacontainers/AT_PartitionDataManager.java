package plugins.partitions.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.components.support.ComponentError;
import plugins.components.support.ComponentId;
import plugins.components.testsupport.SimpleComponentId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PopulationPartition;
import plugins.partitions.support.PopulationPartitionImpl;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionDataManager.class)
public final class AT_PartitionDataManager {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

	// Executes the consumer at time = 0 under an agent
	private void testConsumer(long seed, Consumer<AgentContext> consumer) {
		Builder builder = Simulation.builder();

		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add an agent that executes the consumer
		ActionPlugin actionPlugin = ActionPlugin.builder()//
												.addAgent("agent")//
												.addAgentActionPlan("agent", new AgentActionPlan(0, consumer))//
												.build();//
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "addPartition", args = { Object.class, ComponentId.class, PopulationPartition.class })
	public void testaddPartition() {


		testConsumer(1137046131619466337L,(c) -> {
			AgentId agentId = c.getCurrentAgentId();
			PartitionDataManager partitionDataManager = new PartitionDataManager();
			Object key = new Object();
			
			Partition partition = Partition.builder().build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
			partitionDataManager.addPartition(key, agentId, populationPartition);

			assertEquals(populationPartition, partitionDataManager.getPopulationPartition(key));

			// precondition tests

			// if the key is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(null, agentId, populationPartition));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

			// if the component id is null
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(key, null, populationPartition));
			assertEquals(ComponentError.NULL_AGENT_ID, contractException.getErrorType());

			// if the population partition is null
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(key, agentId, null));
			assertEquals(PartitionError.NULL_POPULATION_PARTITION, contractException.getErrorType());

			// if the key is already allocated to another population partition
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(key, agentId, populationPartition));
			assertEquals(PartitionError.DUPLICATE_PARTITION, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getKeys", args = {})
	public void testGetKeys() {

		testConsumer(3174291309585412438L,(c) -> {
			AgentId agentId = c.getCurrentAgentId();
			PartitionDataManager partitionDataManager = new PartitionDataManager();
			

			// create a container to hold the expected keys
			Set<Object> expectedKeys = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				expectedKeys.add("key" + i);
			}

			// add a partition for each key
			for (Object key : expectedKeys) {
				Partition partition = Partition.builder().build();
				PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
				partitionDataManager.addPartition(key, agentId, populationPartition);
			}

			// show that the expected and actual keys match
			assertEquals(expectedKeys, partitionDataManager.getKeys());

		});

	}

	@Test
	@UnitTestMethod(name = "getOwningComponent", args = { Object.class })
	public void testGetOwningComponent() {
		

		testConsumer(8952280372763678179L,(c) -> {
			PartitionDataManager partitionDataManager = new PartitionDataManager();

			// create a container to hold the expected keys
			Map<Object, AgentId> expectedAgentIds = new LinkedHashMap<>();
			for (int i = 0; i < 10; i++) {
				expectedAgentIds.put("key" + i, new AgentId(i));
			}

			// add a partition for each key
			for (Object key : expectedAgentIds.keySet()) {
				AgentId agentId = expectedAgentIds.get(key);
				Partition partition = Partition.builder().build();
				PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
				partitionDataManager.addPartition(key, agentId, populationPartition);
			}

			// show that the expected and actual component ids match
			for (Object key : expectedAgentIds.keySet()) {
				AgentId expectdAgentId = expectedAgentIds.get(key);
				AgentId acutalAgentId = partitionDataManager.getOwningAgentId(key);
				assertEquals(expectdAgentId, acutalAgentId);
			}
			// show that an unknown key yields a null return
			assertNull(partitionDataManager.getOwningAgentId(new AgentId(100000000)));

			// show that a null key yields a null return
			assertNull(partitionDataManager.getOwningAgentId(null));

		});
	}

	@Test
	@UnitTestMethod(name = "getPopulationPartition", args = { Object.class })
	public void testGetPopulationPartition() {

		testConsumer(8598126216292150427L,(c) -> {
			PartitionDataManager partitionDataManager = new PartitionDataManager();
			AgentId agentId = c.getCurrentAgentId();

			// create a container to hold the expected population partitions
			Map<Object, PopulationPartition> expectedPopulationPartitions = new LinkedHashMap<>();
			for (int i = 0; i < 10; i++) {
				Object key = "key" + i;
				Partition partition = Partition.builder().build();
				PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
				expectedPopulationPartitions.put(key, populationPartition);
			}

			// add a partition for each key
			for (Object key : expectedPopulationPartitions.keySet()) {
				PopulationPartition populationPartition = expectedPopulationPartitions.get(key);
				partitionDataManager.addPartition(key, agentId, populationPartition);
			}

			// show that the expected and actual component ids match
			for (Object key : expectedPopulationPartitions.keySet()) {
				PopulationPartition expectedPopulationPartition = expectedPopulationPartitions.get(key);
				PopulationPartition actualPopulationPartition = partitionDataManager.getPopulationPartition(key);
				assertEquals(expectedPopulationPartition, actualPopulationPartition);
			}

			// show that an unknown key yields a null return
			assertNull(partitionDataManager.getPopulationPartition(new SimpleComponentId("unknown component")));

			// show that a null key yields a null return
			assertNull(partitionDataManager.getPopulationPartition(null));

		});
	}

	@Test
	@UnitTestMethod(name = "partitionExists", args = { Object.class })
	public void testPartitionExists() {

		testConsumer(1968926333881399732L,(c) -> {
			PartitionDataManager partitionDataManager = new PartitionDataManager();
			AgentId agentId = c.getCurrentAgentId();

			// create containers to hold known and unknown keys
			Set<Object> knownKeys = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				Object key = "key" + i;
				knownKeys.add(key);
			}

			Set<Object> unknownKeys = new LinkedHashSet<>();
			for (int i = 10; i < 20; i++) {
				Object key = "key" + i;
				unknownKeys.add(key);
			}

			// add a partition for each key
			for (Object key : knownKeys) {
				Partition partition = Partition.builder().build();
				PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
				partitionDataManager.addPartition(key, agentId, populationPartition);
			}

			// show that the known keys will have a partition
			for (Object key : knownKeys) {
				assertTrue(partitionDataManager.partitionExists(key));
			}

			// show that the unknown keys will not have a partition
			for (Object key : unknownKeys) {
				assertFalse(partitionDataManager.partitionExists(key));
			}

			// show that the null key has no partition
			assertFalse(partitionDataManager.partitionExists(null));

		});
	}

	@Test
	@UnitTestMethod(name = "isEmpty", args = {})
	public void testIsEmpty() {

		testConsumer(1194219972474251585L,(c) -> {

			PartitionDataManager partitionDataManager = new PartitionDataManager();
			
			//show that the manager is initially empty
			assertTrue(partitionDataManager.isEmpty());
			
			//show that the manager is not empty after adding a partition
			Object key = new Object();
			AgentId agentId = c.getCurrentAgentId();
			Partition partition = Partition.builder().build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
			partitionDataManager.addPartition(key, agentId, populationPartition);

			assertFalse(partitionDataManager.isEmpty());
			
			
			//show that the manager is empty after the partition is removed
			partitionDataManager.removePartition(key);
			assertTrue(partitionDataManager.isEmpty());

			
		});
	}

	@Test
	@UnitTestMethod(name = "removePartition", args = { Object.class })
	public void testRemovePartition() {

		testConsumer(5767679585616452606L,(c) -> {

			PartitionDataManager partitionDataManager = new PartitionDataManager();
			Object key = new Object();	
			
			assertFalse(partitionDataManager.partitionExists(key));

			AgentId agentId = c.getCurrentAgentId();
			Partition partition = Partition.builder().build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(c, partition);
			partitionDataManager.addPartition(key, agentId, populationPartition);

			assertTrue(partitionDataManager.partitionExists(key));
			
			//show that removing an unknown partition has no effect
			partitionDataManager.removePartition(new Object());
			assertEquals(1,partitionDataManager.getKeys().size());
			
			//show that removing a null keyed partition has no effect
			partitionDataManager.removePartition(null);
			assertEquals(1,partitionDataManager.getKeys().size());
			
			
			//show that partition is removed
			partitionDataManager.removePartition(key);
			assertFalse(partitionDataManager.partitionExists(key));
			assertEquals(0,partitionDataManager.getKeys().size());
			
		});
	}

}