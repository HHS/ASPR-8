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

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.core.TranslatorController;
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
import nucleus.PluginData;
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
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public class AppTest {

	@Test
	public void testMaterialsTranslator() {

		Path basePath = Path.of("").toAbsolutePath();

		if (!basePath.endsWith("materials-plugin-translator")) {
			basePath = basePath.resolve("materials-plugin-translator");
		}

		Path inputFilePath = basePath.resolve("src/main/resources/json");
		Path outputFilePath = basePath.resolve("src/main/resources/json/output");

		outputFilePath.toFile().mkdir();

		String fileName = "pluginData.json";

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addReader(inputFilePath.resolve(fileName), MaterialsPluginDataInput.class)
				.addWriter(outputFilePath.resolve(fileName), MaterialsPluginData.class)

				.build();

		List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

		MaterialsPluginData materialsPluginData = (MaterialsPluginData) pluginDatas.get(0);

		int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;
		long seed = 524805676405822016L;

		Set<TestMaterialId> expectedMaterialIds = EnumSet.allOf(TestMaterialId.class);
		assertFalse(expectedMaterialIds.isEmpty());

		Set<MaterialId> actualMaterialIds = materialsPluginData.getMaterialIds();
		assertEquals(expectedMaterialIds, actualMaterialIds);

		for (TestMaterialId expectedMaterialId : expectedMaterialIds) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId
					.getTestBatchPropertyIds(expectedMaterialId);
			assertFalse(expectedBatchPropertyIds.isEmpty());
			Set<BatchPropertyId> actualBatchPropertyIds = materialsPluginData.getBatchPropertyIds(expectedMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			for (TestBatchPropertyId batchPropertyId : expectedBatchPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = batchPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsPluginData.getBatchPropertyDefinition(
						expectedMaterialId,
						batchPropertyId);
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
				for (TestBatchPropertyId expectedBatchPropertyId : TestBatchPropertyId
						.getTestBatchPropertyIds(expectedMaterialId)) {
					if (expectedBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
							|| randomGenerator.nextBoolean()) {
						Object expectedPropertyValue = expectedBatchPropertyId.getRandomPropertyValue(randomGenerator);

						Map<BatchPropertyId, Object> propertyValueMap = materialsPluginData
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
			assertTrue(materialsPluginData.getStageIds().containsAll(stages));

			for (int i = 0; i < stages.size(); i++) {
				MaterialsProducerId actualMaterialsProducerId = materialsPluginData
						.getStageMaterialsProducer(stages.get(i));
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

		Set<TestMaterialsProducerPropertyId> expectedMaterialsProducerPropertyIds = EnumSet
				.allOf(TestMaterialsProducerPropertyId.class);
		assertFalse(expectedMaterialsProducerPropertyIds.isEmpty());

		Set<MaterialsProducerPropertyId> actualMaterialProducerPropertyIds = materialsPluginData
				.getMaterialsProducerPropertyIds();
		assertEquals(expectedMaterialsProducerPropertyIds, actualMaterialProducerPropertyIds);

		for (TestMaterialsProducerPropertyId expectedMaterialsProducerPropertyId : expectedMaterialsProducerPropertyIds) {
			PropertyDefinition expectedPropertyDefinition = expectedMaterialsProducerPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = materialsPluginData
					.getMaterialsProducerPropertyDefinition(expectedMaterialsProducerPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			if (expectedPropertyDefinition.getDefaultValue().isEmpty()) {
				for (TestMaterialsProducerId producerId : expectedMaterialsProducerIds) {
					Object expectedPropertyValue = expectedMaterialsProducerPropertyId
							.getRandomPropertyValue(randomGenerator);
					Map<MaterialsProducerPropertyId, Object> propertyValueMap = materialsPluginData
							.getMaterialsProducerPropertyValues(producerId);
					assertTrue(propertyValueMap.containsKey(expectedMaterialsProducerPropertyId));
					Object actualPropertyValue = propertyValueMap.get(expectedMaterialsProducerPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
				}
			}
		}

		translatorController.writeOutput();
	}

	@Test
	public void testBatchStatusReportPluginDataTranslatorSpec() {
		Path basePath = Path.of("").toAbsolutePath();

		if (!basePath.endsWith("materials-plugin-translator")) {
			basePath = basePath.resolve("materials-plugin-translator");
		}

		Path inputFilePath = basePath.resolve("src/main/resources/json");
		Path outputFilePath = basePath.resolve("src/main/resources/json/output");

		outputFilePath.toFile().mkdir();

		String fileName = "batchStatusReport.json";

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.builder(true).build())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addReader(inputFilePath.resolve(fileName), BatchStatusReportPluginDataInput.class)
				.addWriter(outputFilePath.resolve(fileName), BatchStatusReportPluginData.class)
				.build();

		List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

		BatchStatusReportPluginData actualPluginData = (BatchStatusReportPluginData) pluginDatas.get(0);

		BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("batch status report label");

		builder.setReportLabel(reportLabel);

		BatchStatusReportPluginData expectedPluginData = builder.build();

		assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());

		translatorController.writeOutput();
	}

	@Test
	public void testMaterialsProducerPropertyReportPluginDataTranslatorSpec() {
		Path basePath = Path.of("").toAbsolutePath();

		if (!basePath.endsWith("materials-plugin-translator")) {
			basePath = basePath.resolve("materials-plugin-translator");
		}

		Path inputFilePath = basePath.resolve("src/main/resources/json");
		Path outputFilePath = basePath.resolve("src/main/resources/json/output");

		outputFilePath.toFile().mkdir();

		String fileName = "materialsProducerPropertyReport.json";

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.builder(true).build())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addReader(inputFilePath.resolve(fileName), MaterialsProducerPropertyReportPluginDataInput.class)
				.addWriter(outputFilePath.resolve(fileName),
						MaterialsProducerPropertyReportPluginData.class)
				.build();

		List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

		MaterialsProducerPropertyReportPluginData actualPluginData = (MaterialsProducerPropertyReportPluginData) pluginDatas
				.get(0);
		MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("materials producer property report report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerPropertyReportPluginData expectedPluginData = builder.build();

		assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());

		translatorController.writeOutput();
	}

	@Test
	public void testMaterialsProducerResourceReportPluginDataTranslatorSpec() {
		Path basePath = Path.of("").toAbsolutePath();

		if (!basePath.endsWith("materials-plugin-translator")) {
			basePath = basePath.resolve("materials-plugin-translator");
		}

		Path inputFilePath = basePath.resolve("src/main/resources/json");
		Path outputFilePath = basePath.resolve("src/main/resources/json/output");

		outputFilePath.toFile().mkdir();

		String fileName = "materialsProducerResourceReport.json";

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.builder(true).build())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addReader(inputFilePath.resolve(fileName), MaterialsProducerResourceReportPluginDataInput.class)
				.addWriter(outputFilePath.resolve(fileName), MaterialsProducerResourceReportPluginData.class)
				.build();

		List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

		MaterialsProducerResourceReportPluginData actualPluginData = (MaterialsProducerResourceReportPluginData) pluginDatas
				.get(0);
		MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("materials producer resource report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerResourceReportPluginData expectedPluginData = builder.build();

		assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());

		translatorController.writeOutput();
	}

	@Test
	public void testStageReportPluginDataTranslatorSpec() {
		Path basePath = Path.of("").toAbsolutePath();

		if (!basePath.endsWith("materials-plugin-translator")) {
			basePath = basePath.resolve("materials-plugin-translator");
		}

		Path inputFilePath = basePath.resolve("src/main/resources/json");
		Path outputFilePath = basePath.resolve("src/main/resources/json/output");

		outputFilePath.toFile().mkdir();

		String fileName = "stageReport.json";

		TranslatorController translatorController = TranslatorController.builder()
				.setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
				.addTranslator(MaterialsTranslator.builder(true).build())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addReader(inputFilePath.resolve(fileName), StageReportPluginDataInput.class)
				.addWriter(outputFilePath.resolve(fileName), StageReportPluginData.class)
				.build();

		List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

		StageReportPluginData actualPluginData = (StageReportPluginData) pluginDatas.get(0);

		StageReportPluginData.Builder builder = StageReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("stage report label");

		builder.setReportLabel(reportLabel);

		StageReportPluginData expectedPluginData = builder.build();

		assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());

		translatorController.writeOutput();
	}

}
