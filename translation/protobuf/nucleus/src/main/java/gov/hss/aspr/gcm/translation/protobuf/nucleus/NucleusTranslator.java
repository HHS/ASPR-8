package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationTimeInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs.SimulationTimeTranslatorSpec;

public class NucleusTranslator {
    private NucleusTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new SimulationTimeTranslatorSpec());

                    translatorContext
                            .addFieldToIncludeDefaultValue(
                                    SimulationTimeInput.getDescriptor().findFieldByName("startTime"));
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false);

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
