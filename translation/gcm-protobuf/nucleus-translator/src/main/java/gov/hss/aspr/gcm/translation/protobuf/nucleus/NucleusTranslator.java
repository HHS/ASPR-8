package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs.PlanDataTranslatorSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs.PlanQueueDataTranslatorSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs.PlannerTranslatorSpec;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs.SimulationStateTranslatorSpec;

public class NucleusTranslator {

    private NucleusTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new SimulationStateTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlanQueueDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlannerTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlanDataTranslatorSpec());

                    translatorContext.getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class)
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
