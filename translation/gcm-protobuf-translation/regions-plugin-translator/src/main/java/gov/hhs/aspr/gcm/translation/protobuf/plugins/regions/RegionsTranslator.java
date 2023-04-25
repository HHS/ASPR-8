package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionTransferReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.SimpleRegionIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.SimpleRegionPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.TestRegionIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.TestRegionPropertyIdTranslationSpec;
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
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new RegionsPluginDataTranslationSpec());
                    coreBuilder.addTranslatorSpec(new RegionIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new RegionPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new SimpleRegionIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new SimpleRegionPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestRegionIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestRegionPropertyIdTranslationSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new RegionPropertyReportPluginDataTranslationSpec());
                        coreBuilder.addTranslatorSpec(new RegionTransferReportPluginDataTranslationSpec());
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
