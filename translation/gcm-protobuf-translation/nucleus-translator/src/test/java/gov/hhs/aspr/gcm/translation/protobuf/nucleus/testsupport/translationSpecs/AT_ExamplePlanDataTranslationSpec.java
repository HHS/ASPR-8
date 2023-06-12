package gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.example.input.ExamplePlanDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.ExamplePlanData;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ExamplePlanDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = ExamplePlanDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ExamplePlanDataTranslationSpec());
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

        ExamplePlanDataTranslationSpec translationSpec = new ExamplePlanDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ExamplePlanData expectedAppValue = new ExamplePlanData(15);

        ExamplePlanDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ExamplePlanData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ExamplePlanDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ExamplePlanDataTranslationSpec translationSpec = new ExamplePlanDataTranslationSpec();

        assertEquals(ExamplePlanData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ExamplePlanDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ExamplePlanDataTranslationSpec translationSpec = new ExamplePlanDataTranslationSpec();

        assertEquals(ExamplePlanDataInput.class, translationSpec.getInputObjectClass());
    }
}
