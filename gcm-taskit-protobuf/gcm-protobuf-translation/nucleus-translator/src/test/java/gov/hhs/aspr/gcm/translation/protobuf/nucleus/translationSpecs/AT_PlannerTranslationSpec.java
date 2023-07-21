package gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlannerInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import nucleus.Planner;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PlannerTranslationSpec {

    @Test
    @UnitTestConstructor(target = PlannerTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PlannerTranslationSpec());
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

        PlannerTranslationSpec translationSpec = new PlannerTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Planner expectedAppValue = Planner.DATA_MANAGER;

        PlannerInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        Planner actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PlannerTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PlannerTranslationSpec translationSpec = new PlannerTranslationSpec();

        assertEquals(Planner.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PlannerTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PlannerTranslationSpec translationSpec = new PlannerTranslationSpec();

        assertEquals(PlannerInput.class, translationSpec.getInputObjectClass());
    }
}
