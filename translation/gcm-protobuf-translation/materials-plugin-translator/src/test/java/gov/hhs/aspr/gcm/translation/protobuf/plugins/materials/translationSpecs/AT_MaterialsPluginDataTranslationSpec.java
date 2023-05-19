package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.materials.MaterialsPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_MaterialsPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = MaterialsPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new MaterialsPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(MaterialsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        MaterialsPluginDataTranslationSpec translationSpec = new MaterialsPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        int numBatches = 50;
        int numStages = 10;
        int numBatchesInStage = 30;
        long seed = 524805676405822016L;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

        int bId = 0;
        int sId = 0;
        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            builder.addMaterial(testMaterialId);
        }

        for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

            List<BatchId> batches = new ArrayList<>();

            for (int i = 0; i < numBatches; i++) {

                TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
                double amount = randomGenerator.nextDouble();
                BatchId batchId = new BatchId(bId++);
                builder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
                batches.add(batchId);
                for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
                        .getTestBatchPropertyIds(testMaterialId)) {
                    boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
                    if (required || randomGenerator.nextBoolean()) {
                        builder.setBatchPropertyValue(batchId, testBatchPropertyId,
                                testBatchPropertyId.getRandomPropertyValue(randomGenerator));
                    }
                }

            }

            List<StageId> stages = new ArrayList<>();

            for (int i = 0; i < numStages; i++) {
                StageId stageId = new StageId(sId++);
                stages.add(stageId);
                boolean offered = i % 2 == 0;
                builder.addStage(stageId, offered, testMaterialsProducerId);
            }

            Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
            for (int i = 0; i < numBatchesInStage; i++) {
                BatchId batchId = batches.get(i);
                StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
                builder.addBatchToStage(stageId, batchId);
            }
            builder.addMaterialsProducerId(testMaterialsProducerId);

            for (ResourceId resourceId : TestResourceId.values()) {
                if (randomGenerator.nextBoolean()) {
                    builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId,
                            randomGenerator.nextInt(10));
                }
            }

        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .values()) {
            builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
                    testMaterialsProducerPropertyId.getPropertyDefinition());
        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .getPropertiesWithoutDefaultValues()) {
            for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
                Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
                builder.setMaterialsProducerPropertyValue(testMaterialsProducerId,
                        testMaterialsProducerPropertyId, randomPropertyValue);
            }
        }

        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
            for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
                builder.defineBatchProperty(testMaterialId, testBatchPropertyId,
                        testBatchPropertyId.getPropertyDefinition());
            }
        }

        MaterialsPluginData expectedAppValue = builder.build();

        MaterialsPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        MaterialsPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = MaterialsPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        MaterialsPluginDataTranslationSpec translationSpec = new MaterialsPluginDataTranslationSpec();

        assertEquals(MaterialsPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = MaterialsPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        MaterialsPluginDataTranslationSpec translationSpec = new MaterialsPluginDataTranslationSpec();

        assertEquals(MaterialsPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
