package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
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
                    ProtobufTranslatorCore.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class);

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
