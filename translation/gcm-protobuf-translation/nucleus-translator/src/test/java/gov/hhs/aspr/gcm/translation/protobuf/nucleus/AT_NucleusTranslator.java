package gov.hhs.aspr.gcm.translation.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.simObjects.translationSpecs.ExamplePlanDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanQueueDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlannerTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.SimulationStateTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_NucleusTranslator {

    @Test
    @UnitTestMethod(target = NucleusTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {

        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new SimulationStateTranslationSpec())
                            .addTranslationSpec(new ExamplePlanDataTranslationSpec())
                            .addTranslationSpec(new PlanQueueDataTranslationSpec())
                            .addTranslationSpec(new PlannerTranslationSpec())
                            .addTranslationSpec(new PlanDataTranslationSpec());

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(
                                    SimulationStateInput.getDescriptor()
                                            .findFieldByName("startTime"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("time"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("plannerId"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("active"));
                }).build();

        assertEquals(expectedTranslator, NucleusTranslator.getTranslator());
    }
}
