package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanDataTranslationSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlanQueueDataTranslationSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs.PlannerTranslationSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs.SimulationStateTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;

public class NucleusTranslator {

    private NucleusTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new SimulationStateTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new PlanQueueDataTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new PlannerTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new PlanDataTranslationSpec());

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
