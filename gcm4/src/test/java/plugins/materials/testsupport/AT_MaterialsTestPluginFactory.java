package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.materials.MaterialsPluginData;
import plugins.materials.MaterialsPluginId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsTestPluginFactory.Factory;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.regions.RegionsPluginData;
import plugins.regions.RegionsPluginId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.ResourcesPluginId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_MaterialsTestPluginFactory {

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class, int.class, long.class, Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3328026739613106739L, c -> executed.setValue(true));
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class, int.class, long.class, TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7995349318419680542L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, nullTestPluginData));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

	}

	/*
	 * Given a list of plugins, will show that the plugin with the given
	 * pluginId exists, and exists EXACTLY once.
	 */
	private Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
		Plugin actualPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}

		assertNotNull(actualPlugin);

		return actualPlugin;
	}

	/**
	 * Given a list of plugins, will show that the explicit plugindata for the
	 * given pluginid exists, and exists EXACTLY once.
	 */
	private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData, PluginId pluginId) {
		Plugin actualPlugin = checkPluginExists(plugins, pluginId);
		List<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
		assertNotNull(actualPluginDatas);
		assertEquals(1, actualPluginDatas.size());
		PluginData actualPluginData = actualPluginDatas.get(0);
		assertTrue(expectedPluginData == actualPluginData);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).getPlugins();
		assertEquals(6, plugins.size());

		checkPluginExists(plugins, MaterialsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, ResourcesPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
		checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsPluginData", args = { MaterialsPluginData.class })
	public void testSetMaterialsPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4328791012645031581L);
		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, randomPropertyValue);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
			}
		}

		MaterialsPluginData materialsPluginData = materialsBuilder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setMaterialsPluginData(materialsPluginData).getPlugins();

		checkPluginDataExists(plugins, materialsPluginData, MaterialsPluginId.PLUGIN_ID);

		// precondition: materialsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setMaterialsPluginData(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PLUGIN_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = { ResourcesPluginData.class })
	public void testSetResourcesPluginData() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		ResourcesPluginData resourcesPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setResourcesPluginData(resourcesPluginData).getPlugins();

		checkPluginDataExists(plugins, resourcesPluginData, ResourcesPluginId.PLUGIN_ID);

		// precondition: resourcesPluginData is not null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setResourcesPluginData(null));
		assertEquals(ResourceError.NULL_RESOURCE_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = { RegionsPluginData.class })
	public void testSetRegionsPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(968101337385656117L);
		int initialPopulation = 30;
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the region plugin
		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, randomPropertyValue);
				}
			}
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

		// precondition: regionsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setRegionsPluginData(null));
		assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = { PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		builder.addPersonRange(new PersonRange(0,99));
		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setPeoplePluginData(peoplePluginData).getPlugins();

		checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

		// precondition: peoplePluginData is not null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setPeoplePluginData(null));
		assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = { StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		WellState wellState = WellState.builder().setSeed(2990359774692004249L).build();
		builder.setMainRNGState(wellState);
		
		wellState = WellState.builder().setSeed(1090094972994322820L).build();
		builder.addRNG(TestRandomGeneratorId.BLITZEN, wellState);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

		// precondition: stochasticsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setStochasticsPluginData(null));
		assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardMaterialsPluginData", args = { int.class, int.class, int.class, long.class })
	public void testGetStandardMaterialsPluginData() {
		int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;
		long seed = 9029198675932589278L;

		MaterialsPluginData materialsPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches, numStages, numBatchesInStage, seed);

		Set<TestMaterialId> expectedMaterialIds = EnumSet.allOf(TestMaterialId.class);
		assertFalse(expectedMaterialIds.isEmpty());

		Set<MaterialId> actualMaterialIds = materialsPluginData.getMaterialIds();
		assertEquals(expectedMaterialIds, actualMaterialIds);

		for (TestMaterialId expectedMaterialId : expectedMaterialIds) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(expectedMaterialId);
			assertFalse(expectedBatchPropertyIds.isEmpty());
			Set<BatchPropertyId> actualBatchPropertyIds = materialsPluginData.getBatchPropertyIds(expectedMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			for (TestBatchPropertyId batchPropertyId : expectedBatchPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = batchPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsPluginData.getBatchPropertyDefinition(expectedMaterialId, batchPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		Set<TestMaterialsProducerId> expectedMaterialsProducerIds = EnumSet.allOf(TestMaterialsProducerId.class);
		assertFalse(expectedMaterialsProducerIds.isEmpty());
		Set<MaterialsProducerId> actualProducerIds = materialsPluginData.getMaterialsProducerIds();
		assertEquals(expectedMaterialsProducerIds, actualProducerIds);

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		assertEquals(numBatches * expectedMaterialsProducerIds.size(), materialsPluginData.getBatchIds().size());
		assertEquals(numStages * expectedMaterialsProducerIds.size(), materialsPluginData.getStageIds().size());

		int expectedNumBatchesPerStage = numBatchesInStage * expectedMaterialsProducerIds.size();
		int actualNumBatchesPerStage = 0;

		int bId = 0;
		int sId = 0;
		for (TestMaterialsProducerId expectedProducerId : expectedMaterialsProducerIds) {

			List<BatchId> batches = new ArrayList<>();
			for (int i = 0; i < numBatches; i++) {
				batches.add(new BatchId(bId++));
			}
			assertTrue(materialsPluginData.getBatchIds().containsAll(batches));

			for (BatchId batchId : batches) {

				TestMaterialId expectedMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double expectedAmount = randomGenerator.nextDouble();

				MaterialId actualMaterialId = materialsPluginData.getBatchMaterial(batchId);
				assertEquals(expectedMaterialId, actualMaterialId);

				double actualAmount = materialsPluginData.getBatchAmount(batchId);
				assertEquals(expectedAmount, actualAmount);
				for (TestBatchPropertyId expectedBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(expectedMaterialId)) {
					if (expectedBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
						Object expectedPropertyValue = expectedBatchPropertyId.getRandomPropertyValue(randomGenerator);

						Map<BatchPropertyId, Object> propertyValueMap = materialsPluginData.getBatchPropertyValues(batchId);
						assertTrue(propertyValueMap.containsKey(expectedBatchPropertyId));
						assertEquals(expectedPropertyValue, propertyValueMap.get(expectedBatchPropertyId));
					}
				}
			}

			List<StageId> stages = new ArrayList<>();
			for (int i = 0; i < numStages; i++) {
				stages.add(new StageId(sId++));
			}
			assertTrue(materialsPluginData.getStageIds().containsAll(stages));

			for (int i = 0; i < stages.size(); i++) {
				MaterialsProducerId actualMaterialsProducerId = materialsPluginData.getStageMaterialsProducer(stages.get(i));
				assertEquals(expectedProducerId, actualMaterialsProducerId);
				boolean expectedOffered = i % 2 == 0;
				boolean actualOffered = materialsPluginData.isStageOffered(stages.get(i));
				assertTrue(expectedOffered == actualOffered);
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < numBatchesInStage; i++) {
				StageId expectedStageId = stages.get(randomGenerator.nextInt(stages.size()));
				BatchId expectedBatchId = batches.get(i);

				Set<BatchId> actualBatchIds = materialsPluginData.getStageBatches(expectedStageId);
				assertTrue(actualBatchIds.contains(expectedBatchId));
				actualNumBatchesPerStage++;
			}

		}
		assertEquals(expectedNumBatchesPerStage, actualNumBatchesPerStage);

		Set<TestMaterialsProducerPropertyId> expectedMaterialsProducerPropertyIds = EnumSet.allOf(TestMaterialsProducerPropertyId.class);
		assertFalse(expectedMaterialsProducerPropertyIds.isEmpty());

		Set<MaterialsProducerPropertyId> actualMaterialProducerPropertyIds = materialsPluginData.getMaterialsProducerPropertyIds();
		assertEquals(expectedMaterialsProducerPropertyIds, actualMaterialProducerPropertyIds);

		for (TestMaterialsProducerPropertyId expectedMaterialsProducerPropertyId : expectedMaterialsProducerPropertyIds) {
			PropertyDefinition expectedPropertyDefinition = expectedMaterialsProducerPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = materialsPluginData.getMaterialsProducerPropertyDefinition(expectedMaterialsProducerPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			if (expectedPropertyDefinition.getDefaultValue().isEmpty()) {
				for (TestMaterialsProducerId producerId : expectedMaterialsProducerIds) {
					Object expectedPropertyValue = expectedMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					Map<MaterialsProducerPropertyId, Object> propertyValueMap = materialsPluginData.getMaterialsProducerPropertyValues(producerId);
					assertTrue(propertyValueMap.containsKey(expectedMaterialsProducerPropertyId));
					Object actualPropertyValue = propertyValueMap.get(expectedMaterialsProducerPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
				}
			}
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardResourcesPluginData", args = { long.class })
	public void testGetStandardResourcesPluginData() {

		long seed = 4800551796983227153L;
		ResourcesPluginData resourcesPluginData = MaterialsTestPluginFactory.getStandardResourcesPluginData(seed);

		Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
		assertFalse(expectedResourceIds.isEmpty());

		Set<ResourceId> actualResourceIds = resourcesPluginData.getResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);

		for (TestResourceId resourceId : TestResourceId.values()) {
			TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
			TimeTrackingPolicy actualPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
			assertEquals(expectedPolicy, actualPolicy);
		}

		Set<TestResourcePropertyId> expectedResourcePropertyIds = EnumSet.allOf(TestResourcePropertyId.class);
		assertFalse(expectedResourcePropertyIds.isEmpty());

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
			PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
			Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

			assertTrue(resourcesPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId));

			PropertyDefinition actualPropertyDefinition = resourcesPluginData.getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

			Object actualPropertyValue = resourcesPluginData.getResourcePropertyValue(expectedResourceId, resourcePropertyId);
			assertEquals(expectedPropertyValue, actualPropertyValue);
		}
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {})
	public void testGetStandardRegionsPluginData() {

		RegionsPluginData regionsPluginData = MaterialsTestPluginFactory.getStandardRegionsPluginData();

		Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);
		assertFalse(expectedRegionIds.isEmpty());

		Set<RegionId> actualRegionIds = regionsPluginData.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = MaterialsTestPluginFactory.getStandardPeoplePluginData();
		assertEquals(0, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = { long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = MaterialsTestPluginFactory.getStandardStochasticsPluginData(seed);
		assertEquals(seed, stochasticsPluginData.getWellState().getSeed());
	}
}
