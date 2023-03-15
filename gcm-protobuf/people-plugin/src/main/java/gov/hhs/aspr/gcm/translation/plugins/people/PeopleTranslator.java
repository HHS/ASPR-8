package gov.hhs.aspr.gcm.translation.plugins.people;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.plugins.people.translatorSpecs.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.translatorSpecs.PersonIdTranslator;

public class PeopleTranslator {

    private PeopleTranslator() {
    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setPluginBundleId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PeoplePluginDataTranslator());
                    translatorContext.addTranslatorSpec(new PersonIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslator()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslator().build();
    }
}
