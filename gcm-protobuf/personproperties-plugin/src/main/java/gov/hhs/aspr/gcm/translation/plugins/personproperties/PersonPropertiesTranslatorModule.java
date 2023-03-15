package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertiesPluginDataInput;

public class PersonPropertiesTranslatorModule {
    private PersonPropertiesTranslatorModule() {
    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setPluginBundleId(PersonPropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(PersonPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PersonPropertiesPluginDataTranslator());
                    translatorContext.addTranslator(new PersonPropertyIdTranslator());
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
