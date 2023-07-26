package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.PersonResourceReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourceFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourceIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourceInitializationTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourcePropertyIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourcePropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourceReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.ResourcesPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.TestResourceIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs.TestResourcePropertyIdTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Resources Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * ResourcesPlugin
 */
public class ResourcesTranslator {

    private ResourcesTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new PersonResourceReportPluginDataTranslationSpec());
        list.add(new ResourceFilterTranslationSpec());
        list.add(new ResourceIdTranslationSpec());
        list.add(new ResourceInitializationTranslationSpec());
        list.add(new ResourcePropertyIdTranslationSpec());
        list.add(new ResourcePropertyReportPluginDataTranslationSpec());
        list.add(new ResourceReportPluginDataTranslationSpec());
        list.add(new ResourcesPluginDataTranslationSpec());
        list.add(new TestResourceIdTranslationSpec());
        list.add(new TestResourcePropertyIdTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
