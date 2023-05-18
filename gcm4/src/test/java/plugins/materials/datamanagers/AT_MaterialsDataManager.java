package plugins.materials.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.materials.MaterialsPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.BatchAdditionEvent;
import plugins.materials.events.BatchAmountUpdateEvent;
import plugins.materials.events.BatchImminentRemovalEvent;
import plugins.materials.events.BatchPropertyDefinitionEvent;
import plugins.materials.events.BatchPropertyUpdateEvent;
import plugins.materials.events.MaterialIdAdditionEvent;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerPropertyDefinitionEvent;
import plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.events.StageAdditionEvent;
import plugins.materials.events.StageImminentRemovalEvent;
import plugins.materials.events.StageMaterialsProducerUpdateEvent;
import plugins.materials.events.StageMembershipAdditionEvent;
import plugins.materials.events.StageMembershipRemovalEvent;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyDefinitionInitialization;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerConstructionData;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyDefinitionInitialization;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageConversionInfo;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsTestPluginFactory;
import plugins.materials.testsupport.MaterialsTestPluginFactory.Factory;
import plugins.materials.testsupport.TestBatchConstructionInfo;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.RegionResourceUpdateEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableLong;

public class AT_MaterialsDataManager {

	@Test
	@UnitTestConstructor(target = MaterialsDataManager.class, args = { MaterialsPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialsDataManager(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "init", args = { BatchConstructionInfo.class })
	public void testInit_State() {
		// fill in a materials plugin data with some nominal content
		//////////////////////////////////////////////////////

		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		materialsBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		TestMaterialsProducerPropertyId propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		materialsBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		materialsBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		materialsBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, propId, 345);
		materialsBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_2, propId, 5234);
		materialsBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, 123L);
		materialsBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_2, 55L);
		materialsBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		materialsBuilder.addMaterial(TestMaterialId.MATERIAL_2);
		TestBatchPropertyId bprop = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, bprop, bprop.getPropertyDefinition());
		materialsBuilder.addBatch(new BatchId(0), TestMaterialId.MATERIAL_1, 10, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addBatch(new BatchId(1), TestMaterialId.MATERIAL_1, 11, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addBatch(new BatchId(2), TestMaterialId.MATERIAL_2, 12, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatch(new BatchId(3), TestMaterialId.MATERIAL_2, 13, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatch(new BatchId(4), TestMaterialId.MATERIAL_2, 14, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addStage(new StageId(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatchToStage(new StageId(0), new BatchId(3));
		materialsBuilder.addBatchToStage(new StageId(0), new BatchId(4));
		materialsBuilder.setBatchPropertyValue(new BatchId(0), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 77);
		materialsBuilder.setBatchPropertyValue(new BatchId(1), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 56);
		materialsBuilder.setBatchPropertyValue(new BatchId(4), TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK, 534);
		MaterialsPluginData materialsPluginData = materialsBuilder.build();

		//////////////////////////////////////////////////////

		MaterialsProducerConstructionData materialsProducerConstructionData = MaterialsProducerConstructionData.builder().setMaterialsProducerId(
				TestMaterialsProducerId.MATERIALS_PRODUCER_3).setMaterialsProducerPropertyValue(TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
						true).setMaterialsProducerPropertyValue(TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 21).build();
		List<StageId> expectedStageIds = new ArrayList<>();

		// run the sim with some actors that update a little of everything
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, false);
			materialsDataManager.moveBatchToStage(new BatchId(0), stageId);
			materialsDataManager.removeStage(stageId, false);
			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
			expectedStageIds.add(stageId);
		}));
		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 5818867905165255006L, testPluginData);
		factory.setMaterialsPluginData(materialsPluginData);
		TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins()).setProduceSimulationStateOnHalt(true).setSimulationHaltTime(2).build().execute();
		Map<MaterialsPluginData, Integer> outputItems = testOutputConsumer.getOutputItems(MaterialsPluginData.class);
		assertEquals(1, outputItems.size());
		// build the expected materials plugin data
		MaterialsPluginData materialsPluginData2 = outputItems.keySet().iterator().next();
		MaterialsPluginData.Builder expectedBuilder = MaterialsPluginData.builder();
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		expectedBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		expectedBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, propId, 345);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_2, propId, 5234);
		expectedBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, 123L);
		expectedBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_2, 55L);
		expectedBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		expectedBuilder.addMaterial(TestMaterialId.MATERIAL_2);
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, bprop, bprop.getPropertyDefinition());
		expectedBuilder.addBatch(new BatchId(0), TestMaterialId.MATERIAL_1, 10, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		expectedBuilder.addBatch(new BatchId(1), TestMaterialId.MATERIAL_1, 11, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		expectedBuilder.addBatch(new BatchId(2), TestMaterialId.MATERIAL_2, 12, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addBatch(new BatchId(3), TestMaterialId.MATERIAL_2, 13, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addBatch(new BatchId(4), TestMaterialId.MATERIAL_2, 14, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addStage(new StageId(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addStage(expectedStageIds.get(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		expectedBuilder.addBatchToStage(new StageId(0), new BatchId(3));
		expectedBuilder.addBatchToStage(new StageId(0), new BatchId(4));
		expectedBuilder.setBatchPropertyValue(new BatchId(0), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 77);
		expectedBuilder.setBatchPropertyValue(new BatchId(1), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 56);
		expectedBuilder.setBatchPropertyValue(new BatchId(4), TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK, 534);
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 21);
		MaterialsPluginData expectedPluginData = expectedBuilder.build();

		// compare via equals
		assertEquals(expectedPluginData, materialsPluginData2);

		// Show that plugin data persists after actions at different times
		materialsBuilder = MaterialsPluginData.builder();

		materialsBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		materialsBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		materialsBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		materialsBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, propId, 345);
		materialsBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_2, propId, 5234);
		materialsBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, 123L);
		materialsBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_2, 55L);
		materialsBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		materialsBuilder.addMaterial(TestMaterialId.MATERIAL_2);
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, bprop, bprop.getPropertyDefinition());
		materialsBuilder.addBatch(new BatchId(0), TestMaterialId.MATERIAL_1, 10, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addBatch(new BatchId(1), TestMaterialId.MATERIAL_1, 11, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		materialsBuilder.addBatch(new BatchId(2), TestMaterialId.MATERIAL_2, 12, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatch(new BatchId(3), TestMaterialId.MATERIAL_2, 13, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatch(new BatchId(4), TestMaterialId.MATERIAL_2, 14, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addStage(new StageId(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		materialsBuilder.addBatchToStage(new StageId(0), new BatchId(3));
		materialsBuilder.addBatchToStage(new StageId(0), new BatchId(4));
		materialsBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK,
				TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK.getPropertyDefinition());
		materialsBuilder.setBatchPropertyValue(new BatchId(0), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 77);
		materialsBuilder.setBatchPropertyValue(new BatchId(1), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 56);
		materialsBuilder.setBatchPropertyValue(new BatchId(4), TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK, 534);
		materialsPluginData = materialsBuilder.build();

		// run the sim with some actors that update a little of everything
		pluginBuilder = TestPluginData.builder();

		// run the sim with some actors that update a little of everything
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, false);
			materialsDataManager.moveBatchToStage(new BatchId(0), stageId);
			materialsDataManager.removeStage(stageId, false);
			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
			expectedStageIds.add(stageId);
		}));

		BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder().setMaterialId(TestMaterialId.MATERIAL_2).setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2)
																			.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true).setAmount(58.9).build();

		List<BatchId> expectedBatchIds = new ArrayList<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			expectedBatchIds.add(batchId);
			materialsDataManager.removeBatch(new BatchId(0));
			materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK,
					73);
			materialsDataManager.setBatchPropertyValue(new BatchId(3), TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true);
		}));

		testPluginData = pluginBuilder.build();

		factory = MaterialsTestPluginFactory.factory(0, 0, 0, 5818867905165255006L, testPluginData);
		factory.setMaterialsPluginData(materialsPluginData);
		testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins()).setProduceSimulationStateOnHalt(true).setSimulationHaltTime(2).build().execute();
		outputItems = testOutputConsumer.getOutputItems(MaterialsPluginData.class);
		assertEquals(1, outputItems.size());
		// build the expected materials plugin data
		materialsPluginData2 = outputItems.keySet().iterator().next();
		expectedBuilder = MaterialsPluginData.builder();
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		expectedBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		propId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		expectedBuilder.defineMaterialsProducerProperty(propId, propId.getPropertyDefinition());
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, propId, 73);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_2, propId, 5234);
		expectedBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, 123L);
		expectedBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_2, 55L);
		expectedBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		expectedBuilder.addMaterial(TestMaterialId.MATERIAL_2);
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, bprop, bprop.getPropertyDefinition());
		bprop = TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK;
		expectedBuilder.defineBatchProperty(TestMaterialId.MATERIAL_2, bprop, bprop.getPropertyDefinition());
		expectedBuilder.addBatch(new BatchId(1), TestMaterialId.MATERIAL_1, 11, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		expectedBuilder.addBatch(new BatchId(2), TestMaterialId.MATERIAL_2, 12, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addBatch(new BatchId(3), TestMaterialId.MATERIAL_2, 13, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addBatch(new BatchId(4), TestMaterialId.MATERIAL_2, 14, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addBatch(expectedBatchIds.get(0), TestMaterialId.MATERIAL_2, 58.9, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addStage(new StageId(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		expectedBuilder.addStage(expectedStageIds.get(0), false, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		expectedBuilder.addBatchToStage(new StageId(0), new BatchId(3));
		expectedBuilder.addBatchToStage(new StageId(0), new BatchId(4));
		;
		expectedBuilder.setBatchPropertyValue(new BatchId(1), TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 56);
		expectedBuilder.setBatchPropertyValue(new BatchId(4), TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK, 534);
		expectedBuilder.setBatchPropertyValue(new BatchId(3), TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true);
		expectedBuilder.setBatchPropertyValue(new BatchId(5), TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true);
		expectedBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		expectedBuilder.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 21);
		expectedPluginData = expectedBuilder.build();

		// compare via equals
		assertEquals(expectedPluginData, materialsPluginData2);
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "addBatch", args = { BatchConstructionInfo.class })
	public void testAddBatch() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<BatchId> expectedBatchObservations = new LinkedHashSet<>();
		Set<BatchId> actualBatchObservations = new LinkedHashSet<>();

		/* create an observer actor that will observe the batch creations */

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(BatchAdditionEvent.class).build(), (c2, e) -> {
				actualBatchObservations.add(e.batchId());
			});
		}));

		// create some batches and show that their various features are correct
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 20; i++) {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setMaterialId(testMaterialId);
				double amount = randomGenerator.nextDouble();
				builder.setAmount(amount);//
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					builder.setPropertyValue(testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
				}
				BatchConstructionInfo batchConstructionInfo = builder.build();//

				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
				assertEquals(amount, materialsDataManager.getBatchAmount(batchId));

				expectedBatchObservations.add(batchId);
			}

		}));

		/*
		 * have the observer show that the observations are properly generated
		 */

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertTrue(expectedBatchObservations.size() > 0);
			assertEquals(expectedBatchObservations, actualBatchObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 5818867905165255006L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5818867905165255006L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7126499343584962390L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.getUnknownMaterialsProducerId());
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info in the event is
		 * null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6921778272119512748L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.addBatch(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO, contractException.getErrorType());

		/*
		 * precondition test: if the material id in the batch construction info
		 * is null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3677913497762052761L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if the material id in the batch construction info
		 * is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6823349146270705865L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.getUnknownMaterialId());
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if the amount in the batch construction info is
		 * not finite
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1740687746013988916L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(Double.POSITIVE_INFINITY);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the amount in the batch construction info is
		 * negative
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3552750401177629416L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(-1.0);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains a null
		 * batch property id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2067301487300157385L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(null, 15);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains an unknown
		 * batch property id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3866738227501386466L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.getUnknownBatchPropertyId(), 15);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains a batch
		 * property value that is incompatible with the corresponding property
		 * def
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1224987903432629856L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 2.3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains a batch
		 * property value that is incompatible with the corresponding property
		 * def
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8126846490003696164L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 2.3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "addStage", args = { MaterialsProducerId.class })
	public void testAddStage() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a actor observe stage creations

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(StageAdditionEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});
		}));

		// produce stages at various times
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					// show that the stage exists and belongs to the producer
					assertTrue(materialsDataManager.stageExists(stageId));
					assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));

					// generated expected observations
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertEquals(10 * TestMaterialsProducerId.values().length, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1344617610771747654L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test if the materials producer is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2938510662832987631L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.addStage(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test if the materials producer is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3333157817809403586L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.addStage(TestMaterialsProducerId.getUnknownMaterialsProducerId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "batchExists", args = { BatchId.class })
	public void testBatchExists() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Create a data structure to hold batch ids that have been removed and
		 * require flow of control to leave the actor before removal can be
		 * confirmed
		 */
		Map<TestMaterialsProducerId, Set<BatchId>> removalConfirmationBatches = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			removalConfirmationBatches.put(testMaterialsProducerId, new LinkedHashSet<>());

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					double value = randomGenerator.nextDouble() * 100;
					// add the batch an show it exists
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, value, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					assertTrue(materialsDataManager.batchExists(batchId));
					// remove the batch and show that it still exists
					materialsDataManager.removeBatch(batchId);
					assertTrue(materialsDataManager.batchExists(batchId));
					confimationBatches.add(batchId);
				}
				// show that null and unknown batch ids return false
				assertFalse(materialsDataManager.batchExists(null));
				assertFalse(materialsDataManager.batchExists(new BatchId(10000000)));

			}));

			// show that the batches removed above are now gone
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (BatchId batchId : confimationBatches) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3680467733415023569L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "batchPropertyIdExists", args = { MaterialId.class, BatchPropertyId.class })
	public void testBatchPropertyIdExists() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4250860228077588132L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
				MaterialId associatedMaterialId = testBatchPropertyId.getTestMaterialId();
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					if (testMaterialId.equals(associatedMaterialId)) {
						assertTrue(materialsDataManager.batchPropertyIdExists(testMaterialId, testBatchPropertyId));
					} else {
						assertFalse(materialsDataManager.batchPropertyIdExists(testMaterialId, testBatchPropertyId));
					}
				}
			}

			assertFalse(materialsDataManager.batchPropertyIdExists(TestMaterialId.MATERIAL_1, null));
			assertFalse(materialsDataManager.batchPropertyIdExists(null, TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK));
			assertFalse(materialsDataManager.batchPropertyIdExists(null, null));

			// precondition tests : none

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "convertStageToBatch", args = { StageConversionInfo.class })
	public void testConvertStageToBatch() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(EventFilter.builder(BatchImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId(), "removal"));
			});

			c.subscribe(EventFilter.builder(BatchAdditionEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId(), "creation"));
			});

			c.subscribe(EventFilter.builder(StageImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});

		}));

		// have the producers generate batches via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					TestMaterialId materialId;
					double amount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					amount = randomGenerator.nextDouble() + 0.01;
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);

					StageConversionInfo.Builder stageConversionInfoBuilder = //
							StageConversionInfo	.builder().setStageId(stageId)//
												.setMaterialId(materialId)//
												.setAmount(amount);//

					for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(materialId)) {
						if (testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
							Object propertyValue = testBatchPropertyId.getRandomBatchPropertyValue(randomGenerator);
							stageConversionInfoBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
						}
					}

					StageConversionInfo stageConversionInfo = stageConversionInfoBuilder.build();
					BatchId producedBatchId = materialsDataManager.convertStageToBatch(stageConversionInfo);

					// record the stages and batches that should be removed, but
					// only after the current actor activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					assertTrue(materialsDataManager.batchExists(producedBatchId));
					assertEquals(materialId, materialsDataManager.getBatchMaterial(producedBatchId));
					assertEquals(amount, materialsDataManager.getBatchAmount(producedBatchId));

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "creation"));
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "removal"));
					}
					expectedObservations.add(new MultiKey(c.getTime(), producedBatchId, "creation"));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 855059044560726814L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the material id is unknown */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5132874324434783837L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				double amount = 12.5;
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(stageId)//
																				.setMaterialId(TestMaterialId.getUnknownMaterialId())//
																				.setAmount(amount)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/* precondition test: if stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6716241372071908817L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_2;
				double amount = 12.5;
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(new StageId(10000000))//
																				.setMaterialId(materialId)//
																				.setAmount(amount)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 696988531477059866L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialId materialId = TestMaterialId.MATERIAL_2;
				double amount = 12.5;
				materialsDataManager.setStageOfferState(stageId, true);
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(stageId)//
																				.setMaterialId(materialId)//
																				.setAmount(amount)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

		/* precondition test: if the stage conversion info is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5132874324434783837L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.convertStageToBatch(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_CONVERSION_INFO, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains an unknown
		 * batch property id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 696988531477059866L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialId materialId = TestMaterialId.MATERIAL_2;
				double amount = 12.5;
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(stageId)//
																				.setMaterialId(materialId)//
																				.setAmount(amount)//
																				.setPropertyValue(TestBatchPropertyId.getUnknownBatchPropertyId(), 3)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction info contains a batch
		 * property value that is incompatible with the corresponding property
		 * def
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 696988531477059866L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialId materialId = TestMaterialId.MATERIAL_2;
				double amount = 12.5;
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(stageId)//
																				.setMaterialId(materialId)//
																				.setAmount(amount)//
																				.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, 3)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the batch construction does not contain a batch
		 * property value assignment for a batch property that does not have a
		 * default value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 696988531477059866L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				double amount = 12.5;
				StageConversionInfo stageConversionInfo = StageConversionInfo	.builder()//
																				.setStageId(stageId)//
																				.setMaterialId(materialId)//
																				.setAmount(amount)//
																				.build();
				materialsDataManager.convertStageToBatch(stageConversionInfo);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testMaterialId, value, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				// show that the batch matches the inputs
				assertEquals(value, materialsDataManager.getBatchAmount(batchId));
			}

			// precondition tests : none

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchAmount(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchAmount(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1333558356470864456L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2922188778885130752L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, testMaterialId, value, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				// show that the batch matches the inputs
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8694113802920961598L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchMaterial(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6524569565798029395L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchMaterial(new BatchId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchProducer", args = { BatchId.class })
	public void testGetBatchProducer() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int i = 0; i < 10; i++) {

					TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double value = randomGenerator.nextDouble() * 100;
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, value, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

					// show that the batch matches the inputs
					assertEquals(testMaterialsProducerId, materialsDataManager.getBatchProducer(batchId));
				}

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8873616248377004295L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test : if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1422948417739515067L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchProducer(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test : if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6083037726892077495L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchProducer(new BatchId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchPropertyDefinition", args = { MaterialId.class, BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4785939121817102392L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {

				Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
					PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the material id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5856664286545303775L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
				materialsDataManager.getBatchPropertyDefinition(null, testBatchPropertyId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		/* precondition tests if the material id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3682262623372578238L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
				materialsDataManager.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/* precondition tests if the batch property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2977320444281387466L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition tests if the batch property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 712791219730643932L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, TestBatchPropertyId.getUnknownBatchPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8657082858154514151L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				Set<BatchPropertyId> actualBatchPropertyIds = materialsDataManager.getBatchPropertyIds(testMaterialId);
				assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			}

			// precondition tests

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the material id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6822125249787156609L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchPropertyIds(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		/* precondition test: if the material id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7025275053813907413L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchPropertyTime", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyTime() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold the assignments we expect to
		// retrieve.
		Map<MultiKey, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();

		// create an actor

		/*
		 * Have the actor add 50 randomized batches and record the assignment
		 * times for all properties
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, materialId, amount, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					expectedAssignmentTimes.put(new MultiKey(batchId, batchPropertyId), new MutableDouble(c.getTime()));
				}
			}
		}));

		/*
		 * Have the actor alter about 1/3 of batch property values at 10
		 * distinct times, recording the new assignment times as we go.
		 */
		for (int i = 1; i < 10; i++) {
			double actionTime = i;
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// plan several times to alter some of the batch properties

				// alter the batch properties

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (batchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
							if (randomGenerator.nextDouble() < 0.5) {
								Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
								materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, value);
								MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
								mutableDouble.setValue(c.getTime());
							}
						}
					}
				}

			}));
		}

		/*
		 * Have the actor compare the assignment times at time = 10 to the
		 * expected values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (randomGenerator.nextDouble() < 0.33) {
							MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
							double expectedAssignmentTime = mutableDouble.getValue();
							double actualAssignmentTime = materialsDataManager.getBatchPropertyTime(batchId, batchPropertyId);
							assertEquals(expectedAssignmentTime, actualAssignmentTime);
						}
					}
				}
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1470041164645430466L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 411385203720638722L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
				materialsDataManager.getBatchPropertyTime(null, batchPropertyId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6352485251167807955L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
				materialsDataManager.getBatchPropertyTime(new BatchId(100000), batchPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3856953954489485161L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialId.MATERIAL_2, 15L,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.getBatchPropertyTime(batchId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2978468228127714889L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 65L,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.getBatchPropertyTime(batchId, TestBatchPropertyId.getUnknownBatchPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		/*
		 * create a data structure to hold the assignments we expect to
		 * retrieve.
		 */

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			/*
			 * Have the actor add 50 randomized batches and record the values
			 * for all properties
			 */

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {

				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, materialId, amount, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					Object value = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
					expectedValues.put(new MultiKey(batchId, batchPropertyId), value);
				}
			}

			// alter randomly chosen batch properties

			for (int i = 0; i < 200; i++) {

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				BatchId batchId = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
				TestMaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
				TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId, randomGenerator);
				Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, value);
				expectedValues.put(new MultiKey(batchId, batchPropertyId), value);

			}

		}));

		// have an actor show that the batches have the expected property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						Object expectedValue = expectedValues.get(new MultiKey(batchId, batchPropertyId));
						Object actualValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
						assertEquals(expectedValue, actualValue);
					}
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1629075115765446254L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7782292483170344303L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
				materialsDataManager.getBatchPropertyValue(null, batchPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2235610256211958684L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
				materialsDataManager.getBatchPropertyValue(new BatchId(100000), batchPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4276253162944402582L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 45L,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.getBatchPropertyValue(batchId, null);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 211483511977100214L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 45L,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.getBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getBatchStageId", args = { BatchId.class })
	public void testGetBatchStageId() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 472707250737446845L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			// create a stage and a batch
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);

			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialId.MATERIAL_2, 4.5,
					randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

			// show that the batch does not have a stage
			Optional<StageId> optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

			// put the batch onto the stage
			materialsDataManager.moveBatchToStage(batchId, stageId);

			// show that the batch is on the stage
			optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertTrue(optionalOwningStageId.isPresent());
			assertEquals(stageId, optionalOwningStageId.get());

			// put the batch back into inventory
			materialsDataManager.moveBatchToInventory(batchId);

			// show that the batch is not on any stage
			optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8182230906627557939L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchStageId(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3682958492574276233L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getBatchStageId(new BatchId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getInventoryBatches", args = { MaterialsProducerId.class })
	public void testGetInventoryBatches() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold expectd inventories
		Map<TestMaterialsProducerId, Set<BatchId>> expectedInventoryBatchesByProducer = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			expectedInventoryBatchesByProducer.put(testMaterialsProducerId, new LinkedHashSet<>());
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// create some batches
				for (int i = 0; i < 100; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
							randomGenerator.nextInt(100), randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					expectedInventoryBatches.add(batchId);
				}

				// show that the inventory batches are correct
				List<BatchId> inventory = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventory);
				assertEquals(inventory.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);

				// create some stages and put some of the batches onto stages
				List<BatchId> batches = new ArrayList<>(expectedInventoryBatches);
				Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
				int batchIndex = 0;
				for (int i = 0; i < 5; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						materialsDataManager.moveBatchToStage(batchId, stageId);
						expectedInventoryBatches.remove(batchId);
					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					materialsDataManager.removeBatch(batchId);
					expectedInventoryBatches.remove(batchId);
				}

			}));

		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// show that the inventory batches are correct
				List<BatchId> batchList = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
				assertEquals(batchList.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6343917844917632364L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6759896268818524420L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatches(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition tests if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 944257921550728616L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatches(TestMaterialsProducerId.getUnknownMaterialsProducerId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getInventoryBatchesByMaterialId", args = { MaterialsProducerId.class, MaterialId.class })
	public void testGetInventoryBatchesByMaterialId() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold the expected inventories
		Map<MaterialsProducerId, Map<MaterialId, Set<BatchId>>> expectedInventoryBatchesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			Map<MaterialId, Set<BatchId>> materialToBatchesMap = new LinkedHashMap<>();
			expectedInventoryBatchesMap.put(testMaterialsProducerId, materialToBatchesMap);
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				materialToBatchesMap.put(testMaterialId, new LinkedHashSet<>());
			}
		}
		/*
		 * Have the various material producers create batches and place them in
		 * inventory. Add some batches to stages. Destroy some of the batches.
		 */
		for (int k = 0; k < 10; k++) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(materialsProducerId);

				// create some (100) batches
				for (int i = 0; i < 100; i++) {
					MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, randomGenerator.nextInt(100), randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					materialToBatchesMap.get(materialId).add(batchId);
				}

				// show that the inventory batches are correct
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batchList = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
					Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
					assertEquals(batchList.size(), actualInventoryBatches.size());
					Set<BatchId> expectedInventoryBatches = materialToBatchesMap.get(testMaterialId);
					assertEquals(expectedInventoryBatches, actualInventoryBatches);
				}

				/*
				 * Create some stages and put some (25) of the batches onto
				 * stages
				 * 
				 */
				List<BatchId> batches = new ArrayList<>();
				for (Set<BatchId> expectedInventoryBatches : materialToBatchesMap.values()) {
					batches.addAll(expectedInventoryBatches);
				}

				Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
				int batchIndex = 0;
				for (int i = 0; i < 5; i++) {
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						materialsDataManager.moveBatchToStage(batchId, stageId);
						MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
						materialToBatchesMap.get(batchMaterial).remove(batchId);

					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
					materialsDataManager.removeBatch(batchId);
					materialToBatchesMap.get(batchMaterial).remove(batchId);
				}

			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			// show that the inventory batches are correct
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> invBatches = materialsDataManager.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);
					Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(invBatches);
					assertEquals(invBatches.size(), actualInventoryBatches.size());
					Set<BatchId> expectedInventoryBatches = materialToBatchesMap.get(testMaterialId);
					assertEquals(expectedInventoryBatches, actualInventoryBatches);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8436700054410844417L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8066333940937253765L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatchesByMaterialId(null, TestMaterialId.MATERIAL_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3143917391309849287L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestMaterialId.MATERIAL_2);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the material id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 874196115936784556L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		/* precondition test: if the material id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 9112311292467047420L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.getUnknownMaterialId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6611654668838622496L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialId.class), materialsDataManager.getMaterialIds());
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerIds", args = {})
	public void testGetMaterialsProducerIds() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3824970086302200338L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialsProducerId.class), materialsDataManager.getMaterialsProducerIds());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "materialIdExists", args = { MaterialId.class })
	public void testMaterialIdExists() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6918669723394457093L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				assertTrue(materialsDataManager.materialIdExists(testMaterialId));
			}
			assertFalse(materialsDataManager.materialIdExists(TestMaterialId.getUnknownMaterialId()));
			assertFalse(materialsDataManager.materialIdExists(null));
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerPropertyDefinition", args = { MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7151961147034751776L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsDaView.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer property id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4030472148503907839L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyDefinition(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materials producer property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 863172317284141879L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyDefinition(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8718225529106870071L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class), materialsDaView.getMaterialsProducerPropertyIds());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerPropertyTime", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, MutableDouble> expectedTimesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				expectedTimesMap.put(new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId), new MutableDouble());
			}
		}

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDaView.getMaterialsProducerPropertyTime(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4362229716953652532L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8047663013308359028L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyTime(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7076209560671384217L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
						TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8444324674368897195L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materials producerId property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3195486517854831744L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, Object> expectedValuesMap = new LinkedHashMap<>();

		// initialize the materials data manager
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object value = materialsDaView.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					expectedValuesMap.put(multiKey, value);
				}
			}

		}));

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedValuesMap.put(multiKey, propertyValue);
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object expectedValue = expectedValuesMap.get(multiKey);
					Object actualValue = materialsDaView.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3587272435527239583L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4247143641356704364L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyValue(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7731689857034028615L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
						TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1004792420489047936L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materials producerId property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 133851619411326116L, (c) -> {
				MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
				materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceLevel() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected resource levels for producers
		Map<MultiKey, MutableLong> expectedLevelsMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedLevelsMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableLong());
			}
		}

		// determine a reasonable number of changes per time
		int resourceLevelChangeCount = TestMaterialsProducerId.size() * TestResourceId.size() / 10;

		// update several random resource levels values at various times
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int j = 0; j < resourceLevelChangeCount; j++) {

					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Long resourceLevel = (long) (randomGenerator.nextInt(100) + 1);
					materialsDataManager.convertStageToResource(stageId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					MutableLong mutableLong = expectedLevelsMap.get(multiKey);
					mutableLong.increment(resourceLevel);
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					long expectedLevel = expectedLevelsMap.get(multiKey).getValue();
					long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					assertEquals(expectedLevel, actualLevel);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1633676078121550637L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 11082575022266925L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getMaterialsProducerResourceLevel(null, TestResourceId.RESOURCE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6362058221207452078L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7531288497048301736L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2745862264533327311L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getOfferedStages", args = { MaterialsProducerId.class })
	public void testGetOfferedStages() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				/*
				 * create several stages and place about half of them into the
				 * offer state
				 */
				Set<StageId> expectedStageOffers = new LinkedHashSet<>();

				for (int i = 0; i < 100; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					if (randomGenerator.nextBoolean()) {
						expectedStageOffers.add(stageId);
						materialsDataManager.setStageOfferState(stageId, true);
					}
				}

				List<StageId> stages = materialsDataManager.getOfferedStages(testMaterialsProducerId);
				Set<StageId> actualStages = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStages.size());
				assertEquals(expectedStageOffers, actualStages);

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7995017020582510238L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the materials producerId id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1728234953072549300L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getOfferedStages(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition tests if the materials producerId id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 809512240800144004L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getOfferedStages(TestMaterialsProducerId.getUnknownMaterialsProducerId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expectations
		Map<StageId, Set<BatchId>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(3) + 1;
					Set<BatchId> batches = new LinkedHashSet<>();
					for (int j = 0; j < batchCount; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
								randomGenerator.nextInt(100), randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						batches.add(batchId);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					expectedStageBatches.put(stageId, batches);
				}
			}));
		}
		// have an actor show that the batches are staged as expected

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Map<StageId, Set<BatchId>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stageIds) {
					actualStageBatches.put(stageId, new LinkedHashSet<>(materialsDataManager.getStageBatches(stageId)));
				}
			}

			assertEquals(expectedStageBatches, actualStageBatches);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3682458920522952415L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7434749084817685354L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStageBatches(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition tests if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8988415576850624232L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStageBatches(new StageId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getStageBatchesByMaterialId", args = { StageId.class, MaterialId.class })
	public void testGetStageBatchesByMaterialId() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// build a structure to hold the batches expected per stage and
		// material id
		Map<StageId, Map<MaterialId, Set<BatchId>>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(10) + 1;
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					for (int j = 0; j < batchCount; j++) {
						TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						Set<BatchId> batches = map.get(materialId);
						if (batches == null) {
							batches = new LinkedHashSet<>();
							map.put(materialId, batches);
						}
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, randomGenerator.nextInt(100),
								randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						batches.add(batchId);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					expectedStageBatches.put(stageId, map);
				}

			}));
		}

		// have an actor show that the batches are staged as expected
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			/*
			 * gather the actual stage batches in a structure identical to the
			 * expected values
			 */
			Map<StageId, Map<MaterialId, Set<BatchId>>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataManager.getStages(materialsProducerId);

				for (StageId stageId : stageIds) {
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					actualStageBatches.put(stageId, map);
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : stageBatches) {
						MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
						Set<BatchId> set = map.get(materialId);
						if (set == null) {
							set = new LinkedHashSet<>();
							map.put(materialId, set);
						}
						set.add(batchId);
					}
				}
			}

			// show that the actual batches are retrievable by stage and
			// material id correctly
			assertEquals(expectedStageBatches, actualStageBatches);

		}));

		// precondition tests
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(10000000), TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the material id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(0), null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(0), TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2013967899243685546L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getStageProducer", args = { StageId.class })
	public void testGetStageProducer() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
				assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4322374809851867527L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3429442631390139742L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStageProducer(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8553021441594074433L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStageProducer(new StageId(1000000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getStages", args = { MaterialsProducerId.class })
	public void testGetStages() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				Set<StageId> expectedStages = new LinkedHashSet<>();

				int count = randomGenerator.nextInt(10) + 1;
				for (int i = 0; i < count; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					expectedStages.add(stageId);
				}

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);

				Set<StageId> actualStageIds = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStageIds.size());
				assertEquals(expectedStages, actualStageIds);
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3431193355375533655L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8012112350970626114L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStages(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4013634140214782310L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.getStages(TestMaterialsProducerId.getUnknownMaterialsProducerId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "isStageOffered", args = { StageId.class })
	public void testIsStageOffered() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (int i = 0; i < 100; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertFalse(materialsDataManager.isStageOffered(stageId));
				materialsDataManager.setStageOfferState(stageId, true);
				assertTrue(materialsDataManager.isStageOffered(stageId));
			}

			// precondition tests

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.isStageOffered(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.isStageOffered(new StageId(1000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 475901778920012875L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "materialsProducerIdExists", args = { MaterialsProducerId.class })
	public void testMaterialsProducerIdExists() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4005535514531641716L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				assertTrue(materialsDataManager.materialsProducerIdExists(testMaterialsProducerId));
			}
			assertFalse(materialsDataManager.materialsProducerIdExists(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertFalse(materialsDataManager.materialsProducerIdExists(null));
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "materialsProducerPropertyIdExists", args = { MaterialsProducerPropertyId.class })
	public void testMaterialsProducerPropertyIdExists() {

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8256309156804000329L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				assertTrue(materialsDataManager.materialsProducerPropertyIdExists(testMaterialsProducerPropertyId));
			}
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(null));
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "stageExists", args = { StageId.class })
	public void testStageExists() {
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4646356228574091149L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertTrue(materialsDataManager.stageExists(stageId));
			}
			assertFalse(materialsDataManager.stageExists(null));
			assertFalse(materialsDataManager.stageExists(new StageId(123)));
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "moveBatchToInventory", args = { BatchId.class })
	public void testMoveBatchToInventoryEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record batches being removed from stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(StageMembershipRemovalEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.batchId(), e.stageId());
				actualObservations.add(multiKey);
			});
		}));

		/*
		 * Have the materials producers create batches and place about half of
		 * them on stages
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int i = 0; i < 40; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
							randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}

				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}

			}));
		}

		/*
		 * Have the materials producers return some about half of the staged
		 * batches to inventory and show that the batches are now in inventory
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							materialsDataManager.moveBatchToInventory(batchId);
							// show that the batch was returned to inventory
							assertFalse(materialsDataManager.getBatchStageId(batchId).isPresent());
							assertTrue(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
							// create the observation
							MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
							expectedObservations.add(multiKey);
						}
					}
				}
			}));
		}

		// have the observer show that the correct observations were made
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 5136057466059323708L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 9033912130601526542L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.moveBatchToInventory(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4357313141781993780L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.moveBatchToInventory(new BatchId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch is not staged */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5899034328012868517L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 5.0,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.moveBatchToInventory(batchId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.BATCH_NOT_STAGED, contractException.getErrorType());

		/* precondition test: if the stage containing the batch is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6151702850690578711L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 5.0,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(batchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.moveBatchToInventory(batchId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "moveBatchToStage", args = { BatchId.class, StageId.class })
	public void testMoveBatchToStageEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record observations of batches being assigned to
		// stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(StageMembershipAdditionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.batchId(), e.stageId());
				actualObservations.add(multiKey);
			});
		}));

		// have the producers create several batches and stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 40; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
							randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}
			}));
		}

		// have the producers put about half of their batches onto stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);

						// show that the batch is now on the stage
						Optional<StageId> optionalStageId = materialsDataManager.getBatchStageId(batchId);
						assertTrue(optionalStageId.isPresent());
						StageId actualStageId = optionalStageId.get();
						assertEquals(stageId, actualStageId);

						// add the expected observation
						MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}
		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6845954292451913670L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8929308420703752743L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(null, stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2109267427404198126L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(new BatchId(100000000), stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch is already staged */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7854805166816872481L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(batchId, stageId);
				materialsDataManager.moveBatchToStage(batchId, stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.BATCH_ALREADY_STAGED, contractException.getErrorType());

		/* precondition test: if the stage id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7324432599621147973L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.moveBatchToStage(batchId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1695963867758678736L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.moveBatchToStage(batchId, new StageId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2664182628538225235L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.moveBatchToStage(batchId, stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

		/*
		 * precondition test: if batch and stage do not have the same owning
		 * materials producer
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 424341512515165163L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialId.MATERIAL_2, 12,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(batchId, stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "removeBatch", args = { BatchId.class })
	public void testRemoveBatch() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// construct data structures to hold observations
		Set<BatchId> expectedObservations = new LinkedHashSet<>();
		Set<BatchId> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(BatchImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(e.batchId());
			});
		}));

		/*
		 * Add batches -- although we will concentrate on producer 1, we will
		 * have the other producers generate batches for use in precondition
		 * tests
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
							randomGenerator.nextDouble(), randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
			}));
		}

		// remove some batches
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			for (BatchId batchId : inventoryBatches) {
				if (randomGenerator.nextBoolean()) {
					materialsDataManager.removeBatch(batchId);
					expectedObservations.add(batchId);
				}
			}
		}));

		// show that the batches were indeed removed
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (BatchId batchId : expectedObservations) {
				assertFalse(materialsDataManager.batchExists(batchId));
			}
		}));

		// show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2869388661813620663L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6338675888020916426L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.removeBatch(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3451796010171778050L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.removeBatch(new BatchId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch is on an offered stage */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2022222016456137374L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(batchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.removeBatch(batchId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "removeStage", args = { StageId.class, boolean.class })
	public void testRemoveStage() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(EventFilter.builder(StageImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});

			c.subscribe(EventFilter.builder(StageMembershipRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId(), e.stageId()));
			});

			c.subscribe(EventFilter.builder(BatchImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId()));
			});
		}));

		// have the producers create some stages with batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
								randomGenerator.nextDouble() + 0.01, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}
			}));
		}

		// have the producers destroy all their stages, returning about half of
		// the batches to inventory
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			Set<BatchId> expectedInventoryBatches = new LinkedHashSet<>();

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					boolean destroyBatches = randomGenerator.nextBoolean();
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					materialsDataManager.removeStage(stageId, destroyBatches);

					/*
					 * record the batch and stage ids that will be removed after
					 * the current agent activation so that they can be
					 * confirmed later
					 */
					if (destroyBatches) {
						batchesToConfirm.addAll(stageBatches);
					} else {
						expectedInventoryBatches.addAll(stageBatches);
					}
					stagesToConfirm.add(stageId);

					// generate the expected observations
					if (destroyBatches) {
						for (BatchId batchId : stageBatches) {
							expectedObservations.add(new MultiKey(c.getTime(), batchId));
						}
					}

					expectedObservations.add(new MultiKey(c.getTime(), stageId));

				}
			}));

			// show that the expected batches and stages were removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventoryBatches);
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}));

		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 886447125697525680L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7585484363151799317L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.removeStage(null, false);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2068986583725856249L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.removeStage(new StageId(1000000), false);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/* precondition test: if stage is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4255522387251717178L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.removeStage(stageId, false);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testBatchPropertyValueAssignmentEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(BatchPropertyUpdateEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.batchId(), e.batchPropertyId(), e.previousPropertyValue(), e.currentPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// create some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 10; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
							randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
			}));
		}

		// Alter about half of the mutable properties of batches over 5
		// different times
		for (int i = 0; i < 5; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
					for (BatchId batchId : inventoryBatches) {
						TestMaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
						for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(batchMaterial)) {
							if (testBatchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
								if (randomGenerator.nextBoolean()) {
									Object newPropertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
									Object oldPropertyValue = materialsDataManager.getBatchPropertyValue(batchId, testBatchPropertyId);
									expectedObservations.add(new MultiKey(c.getTime(), batchId, testBatchPropertyId, oldPropertyValue, newPropertyValue));
									materialsDataManager.setBatchPropertyValue(batchId, testBatchPropertyId, newPropertyValue);

									// show that the new property value is
									// present
									assertEquals(newPropertyValue, materialsDataManager.getBatchPropertyValue(batchId, testBatchPropertyId));
								}
							}
						}
					}
				}));
			}
		}

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6834204103777199004L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7018657860620291081L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
				Object propertyValue = 56;
				materialsDataManager.setBatchPropertyValue(null, batchPropertyId, propertyValue);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8208393972487550344L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
				Object propertyValue = 56;
				materialsDataManager.setBatchPropertyValue(new BatchId(100000), batchPropertyId, propertyValue);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1469429590277285989L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				Object propertyValue = 56;
				materialsDataManager.setBatchPropertyValue(batchId, null, propertyValue);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the batch property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5183183157525649267L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				Object propertyValue = 56;
				materialsDataManager.setBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId(), propertyValue);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if batch property is not mutable */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6735391483628478568L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.setBatchPropertyValue(batchId, TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK, false);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		/* precondition test: if the batch property value is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7110456544138996752L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, null);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the batch property value is not compatible with
		 * the corresponding property definition
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2721004733907727252L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5751385676704973926L);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, 12.4);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/* precondition test: if the batch in on an offered stage */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8421379216279187305L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
				Object propertyValue = 56;
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(batchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe all changes to all producer property values
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					EventFilter<MaterialsProducerPropertyUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(testMaterialsProducerId,
							testMaterialsProducerPropertyId);
					c.subscribe(eventFilter, (c2, e) -> {
						MultiKey multiKey = new MultiKey(c2.getTime(), e.materialsProducerId(), e.materialsProducerPropertyId(), e.previousPropertyValue(), e.currentPropertyValue());
						actualObservations.add(multiKey);
					});
				}
			}
		}));

		for (int i = 0; i < 200; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				// pick a random materials producer property and update it to a
				// random value
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object oldValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId);
				Object newValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId, newValue);

				// show that the new value is present
				assertEquals(newValue, materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId));

				// record the expected observation
				MultiKey multiKey = new MultiKey(c.getTime(), materialsProducerId, testMaterialsProducerPropertyId, oldValue, newValue);
				expectedObservations.add(multiKey);
			}));
		}

		// have the observer show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3888305479377600267L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6542684073527908815L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				Object propertyValue = 5;
				materialsDataManager.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7681762910631637513L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				Object propertyValue = 5;
				materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(), materialsProducerPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5021355716878157868L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				Object propertyValue = 5;
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materials producer property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6739449188760503613L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				Object propertyValue = 5;
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the materials producer property is immutable */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3015359065220869477L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				Object propertyValue = 5;
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		/* precondition test: if the property value is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2614476067199172944L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the property value is incompatible with the
		 * corresponding property definition
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5704066697861534404L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, 12.5);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "setStageOfferState", args = { StageId.class, boolean.class })
	public void testSetStageOfferState() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(StageOfferUpdateEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId(), e.previousOfferState(), e.currentOfferState()));
			});
		}));

		// have the producers create a few stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}
			}));
		}

		// have the producers make a few random offer state changes
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
					StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
					boolean isOffered = materialsDataManager.isStageOffered(stageId);
					materialsDataManager.setStageOfferState(stageId, !isOffered);

					// show that the offer state changed
					boolean newOfferState = materialsDataManager.isStageOffered(stageId);
					assertNotEquals(isOffered, newOfferState);

					// generate the expected observation
					expectedObservations.add(new MultiKey(c.getTime(), stageId, isOffered, !isOffered));
				}));
			}
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 916177724112971509L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5384463547664747393L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.setStageOfferState(null, true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 319106508275202491L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.setStageOfferState(new StageId(10000000), true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "transferMaterialBetweenBatches", args = { BatchId.class, BatchId.class, double.class })
	public void testBatchContentShiftEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;

		// create data structures to

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the movement of materials between batches
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(BatchAmountUpdateEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(e.batchId(), e.previousAmount(), e.currentAmount());
				actualObservations.add(multiKey);
			});
		}));

		// Have the materials producers create a few batches, ensuring the
		// amount in each batch is positive
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					for (int i = 0; i < 10; i++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, randomGenerator.nextDouble() + 0.1,
								randomGenerator);
						materialsDataManager.addBatch(batchConstructionInfo);
					}
				}
			}));
		}

		/*
		 * We will concentrate on just producer 2 for most of the test but will
		 * utilize batches in other producers in some tests
		 */
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		// have the materials producer swap material amount around
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
				int index1 = randomGenerator.nextInt(batches.size());
				int index2 = randomGenerator.nextInt(batches.size());
				if (index2 == index1) {
					index2++;
					index2 = index2 % batches.size();
				}
				BatchId batchId1 = batches.get(index1);
				BatchId batchId2 = batches.get(index2);
				double batchAmount1 = materialsDataManager.getBatchAmount(batchId1);
				double batchAmount2 = materialsDataManager.getBatchAmount(batchId2);
				// ensure that we transfer a positive amount, but not the whole
				// amount
				double portion = randomGenerator.nextDouble() * 0.8 + 0.1;
				double transferAmount = batchAmount1 * portion;
				materialsDataManager.transferMaterialBetweenBatches(batchId1, batchId2, transferAmount);

				double batchAmount3 = materialsDataManager.getBatchAmount(batchId1);
				double batchAmount4 = materialsDataManager.getBatchAmount(batchId2);

				assertEquals(batchAmount1 - transferAmount, batchAmount3, 0.00000000001);
				assertEquals(batchAmount2 + transferAmount, batchAmount4, 0.00000000001);

				MultiKey multiKey = new MultiKey(batchId1, batchAmount1, batchAmount3);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(batchId2, batchAmount2, batchAmount4);
				expectedObservations.add(multiKey);
			}
		}));

		// Have the observer show that the observations were generated correctly
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6185579658134885353L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the source batch id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4531031694400929670L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(null, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the source batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6565712732641056695L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchId sourceBatchId = new BatchId(100000);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the destination batch id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2331763843587032989L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = null;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		/* precondition test: if the destination batch id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6191035775701595700L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = new BatchId(10000000);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * precondition test: if the source and destination batches are the same
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3554772523918373156L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = sourceBatchId;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);

			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.REFLEXIVE_BATCH_SHIFT, contractException.getErrorType());

		/*
		 * precondition test: if the batches do not have the same material type
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7162782209932339508L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.MATERIAL_TYPE_MISMATCH, contractException.getErrorType());

		/*
		 * precondition test: if the batches have different owning materials
		 * producers
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5391338933894482101L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS, contractException.getErrorType());

		/* precondition test: if the source batch is on a stage is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3272165493215915111L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(sourceBatchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

		/*
		 * precondition test: if the destination batch is on a stage is offered
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 472094558069917703L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(destinationBatchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

		/* precondition test: if the shift amount is not a finite number */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2032850391793850929L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, Double.POSITIVE_INFINITY);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		/* precondition test: if the shift amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5245408361837825062L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, -0.5);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the shift amount exceeds the available material
		 * on the source batch
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8724998204446972180L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0,
						randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 2.0);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.INSUFFICIENT_MATERIAL_AVAILABLE, contractException.getErrorType());

		/*
		 * precondition test: if the shift amount would cause an overflow on the
		 * destination batch
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3007064903100070620L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1,
						Double.MAX_VALUE, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, Double.MAX_VALUE, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				double amount = Double.MAX_VALUE / 2;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, amount);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "transferOfferedStage", args = { StageId.class, MaterialsProducerId.class })
	public void testTransferOfferedStage() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(StageMaterialsProducerUpdateEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.stageId(), e.previousMaterialsProducerId(), e.currentMaterialsProducerId());
				actualObservations.add(multiKey);
			});

			c.subscribe(EventFilter.builder(StageOfferUpdateEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.stageId());
				actualObservations.add(multiKey);

			});
		}));

		int stagesPerProducer = 5;
		int transferCount = stagesPerProducer * TestMaterialsProducerId.values().length;

		// have the materials producers create a few offered stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (int i = 0; i < stagesPerProducer; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					for (int j = 0; j < 3; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator),
								randomGenerator.nextDouble() + 0.01, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					materialsDataManager.setStageOfferState(stageId, true);
					MultiKey multiKey = new MultiKey(c.getTime(), stageId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have an agent transfer offered stages
		for (int i = 0; i < transferCount; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// determine the stages to transfer

				List<StageId> stagesToTransfer = new ArrayList<>();
				for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
					List<StageId> offeredStages = materialsDataManager.getOfferedStages(testMaterialsProducerId);
					stagesToTransfer.addAll(offeredStages);
				}
				StageId stageId = stagesToTransfer.get(randomGenerator.nextInt(stagesToTransfer.size()));

				// select a producer at random to receive the transfered
				// stage

				MaterialsProducerId stageProducer = materialsDataManager.getStageProducer(stageId);
				List<MaterialsProducerId> candidateProducers = new ArrayList<>(materialsDataManager.getMaterialsProducerIds());
				candidateProducers.remove(stageProducer);
				MaterialsProducerId altProducer = candidateProducers.get(randomGenerator.nextInt(candidateProducers.size()));

				// transfer the stage
				materialsDataManager.transferOfferedStage(stageId, altProducer);

				// show that the stage was properly transferred
				assertEquals(altProducer, materialsDataManager.getStageProducer(stageId));

				// record expected observations

				MultiKey multiKey = new MultiKey(c.getTime(), stageId, stageProducer, altProducer);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(c.getTime(), stageId);
				expectedObservations.add(multiKey);

			}));
		}

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3739485201643969207L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1130010427224075392L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(null, altProducer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3682112595474000731L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(new StageId(10000000), altProducer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2900230239814256887L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, null);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 452781590728467653L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, TestMaterialsProducerId.getUnknownMaterialsProducerId());
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the stage is not offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6778669475043282422L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.setStageOfferState(stageId, false);
				materialsDataManager.transferOfferedStage(stageId, altProducer);
				materialsDataManager.setStageOfferState(stageId, true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE, contractException.getErrorType());

		/*
		 * precondition test: if the source and destination materials producers
		 * are the same
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4646205108657064829L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.REFLEXIVE_STAGE_TRANSFER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "transferResourceToRegion", args = { MaterialsProducerId.class, ResourceId.class, RegionId.class, long.class })
	public void testTransferResourceToRegion() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe resource transfers from producers to regions.
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			/*
			 * When transferring resources from a producer to a region, a
			 * RegionResourceAdditionEvent is generated. This is not an
			 * observation, but is the contracted reaction event that is then
			 * processed by the resources package. To assess that this event is
			 * indeed generated we could add a custom resolver, but choose
			 * instead to have the observer agent subscribe to the resulting
			 * RegionResourceUpdateEvent
			 */

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						MultiKey multiKey = new MultiKey(c.getTime(), e.resourceId(), e.regionId(), e.previousResourceLevel(), e.currentResourceLevel());
						actualObservations.add(multiKey);
					});
				}
			}

			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<MaterialsProducerResourceUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.resourceId(), e.materialsProducerId(), e.previousResourceLevel(), e.currentResourceLevel());
					actualObservations.add(multiKey);
				});
			}

		}));

		// have the producers generate some resources
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (TestResourceId testResourceId : TestResourceId.values()) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
						long amount = (randomGenerator.nextInt(1000) + 100);
						materialsDataManager.convertStageToResource(stageId, testResourceId, amount);
						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, 0L, amount);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}

		// have an agent distribute the resources over time
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					if (materialsProducerResourceLevel > 0) {
						long amountToTransfer = randomGenerator.nextInt((int) materialsProducerResourceLevel) + 1;
						TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
						long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
						materialsDataManager.transferResourceToRegion(testMaterialsProducerId, testResourceId, regionId, amountToTransfer);

						// show that the resource was transfered
						long currentProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
						long currentRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);

						assertEquals(materialsProducerResourceLevel - amountToTransfer, currentProducerResourceLevel);
						assertEquals(regionResourceLevel + amountToTransfer, currentRegionResourceLevel);

						// record the expected observations

						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, regionId, regionResourceLevel, currentRegionResourceLevel);
						expectedObservations.add(multiKey);
						multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, materialsProducerResourceLevel, currentProducerResourceLevel);
						expectedObservations.add(multiKey);

					}
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2289322490697828226L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1367796071113751106L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, null, regionId, amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8014590590926533288L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, TestResourceId.getUnknownResourceId(), regionId, amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the region id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4865873025074936636L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, null, amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the region id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4472671173642659805L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, TestRegionId.getUnknownRegionId(), amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is null */

		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6956131170154399460L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(null, resourceId, regionId, amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1760306489660703762L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, regionId, amountToTransfer);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2214714534579989103L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, -1L);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the materials amount exceeds the resource level
		 * of the materials producer
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8260344965557977221L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, amountToTransfer * 2);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

		/*
		 * precondition test: if the materials amount would cause an overflow of
		 * the regions resource level
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4416313459810970424L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				RegionId regionId = TestRegionId.REGION_4;
				long amountToTransfer = 45L;
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, amountToTransfer);

				/*
				 * There is currently some of the resource present, so we will
				 * add half of the max value of long two times in a row. That
				 * will cause the region to overflow while keeping the producer
				 * from doing so
				 * 
				 */
				long hugeAmount = Long.MAX_VALUE / 2;
				stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, hugeAmount);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, hugeAmount);

				stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, hugeAmount);
				materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, hugeAmount);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testResourceIdAddition() {
		/*
		 * Have the actor add a resource id and show that the materials data
		 * manager will support the addition
		 */
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7336173642619419311L, (c) -> {
			ResourceId newResourceId = TestResourceId.getUnknownResourceId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceId(newResourceId, true);

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			assertTrue(materialsProducerIds.size() > 0);
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, newResourceId);
				assertEquals(0, materialsProducerResourceLevel);
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testMaterialsDataManagerInitialState() {
		long seed = 5272599676969321594L;
		int numBatches = 50;
		int numStages = 10;
		int numBatchesToStage = 30;

		MaterialsPluginData materialsPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches, numStages, numBatchesToStage, seed);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent to show that materials initial data was properly
		// loaded as reflected in the data view
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the correct materials producer ids are present
			assertEquals(materialsPluginData.getMaterialsProducerIds(), materialsDataManager.getMaterialsProducerIds());

			// show that the correct material ids are present
			assertEquals(materialsPluginData.getMaterialIds(), materialsDataManager.getMaterialIds());

			// show that the resource ids used for initial resource levels are
			// all contained in the resource plugin
			assertTrue(resourcesDataManager.getResourceIds().containsAll(materialsPluginData.getResourceIds()));

			// show that the material property ids are correct
			assertEquals(materialsPluginData.getMaterialsProducerPropertyIds(), materialsDataManager.getMaterialsProducerPropertyIds());

			// show that the material producer property definitions are correct
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
				assertEquals(materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId),
						materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId));
			}

			/*
			 * Show that the material producer property values are correct. Show
			 * that the initial resource levels are correct.
			 */
			for (MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
					Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsPluginData.getMaterialsProducerPropertyValues(materialsProducerId);
					Object expectedValue = materialsProducerPropertyValues.get(materialsProducerPropertyId);
					if (expectedValue == null) {
						PropertyDefinition propertyDefinition = materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
						expectedValue = propertyDefinition.getDefaultValue().get();
					}
					Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
				for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
					Long expectedResourceLevel = materialsPluginData.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					Long actualResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);
				}
			}
			/*
			 * Show that each material is associated with the correct batch
			 * property ids. Show that each batch property id has the correct
			 * definition.
			 */
			for (MaterialId materialId : materialsPluginData.getMaterialIds()) {
				assertEquals(materialsPluginData.getBatchPropertyIds(materialId), materialsDataManager.getBatchPropertyIds(materialId));
				for (BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(materialId)) {
					PropertyDefinition expectedPropertyDefinition = materialsPluginData.getBatchPropertyDefinition(materialId, batchPropertyId);
					PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// Show that the correct initial batches are present.
			Set<BatchId> expectedBatchIds = new LinkedHashSet<>();
			for (BatchId batchId : materialsPluginData.getBatchIds()) {
				expectedBatchIds.add(batchId);
			}

			Set<BatchId> actualBatchIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				actualBatchIds.addAll(materialsDataManager.getInventoryBatches(materialsProducerId));
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stages) {
					actualBatchIds.addAll(materialsDataManager.getStageBatches(stageId));
				}
			}
			assertEquals(expectedBatchIds, actualBatchIds);

			// Show that the batches have the correct material id, materials
			// producer and amounts

			for (BatchId batchId : materialsPluginData.getBatchIds()) {

				MaterialId expectedMaterialId = materialsPluginData.getBatchMaterial(batchId);
				MaterialId actualMaterialId = materialsDataManager.getBatchMaterial(batchId);
				assertEquals(expectedMaterialId, actualMaterialId);

				MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getBatchMaterialsProducer(batchId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataManager.getBatchProducer(batchId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				double expectedAmount = materialsPluginData.getBatchAmount(batchId);
				double actualAmount = materialsDataManager.getBatchAmount(batchId);
				assertEquals(expectedAmount, actualAmount);

				for (BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(expectedMaterialId)) {
					Object expectedValue = materialsPluginData.getBatchPropertyValues(batchId).get(batchPropertyId);
					if (expectedValue == null) {
						PropertyDefinition propertyDefinition = materialsPluginData.getBatchPropertyDefinition(expectedMaterialId, batchPropertyId);
						expectedValue = propertyDefinition.getDefaultValue().get();
					}
					Object actualValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
					assertEquals(expectedValue, actualValue);
				}

			}

			// Show that the correct initial stages are present with their
			// normalized id values.
			Set<StageId> expectedStageIds = new LinkedHashSet<>();
			for (StageId stageId : materialsPluginData.getStageIds()) {
				expectedStageIds.add(stageId);
			}

			Set<StageId> actualStageIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				actualStageIds.addAll(materialsDataManager.getStages(materialsProducerId));

			}
			assertEquals(expectedStageIds, actualStageIds);

			for (StageId stageId : materialsPluginData.getStageIds()) {

				MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getStageMaterialsProducer(stageId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataManager.getStageProducer(stageId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				boolean expectedOfferState = materialsPluginData.isStageOffered(stageId);
				boolean actualOfferStage = materialsDataManager.isStageOffered(stageId);
				assertEquals(expectedOfferState, actualOfferStage);

				Set<BatchId> expectedBatches = new LinkedHashSet<>();
				for (BatchId batchId : materialsPluginData.getStageBatches(stageId)) {
					expectedBatches.add(batchId);
				}
				Set<BatchId> actualBatches = new LinkedHashSet<>(materialsDataManager.getStageBatches(stageId));
				assertEquals(expectedBatches, actualBatches);
			}

		}));

		// add the test plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(numBatches, numStages, numBatchesToStage, seed, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "addMaterialsProducer", args = { MaterialsProducerConstructionData.class })
	public void testAddMaterialsProducer() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(20, (c) -> {
			c.subscribe(EventFilter.builder(MaterialsProducerAdditionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialsProducerId());
				actualObservations.add(multiKey);
			});
		}));

		// show that a new materials producer can be added
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(20, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
			assertFalse(materialsDataManager.materialsProducerIdExists(newMaterialsProducerId));

			MaterialsProducerConstructionData.Builder builder //
			= MaterialsProducerConstructionData	.builder()//
												.setMaterialsProducerId(newMaterialsProducerId);//

			Map<TestMaterialsProducerPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				Optional<Object> optional = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue();
				if (optional.isPresent()) {
					expectedPropertyValues.put(testMaterialsProducerPropertyId, optional.get());
				} else {
					Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
					expectedPropertyValues.put(testMaterialsProducerPropertyId, randomPropertyValue);
				}
			}

			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();

			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			assertTrue(materialsDataManager.materialsProducerIdExists(newMaterialsProducerId));

			double expectedTime = c.getTime();
			int propertyTimeIsCurrentTimeCount = 0;
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				Object expectedValue = expectedPropertyValues.get(testMaterialsProducerPropertyId);
				Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(newMaterialsProducerId, testMaterialsProducerPropertyId);
				assertEquals(expectedValue, actualValue);

				double actualTime = materialsDataManager.getMaterialsProducerPropertyTime(newMaterialsProducerId, testMaterialsProducerPropertyId);

				boolean defaultValueExists = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isPresent();

				if (defaultValueExists) {
					assertEquals(0.0, actualTime);
				} else {
					propertyTimeIsCurrentTimeCount++;
					assertEquals(expectedTime, actualTime);
				}
			}

			assertTrue(propertyTimeIsCurrentTimeCount > 0);
			long expectedLevel = 0;
			for (TestResourceId testResourceId : TestResourceId.values()) {
				long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(newMaterialsProducerId, testResourceId);
				assertEquals(expectedLevel, actualLevel);				
			}

			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialsProducerId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(21, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7934044435210594542L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the materials producer id is already present
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6261955547781316622L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerConstructionData.Builder builder = //
						MaterialsProducerConstructionData.builder();
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
					Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
				}
				MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();
				materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materialsProducerConstructionData does not
		 * contain a property value for any corresponding materials producer
		 * property definition that lacks a default value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 1777796798041842032L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
				MaterialsProducerConstructionData materialsProducerConstructionData = MaterialsProducerConstructionData.builder().setMaterialsProducerId(newMaterialsProducerId).build();
				materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * precondition test: if the materialsProducerConstructionData contains
		 * a property value assignment for an unknown materials producer
		 * property id.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 405967616866830371L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerConstructionData.Builder builder = //
						MaterialsProducerConstructionData.builder();
				builder.setMaterialsProducerId(TestMaterialsProducerId.getUnknownMaterialsProducerId());
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
					Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
				}

				builder.setMaterialsProducerPropertyValue(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), 10);

				MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();

				materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);

			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "defineBatchProperty", args = { BatchPropertyDefinitionInitialization.class })
	public void testDefineBatchProperty() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7985084158958183488L);

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(BatchPropertyDefinitionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.materialId(), e.batchPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																																.setPropertyDefinition(propertyDefinition).build();
			materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			MultiKey multiKey = new MultiKey(c.getTime(), materialId, batchPropertyId);
			expectedObservations.add(multiKey);
			assertTrue(materialsDataManager.getBatchPropertyIds(materialId).contains(batchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_2;
			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																																.setPropertyDefinition(propertyDefinition).build();
			materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			MultiKey multiKey = new MultiKey(c.getTime(), materialId, batchPropertyId);
			expectedObservations.add(multiKey);
			assertTrue(materialsDataManager.getBatchPropertyIds(materialId).contains(batchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2434116219643564071L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the material id is unknown
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3376758409444036216L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
				BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
				MaterialId materialId = TestMaterialId.getUnknownMaterialId();

				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
						BatchPropertyDefinitionInitialization	.builder()//
																.setMaterialId(materialId)//
																.setPropertyId(batchPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();
				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if the batch property id is already present
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7152319084879177681L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
				BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
				MaterialId materialId = TestMaterialId.MATERIAL_1;

				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
						BatchPropertyDefinitionInitialization	.builder()//
																.setMaterialId(materialId)//
																.setPropertyId(batchPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();

				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition test: if a batch property value assignment has an
		// unknown batch id</li>
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8540977102873288312L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
				BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
				MaterialId materialId = TestMaterialId.MATERIAL_1;

				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
						BatchPropertyDefinitionInitialization	.builder()//
																.setMaterialId(materialId)//
																.setPropertyId(batchPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.addPropertyValue(new BatchId(765), 88)//
																.build();

				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch property value assignment has a batch
		 * id associated with a different material id type
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7122546603543728978L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
				BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
				MaterialId materialId = TestMaterialId.MATERIAL_1;
				MaterialId altMaterialId = TestMaterialId.MATERIAL_2;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, altMaterialId, 345.0, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
						BatchPropertyDefinitionInitialization	.builder()//
																.setMaterialId(materialId)//
																.setPropertyId(batchPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.addPropertyValue(batchId, 45)//
																.build();

				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);

			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(MaterialsError.MATERIAL_TYPE_MISMATCH, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "addMaterialId", args = { MaterialId.class })
	public void testAddMaterialId() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(MaterialIdAdditionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.materialId());
				actualObservations.add(multiKey);
			});
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId newMaterialId = TestMaterialId.getUnknownMaterialId();
			materialsDataManager.addMaterialId(newMaterialId);
			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId newMaterialId = TestMaterialId.getUnknownMaterialId();
			materialsDataManager.addMaterialId(newMaterialId);
			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2713286843450316570L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the material id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 801838096204060748L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.addMaterialId(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if the material id is already present
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4318358212946306160L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.addMaterialId(TestMaterialId.MATERIAL_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIAL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerPropertyUpdateEvent", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetEventFilterForMaterialsProducerPropertyUpdateEvent_Producer_Property() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<Pair<MaterialsProducerId, MaterialsProducerPropertyId>> selectedPairs = new LinkedHashSet<>();
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_5_INTEGER_MUTABLE_TRACK));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK));

		// have an agent observe all changes to the selected producer/property
		// pairs
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (Pair<MaterialsProducerId, MaterialsProducerPropertyId> pair : selectedPairs) {
				MaterialsProducerId materialsProducerId = pair.getFirst();
				MaterialsProducerPropertyId materialsProducerPropertyId = pair.getSecond();
				EventFilter<MaterialsProducerPropertyUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(materialsProducerId,
						materialsProducerPropertyId);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c2.getTime(), e.materialsProducerId(), e.materialsProducerPropertyId());
					actualObservations.add(multiKey);
				});
			}
		}));

		for (int i = 0; i < 200; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				// pick a random materials producer property and update it to a
				// random value
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object newValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId, newValue);

				Pair<MaterialsProducerId, MaterialsProducerPropertyId> pair = new Pair<>(materialsProducerId, testMaterialsProducerPropertyId);
				if (selectedPairs.contains(pair)) {
					MultiKey multiKey = new MultiKey(c.getTime(), materialsProducerId, testMaterialsProducerPropertyId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have the observer show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 462390115779917577L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4377185528158333370L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = null;
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(materialsProducerId, materialsProducerPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7996800014350194555L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
				materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(materialsProducerId, materialsProducerPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3912751053563409579L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = null;
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(materialsProducerId, materialsProducerPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the materials producer property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3228733928828489429L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent(materialsProducerId, materialsProducerPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerPropertyUpdateEvent", args = {})
	public void testGetEventFilterForMaterialsProducerPropertyUpdateEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe all changes producer properties
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			EventFilter<MaterialsProducerPropertyUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerPropertyUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.materialsProducerId(), e.materialsProducerPropertyId());
				actualObservations.add(multiKey);
			});

		}));

		for (int i = 0; i < 200; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				// pick a random materials producer property and update it to a
				// random value
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object newValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId, newValue);
				MultiKey multiKey = new MultiKey(c.getTime(), materialsProducerId, testMaterialsProducerPropertyId);
				expectedObservations.add(multiKey);

			}));
		}

		// have the observer show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6818565317427197123L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerResourceUpdateEvent", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetEventFilterForMaterialsProducerResourceUpdateEvent_Producer_Resource() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<Pair<MaterialsProducerId, ResourceId>> selectedPairs = new LinkedHashSet<>();
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_2));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestResourceId.RESOURCE_3));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestResourceId.RESOURCE_4));
		selectedPairs.add(new Pair<>(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestResourceId.RESOURCE_5));

		// have an actor observe the selected producer/resource updates
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (Pair<MaterialsProducerId, ResourceId> pair : selectedPairs) {
				MaterialsProducerId materialsProducerId = pair.getFirst();
				ResourceId resourceId = pair.getSecond();
				EventFilter<MaterialsProducerResourceUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId);

				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.materialsProducerId(), e.resourceId()));
				});
			}
		}));

		// have the producers generate batches via stage conversion
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, 2L);
				Pair<MaterialsProducerId, ResourceId> pair = new Pair<>(materialsProducerId, resourceId);
				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), materialsProducerId, resourceId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1943593849394263760L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the material producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7226633686166745691L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = null;
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the material producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7355528104369898437L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 767647396762963328L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				ResourceId resourceId = null;
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6053842863116555591L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
				ResourceId resourceId = TestResourceId.getUnknownResourceId();
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerResourceUpdateEvent", args = { ResourceId.class })
	public void testGetEventFilterForMaterialsProducerResourceUpdateEvent_Resource() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<ResourceId> selectedResources = new LinkedHashSet<>();
		selectedResources.add(TestResourceId.RESOURCE_1);
		selectedResources.add(TestResourceId.RESOURCE_3);
		selectedResources.add(TestResourceId.RESOURCE_5);

		// have an actor observe the selected resource updates
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (ResourceId resourceId : selectedResources) {
				EventFilter<MaterialsProducerResourceUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(resourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.materialsProducerId(), e.resourceId()));
				});
			}
		}));

		// have the producers generate batches via stage conversion
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, 2L);
				if (selectedResources.contains(resourceId)) {
					expectedObservations.add(new MultiKey(c.getTime(), materialsProducerId, resourceId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4810995292619714100L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7308248516735541073L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				ResourceId resourceId = null;
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 451212875681013142L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				ResourceId resourceId = TestResourceId.getUnknownResourceId();
				materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(resourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerResourceUpdateEvent", args = {})
	public void testGetEventFilterForMaterialsProducerResourceUpdateEvent() {
		// return EventFilter
		// .builder(MaterialsProducerResourceUpdateEvent.class)//
		// .build();

		/*
		 * Returns an event filter used to subscribe to {@link
		 * MaterialsProducerResourceUpdateEvent} events. Matches on all such
		 * events.
		 */
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe all resource updates
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			EventFilter<MaterialsProducerResourceUpdateEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.materialsProducerId(), e.resourceId()));
			});
		}));

		// have the producers generate batches via stage conversion
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.convertStageToResource(stageId, resourceId, 2L);
				expectedObservations.add(new MultiKey(c.getTime(), materialsProducerId, resourceId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7573518940281736875L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMaterialsProducerUpdateEvent_BySource", args = { MaterialsProducerId.class })
	public void testGetEventFilterForStageMaterialsProducerUpdateEvent_Source() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<MaterialsProducerId> selectedProducers = new LinkedHashSet<>();
		selectedProducers.add(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		selectedProducers.add(TestMaterialsProducerId.MATERIALS_PRODUCER_2);

		/*
		 * Have an actor observe all stage transfers where the source of the
		 * stage is one of the selected material producers
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (MaterialsProducerId materialsProducerId : selectedProducers) {
				EventFilter<StageMaterialsProducerUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.stageId(), e.previousMaterialsProducerId(), e.currentMaterialsProducerId());
					actualObservations.add(multiKey);
				});
			}
		}));

		// have an actor transfer stages randomly between producers
		for (int i = 0; i < 100; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				TestMaterialsProducerId sourceProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerId destinationProducerId;
				do {
					destinationProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				} while (sourceProducerId.equals(destinationProducerId));

				StageId stageId = materialsDataManager.addStage(sourceProducerId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, destinationProducerId);
				if (selectedProducers.contains(sourceProducerId)) {
					MultiKey multiKey = new MultiKey(c.getTime(), stageId, sourceProducerId, destinationProducerId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have the observer show that the observations were as expected
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4736078884804179967L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3035182036041809215L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = null;
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5900407295303039835L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMaterialsProducerUpdateEvent_ByDestination", args = { MaterialsProducerId.class })
	public void testGetEventFilterForStageMaterialsProducerUpdateEvent_Destination() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<MaterialsProducerId> selectedProducers = new LinkedHashSet<>();
		selectedProducers.add(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		selectedProducers.add(TestMaterialsProducerId.MATERIALS_PRODUCER_2);

		/*
		 * Have an actor observe all stage transfers where the source of the
		 * stage is one of the selected material producers
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (MaterialsProducerId materialsProducerId : selectedProducers) {
				EventFilter<StageMaterialsProducerUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_ByDestination(materialsProducerId);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.stageId(), e.previousMaterialsProducerId(), e.currentMaterialsProducerId());
					actualObservations.add(multiKey);
				});
			}
		}));

		// have an actor transfer stages randomly between producers
		for (int i = 0; i < 100; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				TestMaterialsProducerId sourceProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerId destinationProducerId;
				do {
					destinationProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				} while (sourceProducerId.equals(destinationProducerId));

				StageId stageId = materialsDataManager.addStage(sourceProducerId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, destinationProducerId);
				if (selectedProducers.contains(destinationProducerId)) {
					MultiKey multiKey = new MultiKey(c.getTime(), stageId, sourceProducerId, destinationProducerId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have the observer show that the observations were as expected
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6640940500286757658L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the materials producer id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 8942138506228493899L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = null;
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_ByDestination(materialsProducerId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/* precondition test: if the materials producer id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5949527361688842922L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_ByDestination(materialsProducerId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMaterialsProducerUpdateEvent", args = { StageId.class })
	public void testGetEventFilterForStageMaterialsProducerUpdateEvent_Stage() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<StageId> selectedStages = new LinkedHashSet<>();
		Set<StageId> nonSelectedStages = new LinkedHashSet<>();

		// have an actor create multiple stage, some of which will be observed
		// in transfer by the observer
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				for (int i = 0; i < 10; i++) {
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					materialsDataManager.setStageOfferState(stageId, true);
					selectedStages.add(stageId);
					stageId = materialsDataManager.addStage(materialsProducerId);
					materialsDataManager.setStageOfferState(stageId, true);
					nonSelectedStages.add(stageId);
				}
			}

		}));

		/*
		 * Have an actor observe all stage transfers where the stage id is one
		 * of the selected stages
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (StageId stageId : selectedStages) {
				EventFilter<StageMaterialsProducerUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent(stageId);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.stageId(), e.previousMaterialsProducerId(), e.currentMaterialsProducerId());
					actualObservations.add(multiKey);
				});
			}
		}));

		// have an actor transfer stages randomly between producers
		for (int i = 0; i < 100; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();

				for (MaterialsProducerId sourceProducerId : materialsProducerIds) {
					List<StageId> stages = materialsDataManager.getStages(sourceProducerId);
					TestMaterialsProducerId destinationProducerId;
					for (StageId stageId : stages) {
						if (materialsDataManager.isStageOffered(stageId)) {
							do {
								destinationProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
							} while (sourceProducerId.equals(destinationProducerId));
							materialsDataManager.transferOfferedStage(stageId, destinationProducerId);
							if (selectedStages.contains(stageId)) {
								MultiKey multiKey = new MultiKey(c.getTime(), stageId, sourceProducerId, destinationProducerId);
								expectedObservations.add(multiKey);
							}
						}
					}
				}
			}));
		}

		// have the observer show that the observations were as expected
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3296787354687433406L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5129361648713614556L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = null;
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent(stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 99312324736600050L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = new StageId(100000000);
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent(stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMaterialsProducerUpdateEvent", args = {})
	public void testGetEventFilterForStageMaterialsProducerUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		/*
		 * Have an actor observe all stage transfers
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			EventFilter<StageMaterialsProducerUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.stageId(), e.previousMaterialsProducerId(), e.currentMaterialsProducerId());
				actualObservations.add(multiKey);
			});

		}));

		// have an actor transfer stages randomly between producers
		for (int i = 0; i < 100; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				TestMaterialsProducerId sourceProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerId destinationProducerId;
				do {
					destinationProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				} while (sourceProducerId.equals(destinationProducerId));

				StageId stageId = materialsDataManager.addStage(sourceProducerId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferOfferedStage(stageId, destinationProducerId);

				MultiKey multiKey = new MultiKey(c.getTime(), stageId, sourceProducerId, destinationProducerId);
				expectedObservations.add(multiKey);

			}));
		}

		// have the observer show that the observations were as expected
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6150408500189298357L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageOfferUpdateEvent", args = { StageId.class })
	public void testGetEventFilterForStageOfferUpdateEvent_Stage() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<StageId> selectedStages = new LinkedHashSet<>();

		/*
		 * have an agent create some stages in various offer states and select
		 * some of them for the observer to observe.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					materialsDataManager.setStageOfferState(stageId, randomGenerator.nextBoolean());
					if (randomGenerator.nextBoolean()) {
						selectedStages.add(stageId);
					}
				}
			}
		}));

		// have a agent observe stage creations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (StageId stageId : selectedStages) {
				EventFilter<StageOfferUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageOfferUpdateEvent(stageId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
				});
			}
		}));

		// have the actor randomly choose to change some of the offer states
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stages) {
					if (randomGenerator.nextBoolean()) {
						boolean newOfferState = !materialsDataManager.isStageOffered(stageId);
						materialsDataManager.setStageOfferState(stageId, newOfferState);
						if (selectedStages.contains(stageId)) {
							expectedObservations.add(new MultiKey(c.getTime(), stageId));
						}
					}
				}
			}
		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2427005100525993777L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the stage id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4844028463801822799L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = null;
				materialsDataManager.getEventFilterForStageOfferUpdateEvent(stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 7970389114090461374L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = new StageId(10000000);
				materialsDataManager.getEventFilterForStageOfferUpdateEvent(stageId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageOfferUpdateEvent", args = {})
	public void testGetEventFilterForStageOfferUpdateEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		/*
		 * have an agent create some stages in various offer states and select
		 * some of them for the observer to observe.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					materialsDataManager.setStageOfferState(stageId, randomGenerator.nextBoolean());
				}
			}
		}));

		// have a agent observe stage creations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<StageOfferUpdateEvent> eventFilter = materialsDataManager.getEventFilterForStageOfferUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});

		}));

		// have the actor randomly choose to change some of the offer states
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stages) {
					if (randomGenerator.nextBoolean()) {
						boolean newOfferState = !materialsDataManager.isStageOffered(stageId);
						materialsDataManager.setStageOfferState(stageId, newOfferState);
						expectedObservations.add(new MultiKey(c.getTime(), stageId));
					}
				}
			}
		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7611854826274953331L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForBatchAdditionEvent", args = {})
	public void testGetEventFilterForBatchAdditionEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe batch creations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<BatchAdditionEvent> eventFilter = materialsDataManager.getEventFilterForBatchAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId()));
			});

		}));

		// have the actor randomly add some batches
		for (int i = 0; i < 30; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();
				//
				batchBuilder.setMaterialsProducerId(materialsProducerId)//
							.setMaterialId(testMaterialId)//
							.setAmount(amount);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				expectedObservations.add(new MultiKey(c.getTime(), batchId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8733374899306819910L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForBatchAmountUpdateEvent", args = {})
	public void testGetEventFilterForBatchAmountUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe batch amount updates
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<BatchAmountUpdateEvent> eventFilter = materialsDataManager.getEventFilterForBatchAmountUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId()));
			});

		}));

		// have the actor randomly add some batches and then alter the amounts
		for (int i = 0; i < 30; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble() + 0.01;
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();

				batchBuilder.setMaterialsProducerId(materialsProducerId)//
							.setMaterialId(testMaterialId)//
							.setAmount(amount);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();
				BatchId batchId1 = materialsDataManager.addBatch(batchConstructionInfo);

				batchBuilder.setMaterialsProducerId(materialsProducerId)//
							.setMaterialId(testMaterialId)//
							.setAmount(amount);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}

				batchConstructionInfo = batchBuilder.build();
				BatchId batchId2 = materialsDataManager.addBatch(batchConstructionInfo);

				amount = materialsDataManager.getBatchAmount(batchId1) / 2;
				materialsDataManager.transferMaterialBetweenBatches(batchId1, batchId2, amount);

				expectedObservations.add(new MultiKey(c.getTime(), batchId1));
				expectedObservations.add(new MultiKey(c.getTime(), batchId2));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1632036988086563905L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForBatchImminentRemovalEvent", args = {})
	public void testGetEventFilterForBatchImminentRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe batch removals
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<BatchImminentRemovalEvent> eventFilter = materialsDataManager.getEventFilterForBatchImminentRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId()));
			});

		}));

		// have the actor randomly add some batches and then remove them
		for (int i = 0; i < 30; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble() + 0.01;
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();

				batchBuilder.setMaterialsProducerId(materialsProducerId)//
							.setMaterialId(testMaterialId)//
							.setAmount(amount);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.removeBatch(batchId);

				expectedObservations.add(new MultiKey(c.getTime(), batchId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7418141671964137152L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForBatchPropertyDefinitionEvent", args = {})
	public void testGetEventFilterForBatchPropertyDefinitionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe batch property definition constructions
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<BatchPropertyDefinitionEvent> eventFilter = materialsDataManager.getEventFilterForBatchPropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchPropertyId()));
			});

		}));

		// have the actor randomly add some batch properties
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				int defaultValue = randomGenerator.nextInt(100);
				PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																			.setType(Integer.class)//
																			.setDefaultValue(defaultValue)//
																			.build();
				BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
						BatchPropertyDefinitionInitialization	.builder()//
																.setMaterialId(testMaterialId).setPropertyDefinition(propertyDefinition)//
																.setPropertyId(batchPropertyId)//
																.build();//
				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);

				expectedObservations.add(new MultiKey(c.getTime(), batchPropertyId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 1659719780457752005L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForBatchPropertyUpdateEvent", args = {})
	public void testGetEventFilterForBatchPropertyUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe batch property property updates
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<BatchPropertyUpdateEvent> eventFilter = materialsDataManager.getEventFilterForBatchPropertyUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId(), e.batchPropertyId()));
			});

		}));

		// have the actor randomly add some batches and change their properties
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble() + 0.01;
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();

				batchBuilder.setMaterialsProducerId(materialsProducerId)//
							.setMaterialId(testMaterialId)//
							.setAmount(amount);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(testMaterialId, randomGenerator);
				Object propertyValue = batchPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);

				expectedObservations.add(new MultiKey(c.getTime(), batchId, batchPropertyId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2839431361490510612L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialIdAdditionEvent", args = {})
	public void testGetEventFilterForMaterialIdAdditionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe the addition of material types
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<MaterialIdAdditionEvent> eventFilter = materialsDataManager.getEventFilterForMaterialIdAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.materialId()));
			});

		}));

		// have the actor add some material ids
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialId materialId = TestMaterialId.getUnknownMaterialId();
				materialsDataManager.addMaterialId(materialId);
				expectedObservations.add(new MultiKey(c.getTime(), materialId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3016777797847869909L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerAdditionEvent", args = {})
	public void testGetEventFilterForMaterialsProducerAdditionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the addition of material producers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<MaterialsProducerAdditionEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getMaterialsProducerId()));
			});
		}));

		// have the actor randomly add some materials producers
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();

				MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();

				builder.setMaterialsProducerId(materialsProducerId);//

				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, propertyValue);
				}

				MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();
				materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);

				expectedObservations.add(new MultiKey(c.getTime(), materialsProducerId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 9030121507723724675L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForMaterialsProducerPropertyDefinitionEvent", args = {})
	public void testGetEventFilterForMaterialsProducerPropertyDefinitionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the definition of material producer properties
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<MaterialsProducerPropertyDefinitionEvent> eventFilter = materialsDataManager.getEventFilterForMaterialsProducerPropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.materialsProducerPropertyId()));
			});
		}));

		// have the actor randomly define some materials producer properties
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
				int defaultValue = randomGenerator.nextInt(100);
				PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																			.setType(Integer.class)//
																			.setDefaultValue(defaultValue)//
																			.build();

				MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization = MaterialsProducerPropertyDefinitionInitialization	.builder()//
																																										.setMaterialsProducerPropertyId(
																																												materialsProducerPropertyId)//
																																										.setPropertyDefinition(
																																												propertyDefinition)//
																																										.build();
				materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyDefinitionInitialization);

				expectedObservations.add(new MultiKey(c.getTime(), materialsProducerPropertyId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2555168166874481212L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageAdditionEvent", args = {})
	public void testGetEventFilterForStageAdditionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the addition of stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<StageAdditionEvent> eventFilter = materialsDataManager.getEventFilterForStageAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});
		}));

		// have the actor add some stages
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);

				expectedObservations.add(new MultiKey(c.getTime(), stageId));
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 5930670132326679913L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageImminentRemovalEvent", args = {})
	public void testGetEventFilterForStageImminentRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the imminent removal of stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<StageImminentRemovalEvent> eventFilter = materialsDataManager.getEventFilterForStageImminentRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});
		}));

		// have the actor add some stages and then remove them
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.removeStage(stageId, false);

				expectedObservations.add(new MultiKey(c.getTime(), stageId));

			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 4965736606382697699L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMembershipAdditionEvent", args = {})
	public void testGetEventFilterForStageMembershipAdditionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the imminent removal of stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<StageImminentRemovalEvent> eventFilter = materialsDataManager.getEventFilterForStageImminentRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});
		}));

		// have the actor add some stages and then remove them
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				materialsDataManager.removeStage(stageId, false);

				expectedObservations.add(new MultiKey(c.getTime(), stageId));

			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3581801183499812974L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "getEventFilterForStageMembershipRemovalEvent", args = {})
	public void testGetEventFilterForStageMembershipRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the removal of batches from stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			EventFilter<StageMembershipRemovalEvent> eventFilter = materialsDataManager.getEventFilterForStageMembershipRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId(), e.batchId()));
			});
		}));

		// have the actor add some stages and then remove them
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();
				batchBuilder.setMaterialsProducerId(materialsProducerId);
				batchBuilder.setAmount(amount);
				batchBuilder.setMaterialId(materialId);
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(materialId)) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
				}
				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.moveBatchToStage(batchId, stageId);
				materialsDataManager.moveBatchToInventory(batchId);

				expectedObservations.add(new MultiKey(c.getTime(), stageId, batchId));

			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 6812070525878040557L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyDefinitionInitialization.class })
	public void testDefineMaterialsProducerProperty() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(EventFilter.builder(MaterialsProducerPropertyDefinitionEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.materialsProducerPropertyId()));
			});
		}));

		for (int i = 0; i < 15; i++) {
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100 * i).setPropertyValueMutability(false)//
																		.setType(Integer.class)//
																		.build();
			MaterialsProducerPropertyDefinitionInitialization matprodpropdefinit = MaterialsProducerPropertyDefinitionInitialization//
																																	.builder()
																																	.setMaterialsProducerPropertyId(materialsProducerPropertyId)
																																	.setPropertyDefinition(propertyDefinition)//
																																	.build();

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.defineMaterialsProducerProperty(matprodpropdefinit);
				expectedObservations.add(new MultiKey(c.getTime(), materialsProducerPropertyId));
			}));
		}
		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 2721085458686966421L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition: Materials producer property definition init is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3735323519290927676L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				materialsDataManager.defineMaterialsProducerProperty(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION_INITIALIZATION, contractException.getErrorType());

		// precondition: duplicate Materials producer property id
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 3735323519290927676L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
				MaterialsProducerPropertyDefinitionInitialization matprodpropdefinit = //
						MaterialsProducerPropertyDefinitionInitialization	.builder().setMaterialsProducerPropertyId(materialsProducerPropertyId)//
																			.setPropertyDefinition(materialsProducerPropertyId.getPropertyDefinition())//
																			.build();//

				materialsDataManager.defineMaterialsProducerProperty(matprodpropdefinit);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		// precondition: insufficient property value assignment
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6282192460518073310L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId();
				PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setPropertyValueMutability(false)//
																			.setType(Integer.class)//
																			.build();
				MaterialsProducerPropertyDefinitionInitialization matprodpropdefinit = //
						MaterialsProducerPropertyDefinitionInitialization	.builder()//
																			.setMaterialsProducerPropertyId(materialsProducerPropertyId)//
																			.setPropertyDefinition(propertyDefinition)//
																			.build();
				materialsDataManager.defineMaterialsProducerProperty(matprodpropdefinit);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "convertStageToResource", args = { StageId.class, ResourceId.class, long.class })
	public void testConvertStageToResource() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(EventFilter.builder(BatchImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.batchId(), "removal"));
			});

			c.subscribe(EventFilter.builder(MaterialsProducerResourceUpdateEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.resourceId(), e.currentResourceLevel(), "update"));
			});

			c.subscribe(EventFilter.builder(StageImminentRemovalEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.stageId()));
			});

		}));

		// have the producers generate batches via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					MaterialId materialId;
					ResourceId resourceId;
					double amount;
					long resourceAmount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					resourceId = TestResourceId.getRandomResourceId(randomGenerator);
					resourceAmount = 125L;

					long previousResourceAmount = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					long newResourceAmount = resourceAmount + previousResourceAmount;

					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					materialsDataManager.convertStageToResource(stageId, resourceId, resourceAmount);

					// record the stages and batches that should be removed, but
					// only after the current actor activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					assertTrue(resourcesDataManager.resourceIdExists(resourceId));
					assertEquals(newResourceAmount, materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId));

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "removal"));
					}
					expectedObservations.add(new MultiKey(c.getTime(), resourceId, newResourceAmount, "update"));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7822140774565669544L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2645688892533853761L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				long amount = 125L;
				materialsDataManager.convertStageToResource(stageId, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 5663564750797913460L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				long amount = 125L;
				materialsDataManager.convertStageToResource(stageId, TestResourceId.getUnknownResourceId(), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the stage id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4802037379297224622L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 125L;
				materialsDataManager.convertStageToResource(null, resourceId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		/* precondition test: if stage id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 2648372629715030136L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 125L;
				materialsDataManager.convertStageToResource(new StageId(10000000), resourceId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/* precondition test: if the stage is offered */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4249451090321590319L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 125L;
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.convertStageToResource(stageId, resourceId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

		/* precondition test: if the resource amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 6695497074307172608L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				materialsDataManager.convertStageToResource(stageId, resourceId, -1L);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/* precondition test: if the resource amount is not finite */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = MaterialsTestPluginFactory.factory(0, 0, 0, 4334935928753037959L, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				ResourceId resourceId = TestResourceId.RESOURCE_1;

				materialsDataManager.convertStageToResource(stageId, resourceId, 10L);
				materialsDataManager.convertStageToResource(stageId, resourceId, Long.MAX_VALUE);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());
	}

}
