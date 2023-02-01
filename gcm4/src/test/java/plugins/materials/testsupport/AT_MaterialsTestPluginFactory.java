package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.materials.MaterialsPluginData;
import plugins.materials.support.StageId;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.ResourcesPluginData;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_MaterialsTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			// TODO: add checks

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
			int.class, long.class, Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(MaterialsTestPluginFactory
				.factory(0, 0, 0, 3328026739613106739L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
			int.class, long.class, TestPluginData.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				MaterialsTestPluginFactory.factory(0, 0, 0, 7995349318419680542L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(6, MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData) {
		Class<?> classRef = expectedPluginData.getClass();
		plugins.forEach((plugin) -> {
			Set<PluginData> pluginDatas = plugin.getPluginDatas();
			if (pluginDatas.size() > 0) {
				PluginData pluginData = pluginDatas.toArray(new PluginData[0])[0];
				if (classRef.isAssignableFrom(pluginData.getClass())) {
					assertEquals(expectedPluginData, classRef.cast(pluginData));
				} else {
					assertNotEquals(expectedPluginData, pluginData);
				}
			}
		});
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsPluginData", args = {
			MaterialsPluginData.class })
	public void testSetMaterialsPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4328791012645031581L);
		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.getPropertiesWithoutDefaultValues()) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId,
						testMaterialsProducerPropertyId, randomPropertyValue);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId,
						testBatchPropertyId.getPropertyDefinition());
			}
		}

		MaterialsPluginData materialsPluginData = materialsBuilder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setMaterialsPluginData(materialsPluginData).getPlugins();

		checkPlugins(plugins, materialsPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = {
			ResourcesPluginData.class })
	public void testSetResourcesPluginData() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		// TODO: add stuff to builder

		ResourcesPluginData resourcesPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setResourcesPluginData(resourcesPluginData).getPlugins();

		checkPlugins(plugins, resourcesPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
			RegionsPluginData.class })
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
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId,
					testRegionPropertyId.getPropertyDefinition());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
						|| randomGenerator.nextBoolean()) {
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

		checkPlugins(plugins, regionsPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setPeoplePluginData(peoplePluginData).getPlugins();

		checkPlugins(plugins, peoplePluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2990359774692004249L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPlugins(plugins, stochasticsPluginData);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardMaterialsPluginData", args = {
			int.class, int.class, int.class, long.class })
	public void testGetStandardMaterialsPluginData() {
		int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;

		MaterialsPluginData materialsPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches,
				numStages, numBatchesInStage,
				9029198675932589278L);
		assertNotNull(materialsPluginData);

		// TODO: add additional checks

		assertEquals(numBatches * TestMaterialsProducerId.values().length, materialsPluginData.getBatchIds().size());
		assertEquals(numStages * TestMaterialsProducerId.values().length, materialsPluginData.getStageIds().size());
		int count = 0;
		for(StageId stageId : materialsPluginData.getStageIds()) {
			count += materialsPluginData.getStageBatches(stageId).size();
		}
		assertEquals(numBatchesInStage * TestMaterialsProducerId.values().length , count);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardResourcesPluginData", args = {
			long.class })
	public void testGetStandardResourcesPluginData() {

		ResourcesPluginData resourcesPluginData = MaterialsTestPluginFactory
				.getStandardResourcesPluginData(4800551796983227153L);
		assertNotNull(resourcesPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {})
	public void testGetStandardRegionsPluginData() {

		RegionsPluginData regionsPluginData = MaterialsTestPluginFactory.getStandardRegionsPluginData();
		assertNotNull(regionsPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = MaterialsTestPluginFactory.getStandardPeoplePluginData();
		assertNotNull(peoplePluginData);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = MaterialsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}
