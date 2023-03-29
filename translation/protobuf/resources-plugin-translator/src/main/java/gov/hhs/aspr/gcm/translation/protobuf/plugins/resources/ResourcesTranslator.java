package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.TestRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.PersonResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourceInitializationTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourcePropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourcePropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.ResourcesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.TestResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.TestResourcePropertyIdTranslatorSpec;
import plugins.resources.ResourcesPluginData;

public class ResourcesTranslator {
    private ResourcesTranslator() {

    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new ResourcesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourceIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourcePropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ResourceInitializationTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestResourceIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestResourcePropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestRegionIdTranslatorSpec());

                    if (withReport) {
                        translatorContext.addTranslatorSpec(new PersonResourceReportPluginDataTranslatorSpec());
                        translatorContext.addTranslatorSpec(new ResourcePropertyReportPluginDataTranslatorSpec());
                        translatorContext.addTranslatorSpec(new ResourceReportPluginDataTranslatorSpec());
                    }
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }
        return builder;
    }

    public static Translator.Builder builder() {
        return builder(false);
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
