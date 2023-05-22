package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.TestGlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_GlobalPropertiesTranslator {

    @Test
    @UnitTestMethod(target = GlobalPropertiesTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(
                                    new GlobalPropertiesPluginDataTranslationSpec())
                            .addTranslationSpec(new GlobalPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestGlobalPropertyIdTranslationSpec());

                }).build();

        assertEquals(expectedTranslator, GlobalPropertiesTranslator.getTranslator());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTranslator.class, name = "getTranslatorWithReport", args = {})
    public void testGetTranslatorWithReport() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(
                                    new GlobalPropertiesPluginDataTranslationSpec())
                            .addTranslationSpec(new GlobalPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestGlobalPropertyIdTranslationSpec())
                            .addTranslationSpec(
                                    new GlobalPropertyReportPluginDataTranslationSpec());

                }).build();

        assertEquals(expectedTranslator, GlobalPropertiesTranslator.getTranslatorWithReport());
    }
}
