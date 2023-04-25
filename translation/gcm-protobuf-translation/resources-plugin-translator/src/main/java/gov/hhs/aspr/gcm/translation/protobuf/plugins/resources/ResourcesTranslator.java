package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
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
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslatorSpec(new ResourcesPluginDataTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new ResourceIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new ResourcePropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new ResourceInitializationTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestResourceIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestResourcePropertyIdTranslationSpec());

                    if (withReport) {
                        translationEngineBuilder.addTranslatorSpec(new PersonResourceReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new ResourcePropertyReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new ResourceReportPluginDataTranslationSpec());
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
