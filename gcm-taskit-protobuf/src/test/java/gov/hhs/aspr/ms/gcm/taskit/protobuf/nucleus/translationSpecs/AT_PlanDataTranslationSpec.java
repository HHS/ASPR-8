package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.ExamplePlanData;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import nucleus.PlanData;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PlanDataTranslationSpec {
    
    @Test
    @UnitTestConstructor(target = PlanDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PlanDataTranslationSpec());
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

        PlanDataTranslationSpec translationSpec = new PlanDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PlanData expectedAppValue = new ExamplePlanData(15);

        PlanDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PlanData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PlanDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PlanDataTranslationSpec translationSpec = new PlanDataTranslationSpec();

        assertEquals(PlanData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PlanDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PlanDataTranslationSpec translationSpec = new PlanDataTranslationSpec();

        assertEquals(PlanDataInput.class, translationSpec.getInputObjectClass());
    }
}
