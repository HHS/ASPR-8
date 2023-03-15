package gov.hhs.aspr.gcm.translation.plugins.people;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PersonIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;

public class PeopleTranslatorModule {

    private PeopleTranslatorModule() {
    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PeoplePluginDataTranslator());
                    translatorContext.addTranslator(new PersonIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslatorModule() {
        return getBaseModule().build();
    }
}
