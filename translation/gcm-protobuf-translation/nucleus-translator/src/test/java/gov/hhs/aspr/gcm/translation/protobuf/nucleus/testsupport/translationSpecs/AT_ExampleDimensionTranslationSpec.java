package gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.example.input.ExampleDimensionInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.ExampleDimension;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ExampleDimensionTranslationSpec {

    @Test
    @UnitTestConstructor(target = ExampleDimensionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ExampleDimensionTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(NucleusTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        ExampleDimensionTranslationSpec translationSpec = new ExampleDimensionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ExampleDimension expectedAppValue = new ExampleDimension("test");

        ExampleDimensionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ExampleDimension actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ExampleDimensionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ExampleDimensionTranslationSpec translationSpec = new ExampleDimensionTranslationSpec();

        assertEquals(ExampleDimension.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ExampleDimensionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ExampleDimensionTranslationSpec translationSpec = new ExampleDimensionTranslationSpec();

        assertEquals(ExampleDimensionInput.class, translationSpec.getInputObjectClass());
    }
}
