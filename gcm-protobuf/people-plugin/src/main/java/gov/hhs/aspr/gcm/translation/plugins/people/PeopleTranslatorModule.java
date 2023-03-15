package gov.hhs.aspr.gcm.translation.plugins.people;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PersonIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;

public class PeopleTranslatorModule {

    private PeopleTranslatorModule() {
    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setPluginBundleId(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PeoplePluginDataTranslator());
                    translatorContext.addTranslator(new PersonIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static TranslatorModule getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static TranslatorModule getTranslatorModule() {
        return getBaseModule().build();
    }
}
