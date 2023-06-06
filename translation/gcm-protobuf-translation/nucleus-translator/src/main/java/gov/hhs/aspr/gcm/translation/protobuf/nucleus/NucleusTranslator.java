package gov.hhs.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.translationSpecs.ExampleDimensionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.translationSpecs.ExamplePlanDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.DimensionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanQueueDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlannerTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.translationSpecs.SimulationStateTranslationSpec;

/**
 * Translator for Nucleus
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * the classes within Nucleus
 */
public class NucleusTranslator {

    private NucleusTranslator() {
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
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
                            .addTranslationSpec(new DimensionTranslationSpec())
                            .addTranslationSpec(new ExampleDimensionTranslationSpec())
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
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
