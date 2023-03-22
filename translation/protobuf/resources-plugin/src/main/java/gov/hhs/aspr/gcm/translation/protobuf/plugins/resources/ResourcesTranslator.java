package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourceInitializationTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourcePropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourcesPluginDataTranslatorSpec;
import plugins.resources.ResourcesPluginData;

public class ResourcesTranslator {
    private ResourcesTranslator() {

    }

    public static Translator.Builder builder() {
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

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, ResourcesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, ResourcesPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, ResourcesPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, ResourcesPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
