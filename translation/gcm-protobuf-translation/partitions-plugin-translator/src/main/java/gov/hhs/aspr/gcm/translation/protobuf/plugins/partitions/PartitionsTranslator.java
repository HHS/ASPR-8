package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.AndFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.AttributeFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.AttributeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.EqualityTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FalseFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.LabelerTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.NotFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.OrFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.TestAttributeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.TrueFilterTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Partitions Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PartitionsPluginData
 */
public class PartitionsTranslator {

    private PartitionsTranslator() {
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PartitionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new AttributeIdTranslationSpec())
                            .addTranslationSpec(new TestAttributeIdTranslationSpec())
                            .addTranslationSpec(new AndFilterTranslationSpec())
                            .addTranslationSpec(new FalseFilterTranslationSpec())
                            .addTranslationSpec(new NotFilterTranslationSpec())
                            .addTranslationSpec(new OrFilterTranslationSpec())
                            .addTranslationSpec(new TrueFilterTranslationSpec())
                            .addTranslationSpec(new AttributeFilterTranslationSpec())
                            .addTranslationSpec(new FilterTranslationSpec())
                            .addTranslationSpec(new LabelerTranslationSpec())
                            .addTranslationSpec(new EqualityTranslationSpec())
                            .addTranslationSpec(new PartitionTranslationSpec())
                            .addTranslationSpec(new PartitionsPluginDataTranslationSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
