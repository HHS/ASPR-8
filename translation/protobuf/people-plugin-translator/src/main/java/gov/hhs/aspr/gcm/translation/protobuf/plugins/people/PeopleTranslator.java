package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PeoplePluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PersonIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PersonRangeTranslatorSpec;
import plugins.people.PeoplePluginData;

public class PeopleTranslator {

    private PeopleTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PeoplePluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonRangeTranslatorSpec());

                    translatorContext
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, PeoplePluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, PeoplePluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, PeoplePluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, PeoplePluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
