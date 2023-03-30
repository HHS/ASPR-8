package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyInteractionReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import plugins.personproperties.PersonPropertiesPluginData;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;

public class PersonPropertiesTranslator {
    private PersonPropertiesTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        return Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PersonPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestPersonPropertyIdTranslatorSpec());

                    if (withReport) {
                        translatorContext.addTranslatorSpec(new PersonPropertyReportPluginDataTranslatorSpec());
                        translatorContext
                                .addTranslatorSpec(new PersonPropertyInteractionReportPluginDataTranslatorSpec());
                    }
                });

    }

    public static Translator.Builder builder() {
        return builder(false);
    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, PersonPropertiesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, PersonPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, PersonPropertiesPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, PersonPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
