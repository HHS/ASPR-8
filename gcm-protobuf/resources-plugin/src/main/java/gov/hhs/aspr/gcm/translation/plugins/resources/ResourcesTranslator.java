package gov.hhs.aspr.gcm.translation.plugins.resources;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourceIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourceInitializationTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourcePropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourcesPluginDataTranslator;

public class ResourcesTranslator {
    private ResourcesTranslator() {

    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorModuleId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new ResourcesPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new ResourceIdTranslator());
                    translatorContext.addTranslatorSpec(new ResourcePropertyIdTranslator());
                    translatorContext.addTranslatorSpec(new ResourceInitializationTranslator());
                })
                .setInputObjectType(ResourcesPluginDataInput.getDefaultInstance());
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
