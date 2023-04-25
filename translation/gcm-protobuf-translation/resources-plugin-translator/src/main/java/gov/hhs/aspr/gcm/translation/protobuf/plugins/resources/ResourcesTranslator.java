package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.TestRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.PersonResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceInitializationTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcePropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcePropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.TestResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.TestResourcePropertyIdTranslatorSpec;

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
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new ResourcesPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new ResourceIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new ResourcePropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new ResourceInitializationTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestResourceIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestResourcePropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestRegionIdTranslatorSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new PersonResourceReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new ResourcePropertyReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new ResourceReportPluginDataTranslatorSpec());
                    }
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}
