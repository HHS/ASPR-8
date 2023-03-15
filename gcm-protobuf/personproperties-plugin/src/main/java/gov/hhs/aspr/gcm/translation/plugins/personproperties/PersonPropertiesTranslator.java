package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import plugins.personproperties.PersonPropertiesPluginData;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs.PersonPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs.PersonPropertyIdTranslatorSpec;

public class PersonPropertiesTranslator {
    private PersonPropertiesTranslator() {
    }

    public static Translator.Builder getBaseTranslatorBuilder() {
        return Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PersonPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonPropertyIdTranslatorSpec());
                });

    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslatorBuilder()
                .addInputFile(inputFileName, PersonPropertiesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, PersonPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslatorBuilder().build();
    }
}
