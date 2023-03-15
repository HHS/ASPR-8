package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs.PersonPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs.PersonPropertyIdTranslator;

public class PersonPropertiesTranslator {
    private PersonPropertiesTranslator() {
    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setPluginBundleId(PersonPropertiesTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInputObjectType(PersonPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PersonPropertiesPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new PersonPropertyIdTranslator());
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
