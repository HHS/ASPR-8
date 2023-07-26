package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.DimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.ExampleDimension;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import nucleus.Dimension;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_DimensionTranslationSpec {

    @Test
    @UnitTestConstructor(target = DimensionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new DimensionTranslationSpec());
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

        DimensionTranslationSpec translationSpec = new DimensionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Dimension expectedAppValue = new ExampleDimension("test");

        DimensionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        Dimension actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = DimensionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        DimensionTranslationSpec translationSpec = new DimensionTranslationSpec();

        assertEquals(Dimension.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = DimensionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        DimensionTranslationSpec translationSpec = new DimensionTranslationSpec();

        assertEquals(DimensionInput.class, translationSpec.getInputObjectClass());
    }
}
