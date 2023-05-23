package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionMembershipInput.RegionPersonInfo;
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

/**
 * Translator for the Regions Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * RegionsPlugin
 */
public class RegionsTranslator {

    private RegionsTranslator() {
    }

    private static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new RegionsPluginDataTranslationSpec())
                            .addTranslationSpec(new RegionIdTranslationSpec())
                            .addTranslationSpec(new RegionPropertyIdTranslationSpec())
                            .addTranslationSpec(new SimpleRegionIdTranslationSpec())
                            .addTranslationSpec(new SimpleRegionPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestRegionIdTranslationSpec())
                            .addTranslationSpec(new TestRegionPropertyIdTranslationSpec());

                    translationEngineBuilder.addFieldToIncludeDefaultValue(
                            RegionPersonInfo.getDescriptor().findFieldByName("personId"));

                    if (withReport) {
                        translationEngineBuilder
                                .addTranslationSpec(new RegionPropertyReportPluginDataTranslationSpec())
                                .addTranslationSpec(new RegionTransferReportPluginDataTranslationSpec());
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
