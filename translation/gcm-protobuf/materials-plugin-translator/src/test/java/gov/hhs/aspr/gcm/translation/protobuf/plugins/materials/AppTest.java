package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchStatusReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestResourceHelper;
import plugins.materials.MaterialsPluginData;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.materials.reports.StageReportPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsTestPluginFactory;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public class AppTest {
	Path basePath = TestResourceHelper.getResourceDir(this.getClass());
	Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

	@Test
	public void testMaterialsTranslator() {
		String fileName = "pluginData.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), MaterialsPluginData.class)

				.build();

		int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;
		long seed = 524805676405822016L;

		MaterialsPluginData expectedPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches,
				numStages, numBatchesInStage, seed);
		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();
		MaterialsPluginData actualPluginData = translatorController.getObject(MaterialsPluginData.class);

		Set<TestMaterialId> expectedMaterialIds = EnumSet.allOf(TestMaterialId.class);
		assertFalse(expectedMaterialIds.isEmpty());

		Set<MaterialId> actualMaterialIds = actualPluginData.getMaterialIds();
		assertEquals(expectedMaterialIds, actualMaterialIds);

		for (TestMaterialId expectedMaterialId : expectedMaterialIds) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId
					.getTestBatchPropertyIds(expectedMaterialId);
			assertFalse(expectedBatchPropertyIds.isEmpty());
			Set<BatchPropertyId> actualBatchPropertyIds = actualPluginData.getBatchPropertyIds(expectedMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			for (TestBatchPropertyId batchPropertyId : expectedBatchPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = batchPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = actualPluginData.getBatchPropertyDefinition(
						expectedMaterialId,
						batchPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}

		Set<TestMaterialsProducerId> expectedMaterialsProducerIds = EnumSet.allOf(TestMaterialsProducerId.class);
		assertFalse(expectedMaterialsProducerIds.isEmpty());
		Set<MaterialsProducerId> actualProducerIds = actualPluginData.getMaterialsProducerIds();
		assertEquals(expectedMaterialsProducerIds, actualProducerIds);

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		assertEquals(numBatches * expectedMaterialsProducerIds.size(), actualPluginData.getBatchIds().size());
		assertEquals(numStages * expectedMaterialsProducerIds.size(), actualPluginData.getStageIds().size());

		int expectedNumBatchesPerStage = numBatchesInStage * expectedMaterialsProducerIds.size();
		int actualNumBatchesPerStage = 0;

		int bId = 0;
		int sId = 0;
		for (TestMaterialsProducerId expectedProducerId : expectedMaterialsProducerIds) {

			List<BatchId> batches = new ArrayList<>();
			for (int i = 0; i < numBatches; i++) {
				batches.add(new BatchId(bId++));
			}
			assertTrue(actualPluginData.getBatchIds().containsAll(batches));

			for (BatchId batchId : batches) {

				TestMaterialId expectedMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double expectedAmount = randomGenerator.nextDouble();

				MaterialId actualMaterialId = actualPluginData.getBatchMaterial(batchId);
				assertEquals(expectedMaterialId, actualMaterialId);

				double actualAmount = actualPluginData.getBatchAmount(batchId);
				assertEquals(expectedAmount, actualAmount);
				for (TestBatchPropertyId expectedBatchPropertyId : TestBatchPropertyId
						.getTestBatchPropertyIds(expectedMaterialId)) {
					if (expectedBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
							|| randomGenerator.nextBoolean()) {
						Object expectedPropertyValue = expectedBatchPropertyId.getRandomPropertyValue(randomGenerator);

						Map<BatchPropertyId, Object> propertyValueMap = actualPluginData
								.getBatchPropertyValues(batchId);
						assertTrue(propertyValueMap.containsKey(expectedBatchPropertyId));
						assertEquals(expectedPropertyValue, propertyValueMap.get(expectedBatchPropertyId));
					}
				}
			}

			List<StageId> stages = new ArrayList<>();
			for (int i = 0; i < numStages; i++) {
				stages.add(new StageId(sId++));
			}
			assertTrue(actualPluginData.getStageIds().containsAll(stages));

			for (int i = 0; i < stages.size(); i++) {
				MaterialsProducerId actualMaterialsProducerId = actualPluginData
						.getStageMaterialsProducer(stages.get(i));
				assertEquals(expectedProducerId, actualMaterialsProducerId);
				boolean expectedOffered = i % 2 == 0;
				boolean actualOffered = actualPluginData.isStageOffered(stages.get(i));
				assertTrue(expectedOffered == actualOffered);
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < numBatchesInStage; i++) {
				StageId expectedStageId = stages.get(randomGenerator.nextInt(stages.size()));
				BatchId expectedBatchId = batches.get(i);

				Set<BatchId> actualBatchIds = actualPluginData.getStageBatches(expectedStageId);
				assertTrue(actualBatchIds.contains(expectedBatchId));
				actualNumBatchesPerStage++;
			}

		}
		assertEquals(expectedNumBatchesPerStage, actualNumBatchesPerStage);

		Set<TestMaterialsProducerPropertyId> expectedMaterialsProducerPropertyIds = EnumSet
				.allOf(TestMaterialsProducerPropertyId.class);
		assertFalse(expectedMaterialsProducerPropertyIds.isEmpty());

		Set<MaterialsProducerPropertyId> actualMaterialProducerPropertyIds = actualPluginData
				.getMaterialsProducerPropertyIds();
		assertEquals(expectedMaterialsProducerPropertyIds, actualMaterialProducerPropertyIds);

		for (TestMaterialsProducerPropertyId expectedMaterialsProducerPropertyId : expectedMaterialsProducerPropertyIds) {
			PropertyDefinition expectedPropertyDefinition = expectedMaterialsProducerPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = actualPluginData
					.getMaterialsProducerPropertyDefinition(expectedMaterialsProducerPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			if (expectedPropertyDefinition.getDefaultValue().isEmpty()) {
				for (TestMaterialsProducerId producerId : expectedMaterialsProducerIds) {
					Object expectedPropertyValue = expectedMaterialsProducerPropertyId
							.getRandomPropertyValue(randomGenerator);
					Map<MaterialsProducerPropertyId, Object> propertyValueMap = actualPluginData
							.getMaterialsProducerPropertyValues(producerId);
					assertTrue(propertyValueMap.containsKey(expectedMaterialsProducerPropertyId));
					Object actualPropertyValue = propertyValueMap.get(expectedMaterialsProducerPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
				}
			}
		}
		// TODO: fix Materials equals contract
		// assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	public void testBatchStatusReportPluginDataTranslatorSpec() {
		String fileName = "batchStatusReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslatorWithReport())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), BatchStatusReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), BatchStatusReportPluginData.class)
				.build();

		BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("batch status report label");

		builder.setReportLabel(reportLabel);

		BatchStatusReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();

		BatchStatusReportPluginData actualPluginData = translatorController
				.getObject(BatchStatusReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);

	}

	@Test
	public void testMaterialsProducerPropertyReportPluginDataTranslatorSpec() {
		String fileName = "materialsProducerPropertyReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslatorWithReport())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsProducerPropertyReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName),
						MaterialsProducerPropertyReportPluginData.class)
				.build();

		MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();
		ReportLabel reportLabel = new SimpleReportLabel("materials producer property report report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerPropertyReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();

		MaterialsProducerPropertyReportPluginData actualPluginData = translatorController
				.getObject(MaterialsProducerPropertyReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	public void testMaterialsProducerResourceReportPluginDataTranslatorSpec() {
		String fileName = "materialsProducerResourceReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslatorWithReport())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsProducerResourceReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), MaterialsProducerResourceReportPluginData.class)
				.build();

		MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("materials producer resource report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerResourceReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);
		translatorController.readInput();

		MaterialsProducerResourceReportPluginData actualPluginData = translatorController
				.getObject(MaterialsProducerResourceReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	public void testStageReportPluginDataTranslatorSpec() {
		String fileName = "stageReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslatorWithReport())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), StageReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), StageReportPluginData.class)
				.build();

		StageReportPluginData.Builder builder = StageReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("stage report label");

		builder.setReportLabel(reportLabel);

		StageReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);
		translatorController.readInput();

		StageReportPluginData actualPluginData = translatorController.getObject(StageReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);

	}

}
