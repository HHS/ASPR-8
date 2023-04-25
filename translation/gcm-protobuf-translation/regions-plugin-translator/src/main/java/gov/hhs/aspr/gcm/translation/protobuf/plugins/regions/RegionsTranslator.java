package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.RegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.RegionPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.RegionPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.RegionTransferReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.RegionsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.SimpleRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.SimpleRegionPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.TestRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.TestRegionPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;

public class RegionsTranslator {

    private RegionsTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new RegionsPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new RegionIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new RegionPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new SimpleRegionIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new SimpleRegionPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestRegionIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestRegionPropertyIdTranslatorSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new RegionPropertyReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new RegionTransferReportPluginDataTranslatorSpec());
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
