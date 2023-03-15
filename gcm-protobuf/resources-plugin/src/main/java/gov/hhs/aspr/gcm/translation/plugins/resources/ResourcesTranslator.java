package gov.hhs.aspr.gcm.translation.plugins.resources;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourceInitializationTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourcePropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs.ResourcesPluginDataTranslatorSpec;
import plugins.resources.ResourcesPluginData;

public class ResourcesTranslator {
    private ResourcesTranslator() {

    }

    private static Translator.Builder getBaseTranslatorBuilder() {
        return Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new ResourcesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourceIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourcePropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourceInitializationTranslatorSpec());
                });
    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslatorBuilder()
                .addInputFile(inputFileName, ResourcesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, ResourcesPluginData.class)

                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslatorBuilder().build();
    }
}
