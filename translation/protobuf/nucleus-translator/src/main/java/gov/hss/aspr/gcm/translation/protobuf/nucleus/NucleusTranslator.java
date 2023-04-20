package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.core.ProtobufTranslatorCore;
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
        return Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new SimulationStateTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlanQueueDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlannerTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PlanDataTranslatorSpec());

                    ((ProtobufTranslatorCore.Builder) translatorContext.getTranslatorCoreBuilder())
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

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}