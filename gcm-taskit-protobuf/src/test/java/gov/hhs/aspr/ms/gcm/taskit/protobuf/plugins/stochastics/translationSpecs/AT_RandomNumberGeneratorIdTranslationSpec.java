package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.support.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_RandomNumberGeneratorIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = RandomNumberGeneratorIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new RandomNumberGeneratorIdTranslationSpec());
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

        RandomNumberGeneratorIdTranslationSpec translationSpec = new RandomNumberGeneratorIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RandomNumberGeneratorId expectedAppValue = TestRandomGeneratorId.BLITZEN;

        RandomNumberGeneratorIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        RandomNumberGeneratorId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = RandomNumberGeneratorIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        RandomNumberGeneratorIdTranslationSpec translationSpec = new RandomNumberGeneratorIdTranslationSpec();

        assertEquals(RandomNumberGeneratorId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = RandomNumberGeneratorIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        RandomNumberGeneratorIdTranslationSpec translationSpec = new RandomNumberGeneratorIdTranslationSpec();

        assertEquals(RandomNumberGeneratorIdInput.class, translationSpec.getInputObjectClass());
    }
}
