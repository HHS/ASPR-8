package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_RegionsTranslator {

    @Test
    @UnitTestMethod(target = RegionsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
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
                }).build();

        assertEquals(expectedTranslator, RegionsTranslator.getTranslator());
    }

    @Test
    @UnitTestMethod(target = RegionsTranslator.class, name = "getTranslatorWithReport", args = {})
    public void testGetTranslatorWithReport() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
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
                            .addTranslationSpec(new RegionPropertyReportPluginDataTranslationSpec())
                            .addTranslationSpec(new RegionTransferReportPluginDataTranslationSpec())
                            .addTranslationSpec(new TestRegionPropertyIdTranslationSpec());
                }).build();

        assertEquals(expectedTranslator, RegionsTranslator.getTranslatorWithReport());
    }
}
