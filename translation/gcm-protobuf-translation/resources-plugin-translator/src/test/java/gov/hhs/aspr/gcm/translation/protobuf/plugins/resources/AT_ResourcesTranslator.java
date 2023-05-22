package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.PersonResourceReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceInitializationTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcePropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcePropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourceReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.ResourcesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.TestResourceIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs.TestResourcePropertyIdTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_ResourcesTranslator {

    @Test
    @UnitTestMethod(target = ResourcesTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new ResourcesPluginDataTranslationSpec())
                            .addTranslationSpec(new ResourceIdTranslationSpec())
                            .addTranslationSpec(new ResourcePropertyIdTranslationSpec())
                            .addTranslationSpec(new ResourceInitializationTranslationSpec())
                            .addTranslationSpec(new TestResourceIdTranslationSpec())
                            .addTranslationSpec(new TestResourcePropertyIdTranslationSpec());

                    translatorContext.addParentChildClassRelationship(SimpleReportLabel.class, ReportLabel.class);
                }).build();

        assertEquals(expectedTranslator, ResourcesTranslator.getTranslator());
    }

    @Test
    @UnitTestMethod(target = ResourcesTranslator.class, name = "getTranslatorWithReport", args = {})
    public void testGetTranslatorWithReport() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new ResourcesPluginDataTranslationSpec())
                            .addTranslationSpec(new ResourceIdTranslationSpec())
                            .addTranslationSpec(new ResourcePropertyIdTranslationSpec())
                            .addTranslationSpec(new ResourceInitializationTranslationSpec())
                            .addTranslationSpec(new TestResourceIdTranslationSpec())
                            .addTranslationSpec(new TestResourcePropertyIdTranslationSpec())
                            .addTranslationSpec(new PersonResourceReportPluginDataTranslationSpec())
                            .addTranslationSpec(new ResourcePropertyReportPluginDataTranslationSpec())
                            .addTranslationSpec(new ResourceReportPluginDataTranslationSpec());

                    translatorContext.addParentChildClassRelationship(SimpleReportLabel.class, ReportLabel.class);
                }).build();

        assertEquals(expectedTranslator, ResourcesTranslator.getTranslatorWithReport());
    }
}
