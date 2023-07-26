package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.testsupport.input.TestRandomGeneratorIdInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestRandomGeneratorIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestRandomGeneratorIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestRandomGeneratorIdTranslationSpec());
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

        TestRandomGeneratorIdTranslationSpec translationSpec = new TestRandomGeneratorIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestRandomGeneratorId expectedAppValue = TestRandomGeneratorId.BLITZEN;

        TestRandomGeneratorIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestRandomGeneratorId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestRandomGeneratorIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestRandomGeneratorIdTranslationSpec translationSpec = new TestRandomGeneratorIdTranslationSpec();

        assertEquals(TestRandomGeneratorId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestRandomGeneratorIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestRandomGeneratorIdTranslationSpec translationSpec = new TestRandomGeneratorIdTranslationSpec();

        assertEquals(TestRandomGeneratorIdInput.class, translationSpec.getInputObjectClass());
    }
}
