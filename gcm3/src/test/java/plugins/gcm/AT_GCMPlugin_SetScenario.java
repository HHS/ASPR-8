package plugins.gcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ReplicationImpl;
import plugins.gcm.input.Scenario;
import plugins.gcm.input.ScenarioBuilder;
import plugins.gcm.input.UnstructuredScenarioBuilder;
import plugins.gcm.support.EnvironmentSupport;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link Simulation}
 * 
 * This test class is incomplete and only tests
 * {@link Simulation#setScenario(Scenario)} demonstrating that the contents of a
 * Scenario are properly reflected in the content of the Environment available
 * to Components at initialization.
 * 
 * A test is executed for each method of the Scenario interface rather that a
 * direct test of {@link Simulation#setScenario(Scenario)}. Each test uses some
 * degree of randomization, with emphasis on not using contiguous, zero-based
 * identifiers for people, groups, batches, stages and other Integer-based
 * elements. The simulation is expected to handle these identifiers by
 * translating them into a zero-based contiguous set of IntId identifiers that
 * preserves value order rather than discovery order.
 * 
 * The general methodology of the tests is to construct a scenario containing
 * the minimum information to achieve a well formed scenario with the data to be
 * tested. A ScenarioTest(a simple interface for making relevant assertions) is
 * then created and both are submitted to the static executeScenarioTest()
 * method.
 * 
 * The executeScenarioTest() method creates a simulation instance with the
 * relevant components and creates one special component to execute the
 * ScenarioTest during its component initialization.
 * 
 * As a way to simplify the unit tests, ScenarioTests generally seek to show
 * that the specific content in Scenario is found in the Environment and not
 * that content in the Environment is also matched by content in the scenario.
 * These one-to-one and onto relationships are instead only tested in the unit
 * tests that specifically focus on methods that return collections of
 * identifiers.
 * 
 * The contract of the simulation requires that it adopt all the data in the
 * scenario, including person, group, batch and stage identifiers. These are all
 * Integer-based and the scenario is free to have ANY such values, including
 * negatives. Since there are potentially many such identifiers, it is not
 * practical for the simulation to contain the scenario's Integer identifiers
 * exactly as given. For example, the scenario may have people numbered from
 * -1,000,000 to 0 and have another batch numbered from 100,000,000 to
 * 123,000,000.
 * 
 * The simulation instead maps the given identifiers to a contiguous range
 * starting from zero. This map preserves the order of the values rather than
 * the order of their discovery from the scenario. Thus the mapping of an
 * identifier from the scenario to the simulation is predictable.
 * 
 * All tests in this test class use randomized, non-contiguous Integer
 * identifiers whenever practicable. There are various static methods to support
 * identifier mappings from the scenario to the expected values in the
 * simulation.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = GCMPlugin.class)
public class AT_GCMPlugin_SetScenario {

	/*
	 * Each test contributes a custom implementation ScenarioTest(usually by
	 * Lambda expression) that contains the assertions of the unit test
	 */
	private static interface ScenarioTest {
		public void test(Scenario scenario, Environment environment);
	}

	private static Scenario SCENARIO;
	private static ScenarioTest SCENARIO_TEST;

	/*
	 * A Component implementor that executes a ScenarioTest during its init()
	 */
	public static class ScenarioTestComponent extends AbstractComponent {
		@Override
		protected void init(Environment environment) {
			SCENARIO_TEST.test(SCENARIO, environment);
		}

	}

	/*
	 * A Component implementor that acts as a placeholder for components not
	 * expected to execute activities relevant to the tests.
	 */
	public static class EmptyComponent extends AbstractComponent {

	}

	/*
	 * Executes the given scenario test by executing a simulation instance
	 * containing a special global component that will in turn execute the
	 * scenario test during its component initialization. The scenario must
	 * contain the global component identifier:SCENARIO_TEST_COMPONENT_ID
	 */
	private static void executeScenarioTest(Scenario scenario, ScenarioTest scenarioTest) {
		SCENARIO = scenario;
		SCENARIO_TEST = scenarioTest;
		EnvironmentSupport.executeSimulation(scenario, new ReplicationImpl(new ReplicationId(0), 1234L));
	}

	/*
	 * Returns an UnstructuredScenarioBuilder that contains the global component
	 * identifier:SCENARIO_TEST_COMPONENT_ID
	 */
	private static UnstructuredScenarioBuilder getScenarioBuilder() {
		UnstructuredScenarioBuilder result = new UnstructuredScenarioBuilder();
		result.addGlobalComponentId(SCENARIO_TEST_COMPONENT_ID, () -> new ScenarioTestComponent()::init);
		return result;
	}

	private final static GlobalComponentId SCENARIO_TEST_COMPONENT_ID = new GlobalComponentId() {
	};

	private static SeedProvider SEED_PROVIDER = new SeedProvider(3664523477344523234L);

	/*
	 * Creates a RandomGenerator from the provided seed case number.
	 */
	private static RandomGenerator getRandomGenerator(int seedCase) {
		return SeedProvider.getRandomGenerator(SEED_PROVIDER.getSeedValue(seedCase));
	}

	private static Map<GroupId, GroupId> getGroupIdMap(Collection<GroupId> ids) {
		List<GroupId> list = new ArrayList<>(ids);
		Collections.sort(list);
		Map<GroupId, GroupId> result = new LinkedHashMap<>();
		for (GroupId id : list) {
			result.put(id, new GroupId(result.size()));
		}
		return result;
	}

	private static Map<StageId, StageId> getStageIdMap(Collection<StageId> ids) {
		List<StageId> list = new ArrayList<>(ids);
		Collections.sort(list);
		Map<StageId, StageId> result = new LinkedHashMap<>();
		for (StageId id : list) {
			result.put(id, new StageId(result.size()));
		}
		return result;
	}

	private static Map<BatchId, BatchId> getBatchIdMap(Collection<BatchId> ids) {
		List<BatchId> list = new ArrayList<>(ids);
		Collections.sort(list);
		Map<BatchId, BatchId> result = new LinkedHashMap<>();
		for (BatchId id : list) {
			result.put(id, new BatchId(result.size()));
		}
		return result;
	}

	private static Map<PersonId, PersonId> getPersonIdMap(Collection<PersonId> ids) {
		List<PersonId> list = new ArrayList<>(ids);
		Collections.sort(list);
		Map<PersonId, PersonId> result = new LinkedHashMap<>();
		for (PersonId id : list) {
			result.put(id, new PersonId(result.size()));
		}
		return result;
	}

	/*
	 * Returns a list of unique, non-negative Integer values containing count
	 * unique elements. Entries are chosen from the interval [0,maxId)
	 */
	private static List<Integer> getScrambledIds(int count, int maxId, RandomGenerator randomGenerator) {
		if (count < 0) {
			throw new RuntimeException("negative count");
		}
		if (count > maxId) {
			throw new RuntimeException("count exceeds maxId");
		}
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < maxId; i++) {
			list.add(i);
		}
		Random random = new Random(randomGenerator.nextLong());
		Collections.shuffle(list, random);
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			result.add(list.get(i));
		}

		return result;
	}

	private static List<StageId> getScrambledStageIds(int count, int maxId, RandomGenerator randomGenerator) {
		return getStageIds(getScrambledIds(count, maxId, randomGenerator));
	}

	private static List<BatchId> getScrambledBatchIds(int count, int maxId, RandomGenerator randomGenerator) {
		return getBatchIds(getScrambledIds(count, maxId, randomGenerator));
	}

	private static List<BatchId> getBatchIds(List<Integer> list) {
		List<BatchId> result = new ArrayList<>();
		for (Integer value : list) {
			result.add(new BatchId(value));
		}
		return result;
	}

	private static List<StageId> getStageIds(List<Integer> list) {
		List<StageId> result = new ArrayList<>();
		for (Integer value : list) {
			result.add(new StageId(value));
		}
		return result;
	}

	private static List<GroupId> getScrambledGroupIds(int count, int maxId, RandomGenerator randomGenerator) {
		return getGroupIds(getScrambledIds(count, maxId, randomGenerator));
	}

	private static List<PersonId> getScrambledPersonIds(int count, int maxId, RandomGenerator randomGenerator) {
		return getPersonIds(getScrambledIds(count, maxId, randomGenerator));
	}

	private static List<PersonId> getPersonIds(List<Integer> list) {
		List<PersonId> result = new ArrayList<>();
		for (Integer value : list) {
			result.add(new PersonId(value));
		}
		return result;
	}

	private static List<GroupId> getGroupIds(List<Integer> list) {
		List<GroupId> result = new ArrayList<>();
		for (Integer value : list) {
			result.add(new GroupId(value));
		}
		return result;
	}

	/*
	 * Returns a randomized property value consistent with the given property
	 * definition.
	 */
	private static Object generatePropertyValue(final PropertyDefinition propertyDefinition, final RandomGenerator randomGenerator) {

		final Class<?> type = propertyDefinition.getType();

		if (type == Boolean.class) {
			return randomGenerator.nextBoolean();
		} else if (type == Integer.class) {
			return randomGenerator.nextInt();
		} else if (type == String.class) {
			return "String " + randomGenerator.nextInt();
		} else if (type == Long.class) {
			return randomGenerator.nextLong();
		} else {
			throw new RuntimeException("unknown type " + type);
		}
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterAll
	public static void afterClass() {
		// if (SEED_PROVIDER.hasUnusedSeeds()) {
		// System.out.println(SEED_PROVIDER.generateUnusedSeedReport());
		// }
	}

	private void testGetBatchAmount() {
		RandomGenerator randomGenerator = getRandomGenerator(0);

		/*
		 * Create a scenario with 100 randomized batches
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<BatchId> batchIds = getScrambledBatchIds(100, 300, randomGenerator);

		for (BatchId batchId : batchIds) {
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble() * 1000;
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
		}

		/*
		 * Show that the resource amount in each batch in the scenario is
		 * reflected in the environment.
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<BatchId, BatchId> idMap = getBatchIdMap(scenario.getBatchIds());
			assertTrue(scenario.getBatchIds().size() > 0);
			for (BatchId scenarioBatchId : scenario.getBatchIds()) {
				BatchId simulationBatchId = idMap.get(scenarioBatchId);
				assertEquals(scenario.getBatchAmount(scenarioBatchId).doubleValue(), environment.getBatchAmount(simulationBatchId), 0);
			}
		});
	}

	private void testGetBatchIds() {
		RandomGenerator randomGenerator = getRandomGenerator(1);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<BatchId> batchIds = getScrambledBatchIds(100, 300, randomGenerator);

		for (BatchId batchId : batchIds) {
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble() * 1000;
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
		}

		// Show that the batch ids in the scenario have a one to one
		// correspondence to the environments batches
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// determine the expected batch ids in the environment
			Map<BatchId, BatchId> idMap = getBatchIdMap(scenario.getBatchIds());
			Set<BatchId> expectedSimulationBatchIds = new LinkedHashSet<>(idMap.values());

			// determine the actual batch ids in the environment
			Set<BatchId> actualSimulationBatchIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : environment.getMaterialsProducerIds()) {
				actualSimulationBatchIds.addAll(environment.getInventoryBatches(materialsProducerId));
				for (StageId stageId : environment.getStages(materialsProducerId)) {
					actualSimulationBatchIds.addAll(environment.getStageBatches(stageId));
				}
			}

			assertTrue(expectedSimulationBatchIds.size() > 0);
			assertEquals(expectedSimulationBatchIds, actualSimulationBatchIds);

		});
	}

	private void testGetBatchMaterial() {
		RandomGenerator randomGenerator = getRandomGenerator(2);

		/*
		 * Create a scenario with 100 randomized batches
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<BatchId> batchIds = getScrambledBatchIds(100, 300, randomGenerator);

		for (BatchId batchId : batchIds) {
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble() * 1000;
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
		}

		/*
		 * Show that the material in each batch in the scenario is reflected in
		 * the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<BatchId, BatchId> idMap = getBatchIdMap(scenario.getBatchIds());
			assertTrue(scenario.getBatchIds().size() > 0);
			for (BatchId scenarioBatchId : scenario.getBatchIds()) {
				BatchId simulationBatchId = idMap.get(scenarioBatchId);
				assertEquals((Object) scenario.getBatchMaterial(scenarioBatchId), environment.getBatchMaterial(simulationBatchId));
			}
		});
	}

	private void testGetBatchMaterialsProducer() {
		RandomGenerator randomGenerator = getRandomGenerator(3);

		/*
		 * Create a scenario with 100 randomized batches
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<BatchId> batchIds = getScrambledBatchIds(100, 300, randomGenerator);

		for (BatchId batchId : batchIds) {
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble() * 1000;
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
		}

		/*
		 * Show that the materials producer for each batch in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<BatchId, BatchId> idMap = getBatchIdMap(scenario.getBatchIds());
			assertTrue(scenario.getBatchIds().size() > 0);
			for (BatchId scenarioBatchId : scenario.getBatchIds()) {
				BatchId simulationBatchId = idMap.get(scenarioBatchId);
				MaterialsProducerId sceanrioMaterialsProduerId = scenario.getBatchMaterialsProducer(scenarioBatchId);
				MaterialsProducerId simulationMaterialsProduerId = environment.getBatchProducer(simulationBatchId);
				assertEquals(sceanrioMaterialsProduerId, simulationMaterialsProduerId);
			}
		});
	}

	/*
	 * Returns a randomized property definition with randomized type and default
	 * value
	 */
	private static PropertyDefinition buildPropertyDefinition(RandomGenerator randomGenerator, Boolean propertyOverrides) {
		Class<?> type;
		final int typeCase = randomGenerator.nextInt(4);
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = randomGenerator.nextBoolean();
			break;
		case 1:
			type = Integer.class;
			defaultValue = randomGenerator.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + randomGenerator.nextInt();
			break;
		default:
			type = Long.class;
			defaultValue = randomGenerator.nextLong();
			break;
		}
		boolean propertyValuesMayOverrideDefaultValue = randomGenerator.nextBoolean();
		if (propertyOverrides != null) {
			propertyValuesMayOverrideDefaultValue = propertyOverrides;
		}

		final PropertyDefinition result = PropertyDefinition.builder()//
															.setType(type)//
															.setDefaultValue(defaultValue)//
															.setPropertyValueMutability(propertyValuesMayOverrideDefaultValue)//
															.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
															.build();
		return result;
	}

	private void testGetBatchPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(4);

		/*
		 * Create a scenario with various batch property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
			for (TestBatchPropertyId batchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that each batch property definition in the scenario is reflected
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getMaterialIds().size() > 0);
			for (MaterialId materialId : scenario.getMaterialIds()) {
				assertTrue(scenario.getBatchPropertyIds(materialId).size() > 0);
				for (BatchPropertyId batchPropertyId : scenario.getBatchPropertyIds(materialId)) {
					PropertyDefinition scenarioBatchPropertyDefinition = scenario.getBatchPropertyDefinition(materialId, batchPropertyId);
					PropertyDefinition simulationBatchPropertyDefinition = environment.getBatchPropertyDefinition(materialId, batchPropertyId);
					assertEquals(scenarioBatchPropertyDefinition, simulationBatchPropertyDefinition);
				}
			}
		});
	}

	private void testGetBatchPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(5);

		/*
		 * Create a scenario with various batch property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
			for (TestBatchPropertyId batchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that each batch property id in the scenario is matched with one
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getMaterialIds().size() > 0);
			for (MaterialId materialId : scenario.getMaterialIds()) {
				Set<BatchPropertyId> expectedBatchPropertyIds = scenario.getBatchPropertyIds(materialId);
				Set<BatchPropertyId> actualBatchPropertyIds = environment.getBatchPropertyIds(materialId);
				assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			}
		});
	}

	private void testGetBatchPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(6);

		/*
		 * Create a scenario with 100 randomized batches
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
			for (TestBatchPropertyId batchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
			}
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
			for (TestBatchPropertyId batchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				PropertyDefinition propertyDefinition = subScenario.getBatchPropertyDefinition(testMaterialId, batchPropertyId);
				scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
			}
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}

		List<BatchId> batchIds = getScrambledBatchIds(100, 300, randomGenerator);

		for (BatchId batchId : batchIds) {
			TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble() * 1000;
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
			for (TestBatchPropertyId batchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId )) {
				PropertyDefinition propertyDefinition = subScenario.getBatchPropertyDefinition(testMaterialId, batchPropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
			}
		}

		/*
		 * Show that each property value of each batch in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<BatchId, BatchId> idMap = getBatchIdMap(scenario.getBatchIds());
			assertTrue(scenario.getBatchIds().size() > 0);
			for (BatchId scenarioBatchId : scenario.getBatchIds()) {
				MaterialId materialId = scenario.getBatchMaterial(scenarioBatchId);
				Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
				assertTrue(batchPropertyIds.size() > 0);
				BatchId simulationBatchId = idMap.get(scenarioBatchId);
				for (BatchPropertyId batchPropertyId : batchPropertyIds) {
					Object expectedPropertyValue = scenario.getBatchPropertyValue(scenarioBatchId, batchPropertyId);
					Object actualPropertyValue = environment.getBatchPropertyValue(simulationBatchId, batchPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
				}
			}
		});
	}

	private void testGetCompartmentPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(7);

		/*
		 * Create a scenario with various compartment property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that each compartment property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<CompartmentId> compartmentIds = scenario.getCompartmentIds();
			assertTrue(compartmentIds.size() > 0);
			for (CompartmentId compartmentId : compartmentIds) {
				Set<CompartmentPropertyId> compartmentPropertyIds = scenario.getCompartmentPropertyIds(compartmentId);
				assertTrue(compartmentPropertyIds.size() > 0);
				for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
					PropertyDefinition actualPropertyDefinition = environment.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}
		});
	}

	private void testGetCompartmentPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(8);

		/*
		 * Create a scenario with various compartment property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
			for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that compartment property identifiers in the scenario match
		 * those in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<CompartmentId> compartmentIds = scenario.getCompartmentIds();
			assertTrue(compartmentIds.size() > 0);
			for (CompartmentId compartmentId : compartmentIds) {
				Set<CompartmentPropertyId> compartmentPropertyIds = scenario.getCompartmentPropertyIds(compartmentId);
				assertTrue(compartmentPropertyIds.size() > 0);
				assertEquals(scenario.getCompartmentPropertyIds(compartmentId), environment.getCompartmentPropertyIds(compartmentId));
			}
		});
	}

	private void testGetCompartmentPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(9);

		/*
		 * Create a scenario with various compartment property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
			for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();

		for (CompartmentId compartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
			for (CompartmentPropertyId compartmentPropertyId : subScenario.getCompartmentPropertyIds(compartmentId)) {
				PropertyDefinition propertyDefinition = subScenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
			}
		}
		Scenario build = scenarioBuilder.build();

		/*
		 * Show that each compartment property value in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(build, (scenario, environment) -> {
			// show that there were some compartments
			assertTrue(scenario.getCompartmentIds().size() > 0);
			for (CompartmentId compartmentId : scenario.getCompartmentIds()) {
				Set<CompartmentPropertyId> compartmentPropertyIds = scenario.getCompartmentPropertyIds(compartmentId);
				assertTrue(compartmentPropertyIds.size() > 0);
				for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
					Object expectedCompartmentPropertyValue = scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
					Object actualCompartmentPropertyValue = environment.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
					assertEquals(expectedCompartmentPropertyValue, actualCompartmentPropertyValue);
				}
			}
		});
	}

	private void testGetGlobalPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(10);

		/*
		 * Create a scenario with various global property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		}

		/*
		 * Show that each global property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getGlobalPropertyIds().size() > 0);
			for (GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
				PropertyDefinition actualPropertyDefinition = environment.getGlobalPropertyDefinition(globalPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	private void testGetGlobalPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(11);

		/*
		 * Create a scenario with various global property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		}

		/*
		 * Show that each global property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// show that the test is valid
			assertTrue(scenario.getGlobalPropertyIds().size() > 0);

			assertEquals(scenario.getGlobalPropertyIds(), environment.getGlobalPropertyIds());

		});
	}

	private void testGetGlobalPropertyValue() {

		RandomGenerator randomGenerator = getRandomGenerator(12);

		/*
		 * Create a scenario with various global property definitions and values
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		}

		Scenario subScenario = scenarioBuilder.build();
		scenarioBuilder = getScenarioBuilder();

		for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = subScenario.getGlobalPropertyDefinition(globalPropertyId);
			scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
			Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
			scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		}

		/*
		 * Show that each global property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// show that the test is valid
			assertTrue(scenario.getGlobalPropertyIds().size() > 0);
			for (GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
				Object expectedPropertyValue = scenario.getGlobalPropertyValue(globalPropertyId);
				Object actualPropertyValue = environment.getGlobalPropertyValue(globalPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);
			}
		});
	}

	private void testGetGroupIds() {
		RandomGenerator randomGenerator = getRandomGenerator(13);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		List<GroupId> groupIds = getScrambledGroupIds(100, 300, randomGenerator);

		for (GroupId groupId : groupIds) {
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			scenarioBuilder.addGroup(groupId, testGroupTypeId);
		}

		// Show that the group ids match those in the environment and have the
		// expected group type
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
			Map<GroupId, GroupId> idMap = getGroupIdMap(scenario.getGroupIds());
			for (GroupId groupId : scenario.getGroupIds()) {
				expectedGroupIds.add(idMap.get(groupId));
			}
			// validate the test by showing there were at least one group
			assertTrue(expectedGroupIds.size() > 0);

			Set<GroupId> actualGroupIds = new LinkedHashSet<>(environment.getGroupIds());
			assertEquals(expectedGroupIds, actualGroupIds);
			Set<GroupTypeId> encounteredGroupTypes = new LinkedHashSet<>();
			for (GroupId scenarioGroupId : scenario.getGroupIds()) {
				GroupTypeId expectedGroupType = scenario.getGroupTypeId(scenarioGroupId);
				GroupId simulationGroupId = idMap.get(scenarioGroupId);
				GroupTypeId actualGroupType = environment.getGroupType(simulationGroupId);
				assertEquals(expectedGroupType, actualGroupType);
				encounteredGroupTypes.add(actualGroupType);
			}
			// validate the test by showing that there were multiple group types
			assertTrue(encounteredGroupTypes.size() > 1);

		});
	}

	private void testGetGroupMembers() {
		RandomGenerator randomGenerator = getRandomGenerator(14);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		// add some groups
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		List<GroupId> groupIds = getScrambledGroupIds(10, 30, randomGenerator);

		for (GroupId groupId : groupIds) {
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			scenarioBuilder.addGroup(groupId, testGroupTypeId);
		}
		// add some people
		List<PersonId> personIds = getScrambledPersonIds(1000, 3000, randomGenerator);
		for (PersonId personId : personIds) {
			TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			TestCompartmentId testCompartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, testRegionId, testCompartmentId);
		}

		// assign each person to 1 to 4 groups at random
		Random random = new Random(randomGenerator.nextLong());
		for (PersonId personId : personIds) {
			int groupCount = randomGenerator.nextInt(4) + 1;
			Collections.shuffle(groupIds, random);
			for (int i = 0; i < groupCount; i++) {
				GroupId groupId = groupIds.get(i);
				scenarioBuilder.addPersonToGroup(groupId, personId);
			}
		}

		// add the compartments and regions needed for the people
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}
		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}

		// Show that each group ids in the scenario has the expected person
		// membership in the environment
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {

			Set<GroupId> scenarioGroupIds = scenario.getGroupIds();
			// validate that the test has some groups
			assertTrue(scenarioGroupIds.size() > 0);
			Map<GroupId, GroupId> groupIdMap = getGroupIdMap(scenarioGroupIds);
			Set<PersonId> scenarioPersonIds = scenario.getPeopleIds();
			// validate that the test has some people
			assertTrue(scenarioPersonIds.size() > 0);
			Map<PersonId, PersonId> personIdMap = getPersonIdMap(scenarioPersonIds);
			boolean encounterdNonEmptyGroup = false;
			for (GroupId scenarioGroupId : scenarioGroupIds) {
				Set<PersonId> scenarioPersonIdsForGroup = scenario.getGroupMembers(scenarioGroupId);
				Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
				for (PersonId scenarioPersonId : scenarioPersonIdsForGroup) {
					PersonId simulationPersonId = personIdMap.get(scenarioPersonId);
					expectedPersonIds.add(simulationPersonId);
				}
				encounterdNonEmptyGroup |= expectedPersonIds.size() > 0;
				GroupId simulationGroupId = groupIdMap.get(scenarioGroupId);
				LinkedHashSet<PersonId> actualPersonIds = new LinkedHashSet<>(environment.getPeopleForGroup(simulationGroupId));
				assertEquals(expectedPersonIds, actualPersonIds);
			}
			// show that the test encountered at least one non-empty group
			assertTrue(encounterdNonEmptyGroup);

		});
	}

	private void testGetGroupPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(15);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			for (GroupPropertyId groupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineGroupProperty(testGroupTypeId, groupPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that the group property definitions in the scenario are found in
		 * the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<GroupTypeId> groupTypeIds = scenario.getGroupTypeIds();
			for (GroupTypeId groupTypeId : groupTypeIds) {
				Set<GroupPropertyId> groupPropertyIds = scenario.getGroupPropertyIds(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = scenario.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					PropertyDefinition actualPropertyDefinition = environment.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

		});
	}

	private void testGetGroupPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(16);

		// build a scenario with just the group properties defined
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			for (GroupPropertyId groupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineGroupProperty(testGroupTypeId, groupPropertyId, propertyDefinition);
			}
		}
		Scenario subScenario = scenarioBuilder.build();

		// build a scenario with assigned group property values
		scenarioBuilder = getScenarioBuilder();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

			for (GroupPropertyId groupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				PropertyDefinition propertyDefinition = subScenario.getGroupPropertyDefinition(testGroupTypeId, groupPropertyId);
				scenarioBuilder.defineGroupProperty(testGroupTypeId, groupPropertyId, propertyDefinition);
			}
		}

		List<GroupId> groupIds = getScrambledGroupIds(10, 30, randomGenerator);

		for (GroupId groupId : groupIds) {
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			scenarioBuilder.addGroup(groupId, testGroupTypeId);
			for (GroupPropertyId groupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				PropertyDefinition propertyDefinition = subScenario.getGroupPropertyDefinition(testGroupTypeId, groupPropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
			}
		}

		/*
		 * Show that the group property values from the scenario are found in
		 * the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<GroupId, GroupId> idMap = getGroupIdMap(scenario.getGroupIds());
			// validate that the test has groups
			assertTrue(scenario.getGroupIds().size() > 0);
			for (GroupId scenarioGroupId : scenario.getGroupIds()) {
				GroupTypeId groupTypeId = scenario.getGroupTypeId(scenarioGroupId);
				// validate the group has properties
				assertTrue(scenario.getGroupPropertyIds(groupTypeId).size() > 0);
				for (GroupPropertyId groupPropertyId : scenario.getGroupPropertyIds(groupTypeId)) {
					Object expectedPropertyValue = scenario.getGroupPropertyValue(scenarioGroupId, groupPropertyId);
					GroupId simulationGroupId = idMap.get(scenarioGroupId);
					Object actualPropertyValue = environment.getGroupPropertyValue(simulationGroupId, groupPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
				}
			}

		});
	}

	private void testGetGroupTypeId() {
		RandomGenerator randomGenerator = getRandomGenerator(17);

		// build a scenario with some groups
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		List<GroupId> groupIds = getScrambledGroupIds(100, 300, randomGenerator);

		for (GroupId groupId : groupIds) {
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			scenarioBuilder.addGroup(groupId, testGroupTypeId);
		}

		/*
		 * Show that the group types for each group in the scenario match those
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<GroupId, GroupId> idMap = getGroupIdMap(scenario.getGroupIds());
			// validate that the test has groups
			assertTrue(scenario.getGroupIds().size() > 0);
			for (GroupId scenarioGroupId : scenario.getGroupIds()) {
				GroupTypeId expectedGroupTypeId = scenario.getGroupTypeId(scenarioGroupId);
				GroupId simulationGroupId = idMap.get(scenarioGroupId);
				GroupTypeId actualGroupTypeId = environment.getGroupType(simulationGroupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}
		});
	}

	private void testGetGroupTypeIds() {
		// build a scenario with some group types
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		/*
		 * Show that the group types from the scenario match those in the
		 * environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {

			// validate that the test has groups
			assertTrue(scenario.getGroupTypeIds().size() > 0);
			Set<GroupTypeId> expectedGroupTypeIds = scenario.getGroupTypeIds();
			Set<GroupTypeId> actualGroupTypeIds = new LinkedHashSet<>(environment.getGroupTypeIds());
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);
		});
	}

	private void testGetGroupPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(18);

		/*
		 * Create a scenario with group types properties defined
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			for (GroupPropertyId groupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				scenarioBuilder.defineGroupProperty(testGroupTypeId, groupPropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that the group property identifiers in the scenario are found in
		 * the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<GroupTypeId> groupTypeIds = scenario.getGroupTypeIds();
			assertTrue(groupTypeIds.size() > 0);
			for (GroupTypeId groupTypeId : groupTypeIds) {
				Set<GroupPropertyId> expectedGroupPropertyIds = scenario.getGroupPropertyIds(groupTypeId);
				Set<GroupPropertyId> actualGroupPropertyIds = environment.getGroupPropertyIds(groupTypeId);
				assertTrue(expectedGroupPropertyIds.size() > 0);
				assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
			}
		});
	}

	private void testGetMaterialIds() {
		/*
		 * Create a scenario with materials
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		/*
		 * Show that the material in the scenario are those found in the
		 * environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<MaterialId> expectedMaterialIds = scenario.getMaterialIds();
			Set<MaterialId> actualMaterialIds = environment.getMaterialIds();
			// validate that there is at least one material in the scenario
			assertTrue(expectedMaterialIds.size() > 0);
			assertEquals(expectedMaterialIds, actualMaterialIds);
		});
	}

	private void testGetComponentIds() {
		/*
		 * Create a scenario with some materials producers
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}

		/*
		 * Show that the material producer identifiers in the scenario are those
		 * found in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<MaterialsProducerId> expectedMaterialsProducerIds = scenario.getMaterialsProducerIds();
			Set<MaterialsProducerId> actualMaterialsProducerIds = environment.getMaterialsProducerIds();
			// validate that there is at least one materials producer in the
			// scenario
			assertTrue(expectedMaterialsProducerIds.size() > 0);
			assertEquals(expectedMaterialsProducerIds, actualMaterialsProducerIds);
		});
	}

	private void testGetMaterialsProducerPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(19);
		/*
		 * Create a scenario with some materials producer property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
		}

		/*
		 * Show that the material producer definitions in the scenario are found
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one materials producer property
			// to test
			assertTrue(scenario.getMaterialsProducerPropertyIds().size() > 0);
			for (MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
				PropertyDefinition expectedMaterialsProducerPropertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
				PropertyDefinition actualMaterialsProducerPropertyDefinition = environment.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
				assertEquals(expectedMaterialsProducerPropertyDefinition, actualMaterialsProducerPropertyDefinition);
			}
		});
	}

	private void testGetMaterialsProducerPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(20);
		/*
		 * Create a scenario with some materials producer property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
		}

		/*
		 * Show that the materials producer property identifiers in the scenario
		 * are those found in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one materials producer property
			// to test
			assertTrue(scenario.getMaterialsProducerPropertyIds().size() > 0);
			Set<MaterialsProducerPropertyId> expectedMaterialsProducerPropertyIds = scenario.getMaterialsProducerPropertyIds();
			Set<MaterialsProducerPropertyId> actualMaterialsProducerPropertyIds = environment.getMaterialsProducerPropertyIds();
			assertEquals(expectedMaterialsProducerPropertyIds, actualMaterialsProducerPropertyIds);

		});
	}

	private void testGetMaterialsProducerPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(21);
		/*
		 * Create a scenario with some materials producer's having property
		 * values
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = subScenario.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
			scenarioBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				PropertyDefinition propertyDefinition = subScenario.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue);
			}
		}

		/*
		 * Show that the materials producer property values in the scenario are
		 * those found in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one materials producer in the
			// test
			assertTrue(scenario.getMaterialsProducerIds().size() > 0);
			// validate that there is at least one materials producer property
			// in the test
			assertTrue(scenario.getMaterialsProducerPropertyIds().size() > 0);
			for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
					Object expectedMaterialsProducerPropertyValue = scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					Object actualMaterialsProducerPropertyValue = environment.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					assertEquals(expectedMaterialsProducerPropertyValue, actualMaterialsProducerPropertyValue);
				}
			}
		});
	}

	private void testGetMaterialsProducerResourceLevel() {
		RandomGenerator randomGenerator = getRandomGenerator(22);
		/*
		 * Create a scenario with some materials producers having non-zero
		 * resource levels
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				long amount = randomGenerator.nextInt(1000) + 1;
				scenarioBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, amount);
			}
		}

		/*
		 * Show that the materials producer resource levels in the scenario are
		 * those found in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one materials producer in the
			// test
			assertTrue(scenario.getMaterialsProducerIds().size() > 0);
			// validate that there is at least one resource the test
			assertTrue(scenario.getResourceIds().size() > 0);
			for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
				for (ResourceId resourceId : scenario.getResourceIds()) {
					Long expectedMaterialsProducerResourceLevel = scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					Long actualMaterialsProducerResourceLevel = environment.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					assertEquals(expectedMaterialsProducerResourceLevel, actualMaterialsProducerResourceLevel);
				}
			}
		});
	}

	private void testGetPeopleIds() {
		RandomGenerator randomGenerator = getRandomGenerator(23);
		/*
		 * Create a scenario with some people
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		List<PersonId> personIds = getScrambledPersonIds(1000, 5000, randomGenerator);

		for (PersonId personId : personIds) {
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, regionId, compartmentId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}

		/*
		 * Show that the people in the scenario match those in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one person in the test
			assertTrue(scenario.getPeopleIds().size() > 0);
			Map<PersonId, PersonId> idMap = getPersonIdMap(scenario.getPeopleIds());
			Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
			for (PersonId scenarioPersonId : scenario.getPeopleIds()) {
				PersonId simulationPersonId = idMap.get(scenarioPersonId);
				expectedPersonIds.add(simulationPersonId);
			}
			Set<PersonId> actualPersonIds = new LinkedHashSet<>(environment.getPeople());
			assertEquals(expectedPersonIds, actualPersonIds);
		});
	}

	private void testGetPersonCompartment() {
		RandomGenerator randomGenerator = getRandomGenerator(24);
		/*
		 * Create a scenario with some people
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		List<PersonId> personIds = getScrambledPersonIds(1000, 5000, randomGenerator);

		for (PersonId personId : personIds) {
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, regionId, compartmentId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}

		/*
		 * Show that the compartments assigned to people in the scenario are
		 * those assigned in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one person in the test
			assertTrue(scenario.getPeopleIds().size() > 0);
			Map<PersonId, PersonId> idMap = getPersonIdMap(scenario.getPeopleIds());
			for (PersonId scenarioPersonId : scenario.getPeopleIds()) {
				PersonId simulationPersonId = idMap.get(scenarioPersonId);
				CompartmentId expectedPersonCompartment = scenario.getPersonCompartment(scenarioPersonId);
				CompartmentId actualPersonCompartment = environment.getPersonCompartment(simulationPersonId);
				assertEquals(expectedPersonCompartment, actualPersonCompartment);
			}
		});
	}

	private void testGetPersonCompartmentArrivalTrackingPolicy() {

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			/*
			 * Create scenario with the compartment arrival tracking time policy
			 * set
			 */
			ScenarioBuilder scenarioBuilder = getScenarioBuilder();
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);

			/*
			 * Show that the compartment arrival tracking time policy in the
			 * scenario matches the one in the environment
			 */
			executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
				assertEquals(scenario.getPersonCompartmentArrivalTrackingPolicy(), environment.getPersonCompartmentArrivalTrackingPolicy());
			});
		}
	}

	private void testGetPersonPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(25);

		/*
		 * Create a scenario person properties defined
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (PersonPropertyId personPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		}

		/*
		 * Show that each person property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getPersonPropertyIds().size() > 0);
			for (PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
				PropertyDefinition actualPropertyDefinition = environment.getPersonPropertyDefinition(personPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	private void testGetPersonPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(26);

		/*
		 * Create a scenario person properties defined
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (PersonPropertyId personPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		}

		/*
		 * Show that the person property ids in the scenario are those in the
		 * environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getPersonPropertyIds().size() > 0);
			Set<PersonPropertyId> expectedPersonPropertyIds = scenario.getPersonPropertyIds();
			Set<PersonPropertyId> actualPersonPropertyIds = environment.getPersonPropertyIds();
			assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);
		});
	}

	private void testGetPersonPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(27);
		/*
		 * Create a scenario with some people having various person properties
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (PersonPropertyId personPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();
		List<PersonId> personIds = getScrambledPersonIds(1000, 5000, randomGenerator);

		for (PersonId personId : personIds) {
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, regionId, compartmentId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}

		for (PersonPropertyId personPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = subScenario.getPersonPropertyDefinition(personPropertyId);
			scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		}
		for (PersonId personId : personIds) {
			for (PersonPropertyId personPropertyId : TestPersonPropertyId.values()) {
				PropertyDefinition propertyDefinition = subScenario.getPersonPropertyDefinition(personPropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
			}
		}

		/*
		 * Show that the person property values for each person in the scenario
		 * are those in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one person in the test
			assertTrue(scenario.getPeopleIds().size() > 0);

			// validate that there is at least one person property defined
			assertTrue(scenario.getPersonPropertyIds().size() > 0);

			Map<PersonId, PersonId> idMap = getPersonIdMap(scenario.getPeopleIds());
			for (PersonId scenarioPersonId : scenario.getPeopleIds()) {
				PersonId simulationPersonId = idMap.get(scenarioPersonId);
				for (PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
					Object expectedPersonPropertyValue = scenario.getPersonPropertyValue(scenarioPersonId, personPropertyId);
					Object actualPersonPropertyValue = environment.getPersonPropertyValue(simulationPersonId, personPropertyId);
					assertEquals(expectedPersonPropertyValue, actualPersonPropertyValue);
				}
			}
		});
	}

	private void testGetPersonRegion() {
		RandomGenerator randomGenerator = getRandomGenerator(28);
		/*
		 * Create a scenario with some people
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		List<PersonId> personIds = getScrambledPersonIds(1000, 5000, randomGenerator);

		for (PersonId personId : personIds) {
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, regionId, compartmentId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}

		/*
		 * Show that the regions assigned to people in the scenario are those
		 * assigned in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one person in the test
			assertTrue(scenario.getPeopleIds().size() > 0);
			Map<PersonId, PersonId> idMap = getPersonIdMap(scenario.getPeopleIds());
			for (PersonId scenarioPersonId : scenario.getPeopleIds()) {
				PersonId simulationPersonId = idMap.get(scenarioPersonId);
				RegionId expectedPersonRegion = scenario.getPersonRegion(scenarioPersonId);
				RegionId actualPersonRegion = environment.getPersonRegion(simulationPersonId);
				assertEquals(expectedPersonRegion, actualPersonRegion);
			}
		});
	}

	private void testGetPersonRegionArrivalTrackingPolicy() {

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			/*
			 * Create scenario with the compartment arrival tracking time policy
			 * set
			 */
			ScenarioBuilder scenarioBuilder = getScenarioBuilder();
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);

			/*
			 * Show that the region arrival tracking time policy in the scenario
			 * matches the one in the environment
			 */
			executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
				assertEquals(scenario.getPersonRegionArrivalTrackingPolicy(), environment.getPersonRegionArrivalTrackingPolicy());
			});
		}
	}

	private void testGetPersonResourceLevel() {
		RandomGenerator randomGenerator = getRandomGenerator(29);
		/*
		 * Create a scenario with some people having non-zero resource levels
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
		}

		List<PersonId> personIds = getScrambledPersonIds(1000, 5000, randomGenerator);

		for (PersonId personId : personIds) {
			RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			scenarioBuilder.addPerson(personId, regionId, compartmentId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
		}
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new EmptyComponent()::init);
		}

		for (PersonId personId : personIds) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				long amount = randomGenerator.nextInt(1000) + 1;
				scenarioBuilder.setPersonResourceLevel(personId, testResourceId, amount);
			}
		}

		/*
		 * Show that the person resource levels in the scenario are those found
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one person in the
			// test
			assertTrue(scenario.getPeopleIds().size() > 0);

			// validate that there is at least one resource the test
			assertTrue(scenario.getResourceIds().size() > 0);

			Map<PersonId, PersonId> idMap = getPersonIdMap(scenario.getPeopleIds());
			for (PersonId scenarioPersonId : scenario.getPeopleIds()) {
				PersonId simulationPersonId = idMap.get(scenarioPersonId);
				for (ResourceId resourceId : scenario.getResourceIds()) {
					Long expectedPersonResourceLevel = scenario.getPersonResourceLevel(scenarioPersonId, resourceId);
					Long actualPersonResourceLevel = environment.getPersonResourceLevel(simulationPersonId, resourceId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}
		});
	}

	private void testGetPersonResourceTimeTrackingPolicy() {
		RandomGenerator randomGenerator = getRandomGenerator(30);
		/*
		 * Create a scenario with the person resource time tracking policy
		 * values set
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[randomGenerator.nextInt(TimeTrackingPolicy.values().length)];
			scenarioBuilder.setResourceTimeTracking(testResourceId, timeTrackingPolicy);
		}

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
		}

		/*
		 * Show that the person resource time tracking policy values in the
		 * scenario match those in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one resource
			assertTrue(scenario.getResourceIds().size() > 0);
			for (ResourceId resoureId : scenario.getResourceIds()) {
				TimeTrackingPolicy expectedPersonResourceTimeTrackingPolicy = scenario.getPersonResourceTimeTrackingPolicy(resoureId);
				TimeTrackingPolicy actualPersonResourceTimeTrackingPolicy = environment.getPersonResourceTimeTrackingPolicy(resoureId);
				assertEquals(expectedPersonResourceTimeTrackingPolicy, actualPersonResourceTimeTrackingPolicy);
			}
		});

	}

	private void testGetRegionPropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(31);

		/*
		 * Create a scenario with various region property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		}

		/*
		 * Show that each region property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getRegionPropertyIds().size() > 0);
			for (RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
				PropertyDefinition actualPropertyDefinition = environment.getRegionPropertyDefinition(regionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	private void testGetRegionPropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(32);

		/*
		 * Create a scenario with various region property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		}

		/*
		 * Show that region property identifiers in the scenario match those in
		 * the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			assertTrue(scenario.getRegionPropertyIds().size() > 0);
			assertEquals(scenario.getRegionPropertyIds(), environment.getRegionPropertyIds());
		});
	}

	private void testGetRegionPropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(33);

		/*
		 * Create a scenario with various compartment property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
			scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();

		for (RegionId regionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(regionId, () -> new EmptyComponent()::init);
		}
		for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = subScenario.getRegionPropertyDefinition(regionPropertyId);
			scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		}
		for (RegionId regionId : TestRegionId.values()) {
			for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
				PropertyDefinition propertyDefinition = subScenario.getRegionPropertyDefinition(regionPropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
			}
		}

		/*
		 * Show that each region property value in the scenario is reflected in
		 * the environment
		 */
		Scenario build = scenarioBuilder.build();
		executeScenarioTest(build, (scenario, environment) -> {
			// show that there were some regions
			assertTrue(scenario.getRegionIds().size() > 0);
			for (RegionId regionId : scenario.getRegionIds()) {
				assertTrue(scenario.getRegionPropertyIds().size() > 0);
				for (RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
					Object expectedRegionPropertyValue = scenario.getRegionPropertyValue(regionId, regionPropertyId);
					Object actualRegionPropertyValue = environment.getRegionPropertyValue(regionId, regionPropertyId);
					assertEquals(expectedRegionPropertyValue, actualRegionPropertyValue);
				}
			}
		});
	}

	private void testGetRegionResourceLevel() {
		RandomGenerator randomGenerator = getRandomGenerator(34);
		/*
		 * Create a scenario with some regions having non-zero resource levels
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new EmptyComponent()::init);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				long amount = randomGenerator.nextInt(1000) + 1;
				scenarioBuilder.setRegionResourceLevel(testRegionId, testResourceId, amount);
			}
		}

		/*
		 * Show that the region resource levels in the scenario are those found
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// validate that there is at least one region producer in the test
			assertTrue(scenario.getRegionIds().size() > 0);

			// validate that there is at least one resource the test
			assertTrue(scenario.getResourceIds().size() > 0);

			for (RegionId regionId : scenario.getRegionIds()) {
				for (ResourceId resourceId : scenario.getResourceIds()) {
					Long expectedRegionResourceLevel = scenario.getRegionResourceLevel(regionId, resourceId);
					Long actualRegionResourceLevel = environment.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);
				}
			}
		});
	}

	private void testGetResourceIds() {
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (ResourceId resoureId : TestResourceId.values()) {
			scenarioBuilder.addResource(resoureId);
		}
		// Show that the resource ids from the scenario are found in the
		// environment
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Set<ResourceId> expectedResourceIds = scenario.getResourceIds();
			Set<ResourceId> actualResourceIds = environment.getResourceIds();
			assertTrue(expectedResourceIds.size() > 0);
			assertEquals(expectedResourceIds, actualResourceIds);
		});
	}

	private void testGetResourcePropertyDefinition() {
		RandomGenerator randomGenerator = getRandomGenerator(35);

		/*
		 * Create a scenario with various resource property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
			for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineResourceProperty(testResourceId, resourcePropertyId, propertyDefinition);
			}
		}
		/*
		 * Show that each resource property definition in the scenario is
		 * reflected in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			for (ResourceId resourceId : scenario.getResourceIds()) {
				assertTrue(scenario.getResourcePropertyIds(resourceId).size() > 0);
				for (ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
					PropertyDefinition expectedPropertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					PropertyDefinition actualPropertyDefinition = environment.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}
		});
	}

	private void testGetResourcePropertyIds() {
		RandomGenerator randomGenerator = getRandomGenerator(36);

		/*
		 * Create a scenario with various resource property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
			for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineResourceProperty(testResourceId, resourcePropertyId, propertyDefinition);
			}
		}

		/*
		 * Show that resource property identifiers in the scenario match those
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			for (ResourceId resourceId : scenario.getResourceIds()) {
				assertTrue(scenario.getResourcePropertyIds(resourceId).size() > 0);
				assertEquals(scenario.getResourcePropertyIds(resourceId), environment.getResourcePropertyIds(resourceId));
			}
		});
	}

	private void testGetResourcePropertyValue() {
		RandomGenerator randomGenerator = getRandomGenerator(37);

		/*
		 * Create a scenario with various resource property definitions
		 */
		ScenarioBuilder scenarioBuilder = getScenarioBuilder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
			for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
				PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, null);
				scenarioBuilder.defineResourceProperty(testResourceId, resourcePropertyId, propertyDefinition);
			}
		}
		Scenario subScenario = scenarioBuilder.build();

		scenarioBuilder = getScenarioBuilder();

		for (ResourceId resourceId : subScenario.getResourceIds()) {
			scenarioBuilder.addResource(resourceId);
		}
		for (ResourceId resourceId : subScenario.getResourceIds()) {
			for (ResourcePropertyId resourcePropertyId : subScenario.getResourcePropertyIds(resourceId)) {
				PropertyDefinition propertyDefinition = subScenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			}
		}
		for (ResourceId resourceId : subScenario.getResourceIds()) {
			for (ResourcePropertyId resourcePropertyId : subScenario.getResourcePropertyIds(resourceId)) {
				PropertyDefinition propertyDefinition = subScenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				Object propertyValue = generatePropertyValue(propertyDefinition, randomGenerator);
				scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
			}
		}

		/*
		 * Show that each resource property value in the scenario is reflected
		 * in the environment
		 */
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// show that there were some resources
			assertTrue(scenario.getResourceIds().size() > 0);
			for (ResourceId resourceId : scenario.getResourceIds()) {
				assertTrue(scenario.getResourcePropertyIds(resourceId).size() > 0);
				for (ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
					Object expectedResourcePropertyValue = scenario.getResourcePropertyValue(resourceId, resourcePropertyId);
					Object actualResourcePropertyValue = environment.getResourcePropertyValue(resourceId, resourcePropertyId);
					assertEquals(expectedResourcePropertyValue, actualResourcePropertyValue);
				}
			}
		});
	}

	private void testGetStageBatches() {
		RandomGenerator randomGenerator = getRandomGenerator(39);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}

		int numStagesPerProducer = 5;
		int numStages = numStagesPerProducer * TestMaterialsProducerId.size();
		int numBatchesPerStage = 12;
		int numBatches = numBatchesPerStage * numStages;

		List<StageId> stageIds = getScrambledStageIds(numStages, 3 * numStages, randomGenerator);
		List<BatchId> batchIds = getScrambledBatchIds(numBatches, 3 * numBatches, randomGenerator);
		int stageIndex = 0;
		int batchIndex = 0;
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (int i = 0; i < numStagesPerProducer; i++) {
				StageId stageId = stageIds.get(stageIndex++);
				scenarioBuilder.addStage(stageId, false, testMaterialsProducerId);
				for (int j = 0; j < numBatchesPerStage; j++) {
					TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble() * 1000;
					BatchId batchId = batchIds.get(batchIndex++);
					scenarioBuilder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
					scenarioBuilder.addBatchToStage(stageId, batchId);
				}
			}
		}

		// Show that each stage to batch relationships in the scenario are
		// reflected in the environment
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {

			Map<StageId, StageId> stageIdMap = getStageIdMap(scenario.getStageIds());
			Map<BatchId, BatchId> batchIdMap = getBatchIdMap(scenario.getBatchIds());
			// validate that there is at least one stage
			assertTrue(scenario.getStageIds().size() > 0);
			for (StageId scenarioStageId : scenario.getStageIds()) {

				// validate that the stage has at least one batch
				assertTrue(scenario.getStageBatches(scenarioStageId).size() > 0);

				Set<BatchId> expectedBatchIds = new LinkedHashSet<>();
				for (BatchId scenarioBatchId : scenario.getStageBatches(scenarioStageId)) {
					BatchId simulationBatchID = batchIdMap.get(scenarioBatchId);
					expectedBatchIds.add(simulationBatchID);
				}
				StageId simulationStageId = stageIdMap.get(scenarioStageId);
				Set<BatchId> actualBatchIds = new LinkedHashSet<>(environment.getStageBatches(simulationStageId));

				assertEquals(expectedBatchIds, actualBatchIds);

			}

		});
	}

	private void testGetStageIds() {
		RandomGenerator randomGenerator = getRandomGenerator(40);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<StageId> stageIds = getScrambledStageIds(100, 300, randomGenerator);

		for (StageId stageId : stageIds) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addStage(stageId, true, testMaterialsProducerId);
		}

		// Show that the stage ids in the scenario have a one to one
		// correspondence to the environments stages
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			// determine the expected batch ids in the environment
			Map<StageId, StageId> idMap = getStageIdMap(scenario.getStageIds());
			Set<StageId> expectedSimulationStageIds = new LinkedHashSet<>(idMap.values());

			// determine the actual batch ids in the environment
			Set<StageId> actualSimulationStageIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : environment.getMaterialsProducerIds()) {
				actualSimulationStageIds.addAll(environment.getStages(materialsProducerId));
			}

			assertTrue(expectedSimulationStageIds.size() > 0);
			assertEquals(expectedSimulationStageIds, actualSimulationStageIds);
		});
	}

	private void testGetStageMaterialsProducer() {
		RandomGenerator randomGenerator = getRandomGenerator(41);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<StageId> stageIds = getScrambledStageIds(100, 300, randomGenerator);

		for (StageId stageId : stageIds) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addStage(stageId, true, testMaterialsProducerId);
		}

		// Show that the stage materials producer in the scenario matches the
		// environment
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<StageId, StageId> idMap = getStageIdMap(scenario.getStageIds());
			// validate that there is at least one stage to test
			assertTrue(scenario.getStageIds().size() > 0);
			for (StageId scenarioStageId : scenario.getStageIds()) {
				StageId simulationStageId = idMap.get(scenarioStageId);
				MaterialsProducerId expectedStageMaterialsProducer = scenario.getStageMaterialsProducer(scenarioStageId);
				MaterialsProducerId actualStageMaterialsProducer = environment.getStageProducer(simulationStageId);
				assertEquals(expectedStageMaterialsProducer, actualStageMaterialsProducer);
			}

		});
	}

	private void testIsStageOffered() {
		RandomGenerator randomGenerator = getRandomGenerator(42);

		ScenarioBuilder scenarioBuilder = getScenarioBuilder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new EmptyComponent()::init);
		}
		List<StageId> stageIds = getScrambledStageIds(100, 300, randomGenerator);

		for (StageId stageId : stageIds) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			scenarioBuilder.addStage(stageId, randomGenerator.nextBoolean(), testMaterialsProducerId);
		}

		// Show that the stage materials producer in the scenario matches the
		// environment
		executeScenarioTest(scenarioBuilder.build(), (scenario, environment) -> {
			Map<StageId, StageId> idMap = getStageIdMap(scenario.getStageIds());
			// validate that there is at least one stage to test
			assertTrue(scenario.getStageIds().size() > 0);
			for (StageId scenarioStageId : scenario.getStageIds()) {
				StageId simulationStageId = idMap.get(scenarioStageId);
				Boolean expectedStageOffered = scenario.isStageOffered(scenarioStageId);
				Boolean actualStageOffered = environment.isStageOffered(simulationStageId);
				assertEquals(expectedStageOffered, actualStageOffered);
			}

		});
	}

	/**
	 * Tests {@link Simulation#setScenario(Scenario)}
	 */
	@Test
	@UnitTestMethod(name = "setScenario", args = { Scenario.class })
	public void test() {
		testGetBatchAmount();
		testGetBatchIds();
		testGetBatchMaterial();
		testGetBatchMaterialsProducer();
		testGetBatchPropertyDefinition();
		testGetBatchPropertyIds();
		testGetBatchPropertyValue();
		testGetCompartmentPropertyDefinition();
		testGetCompartmentPropertyIds();
		testGetCompartmentPropertyValue();
		testGetGlobalPropertyDefinition();
		testGetGlobalPropertyIds();
		testGetGlobalPropertyValue();
		testGetGroupIds();
		testGetGroupMembers();
		testGetGroupPropertyDefinition();
		testGetGroupPropertyIds();
		testGetGroupPropertyValue();
		testGetGroupTypeId();
		testGetGroupTypeIds();
		testGetMaterialIds();
		testGetComponentIds();
		testGetMaterialsProducerPropertyDefinition();
		testGetMaterialsProducerPropertyIds();
		testGetMaterialsProducerPropertyValue();
		testGetMaterialsProducerResourceLevel();
		testGetPeopleIds();
		testGetPersonCompartment();
		testGetPersonCompartmentArrivalTrackingPolicy();
		testGetPersonPropertyDefinition();
		testGetPersonPropertyIds();
		testGetPersonPropertyValue();
		testGetPersonRegion();
		testGetPersonRegionArrivalTrackingPolicy();
		testGetPersonResourceLevel();
		testGetPersonResourceTimeTrackingPolicy();
		testGetRegionPropertyDefinition();
		testGetRegionPropertyIds();
		testGetRegionPropertyValue();
		testGetRegionResourceLevel();
		testGetResourceIds();
		testGetResourcePropertyDefinition();
		testGetResourcePropertyIds();
		testGetResourcePropertyValue();
		testGetStageBatches();
		testGetStageIds();
		testGetStageMaterialsProducer();
		testIsStageOffered();
	}

}
