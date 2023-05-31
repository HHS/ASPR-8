package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import java.util.function.Consumer;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.AndFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FalseFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.LabelerTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.NotFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.OrFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.TrueFilterTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.TranslatorContext;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Partitions Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PartitionsPluginData
 */
public class PartitionsTranslator {

    private static Consumer<TranslatorContext> baseInitializer = (translatorContext) -> {
        ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

        translationEngineBuilder
                .addTranslationSpec(new AndFilterTranslationSpec())
                .addTranslationSpec(new FalseFilterTranslationSpec())
                .addTranslationSpec(new NotFilterTranslationSpec())
                .addTranslationSpec(new OrFilterTranslationSpec())
                .addTranslationSpec(new TrueFilterTranslationSpec())
                .addTranslationSpec(new FilterTranslationSpec())
                .addTranslationSpec(new LabelerTranslationSpec())
                .addTranslationSpec(new PartitionTranslationSpec())
                .addTranslationSpec(new PartitionsPluginDataTranslationSpec());
    };

    private static Translator.Builder baseBuilder = Translator.builder()
            .setTranslatorId(PartitionsTranslatorId.TRANSLATOR_ID);

    private PartitionsTranslator() {
    }

    public static Translator getTranslator() {
        return baseBuilder.setInitializer(baseInitializer).build();
    }
}
