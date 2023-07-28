package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.data.input.StochasticsPluginDataInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.Well;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_StochasticsPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = StochasticsPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new StochasticsPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(StochasticsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        StochasticsPluginDataTranslationSpec translationSpec = new StochasticsPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 524805676405822016L;

        StochasticsPluginData stochasticsPluginDataIn = StochasticsTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        StochasticsDataManager stochasticsDataManager = new StochasticsDataManager(stochasticsPluginDataIn);

        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsDataManager.getRandomNumberGeneratorIds()) {
            Well wellRNG = (Well) stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
            if (randomNumberGeneratorId == TestRandomGeneratorId.DASHER
                    || randomNumberGeneratorId == TestRandomGeneratorId.CUPID) {
                builder.addRNG(randomNumberGeneratorId,
                        WellState.builder().setSeed(wellRNG.getWellState().getSeed()).build());
            } else {
                builder.addRNG(randomNumberGeneratorId, wellRNG.getWellState());
            }
        }
        builder.setMainRNGState(((Well) stochasticsDataManager.getRandomGenerator()).getWellState());

        StochasticsPluginData expectedAppValue = builder.build();

        StochasticsPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        StochasticsPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = StochasticsPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        StochasticsPluginDataTranslationSpec translationSpec = new StochasticsPluginDataTranslationSpec();

        assertEquals(StochasticsPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = StochasticsPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        StochasticsPluginDataTranslationSpec translationSpec = new StochasticsPluginDataTranslationSpec();

        assertEquals(StochasticsPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
