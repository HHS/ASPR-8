package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertiesPluginDataInput;

public class PersonPropertiesTranslatorModule {
    private PersonPropertiesTranslatorModule() {
    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(PersonPropertiesTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_ID)
                .setInputObjectType(PersonPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PersonPropertiesPluginDataTranslator());
                    translatorContext.addTranslator(new PersonPropertyIdTranslator());
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
