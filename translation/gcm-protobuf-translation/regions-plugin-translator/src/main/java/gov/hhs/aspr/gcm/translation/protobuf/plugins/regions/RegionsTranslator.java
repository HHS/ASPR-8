package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionMembershipInput.RegionPersonInfo;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs.RegionPropertyDimensionTranslationSpec;
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

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new RegionFilterTranslationSpec());
        list.add(new RegionIdTranslationSpec());
        list.add(new RegionPropertyDimensionTranslationSpec());
        list.add(new RegionPropertyIdTranslationSpec());
        list.add(new RegionPropertyReportPluginDataTranslationSpec());
        list.add(new RegionsPluginDataTranslationSpec());
        list.add(new RegionTransferReportPluginDataTranslationSpec());
        list.add(new SimpleRegionIdTranslationSpec());
        list.add(new SimpleRegionPropertyIdTranslationSpec());
        list.add(new TestRegionIdTranslationSpec());
        list.add(new TestRegionPropertyIdTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                    translationEngineBuilder.addFieldToIncludeDefaultValue(
                            RegionPersonInfo.getDescriptor().findFieldByName("personId"));

                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
