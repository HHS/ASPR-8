package gov.hhs.aspr.gcm.translation.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.TestBatchPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.TestMaterialIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.TestMaterialsProducerIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.TestMaterialsProducerPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsTranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.TestResourceIdTranslator;
import nucleus.PluginData;
import plugins.materials.MaterialsPluginData;
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
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public class App {
    
    private static void checkSame(MaterialsPluginData actualPluginData) {
        int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;
		long seed = 524805676405822016L;

		MaterialsPluginData materialsPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches,
				numStages, numBatchesInStage, seed);

		Set<TestMaterialId> expectedMaterialIds = EnumSet.allOf(TestMaterialId.class);

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

		System.out.println("Datas are the same.");
    }
    public static void main(String[] args) {
        String inputFileName = "./materials-plugin/src/main/resources/json/input.json";
        String outputFileName = "./materials-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(MaterialsTranslator.getTranslator(inputFileName, outputFileName))
                .addBundle(PropertiesTranslator.getTranslator())
                .addBundle(ResourcesTranslator.getTranslator())
                .addBundle(RegionsTranslatorModule.getTranslatorModule())
                .addBundle(PeopleTranslator.getTranslator())
                .addTranslatorSpec(new TestResourceIdTranslator())
                .addTranslatorSpec(new TestBatchPropertyIdTranslator())
                .addTranslatorSpec(new TestMaterialIdTranslator())
                .addTranslatorSpec(new TestMaterialsProducerIdTranslator())
                .addTranslatorSpec(new TestMaterialsProducerPropertyIdTranslator())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        MaterialsPluginData actualPluginData = (MaterialsPluginData) pluginDatas.get(0);

        // List<PersonId> people = new ArrayList<>();

        // for (int i = 0; i < 100; i++) {
        //     people.add(new PersonId(i));
        // }

        checkSame(actualPluginData);

        translatorController.writeOutput();
    }
    
}
