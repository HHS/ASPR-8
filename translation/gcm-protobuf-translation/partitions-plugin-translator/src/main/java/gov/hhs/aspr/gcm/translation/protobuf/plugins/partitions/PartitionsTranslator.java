package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import java.util.ArrayList;
import java.util.List;

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
import gov.hhs.aspr.translation.core.TranslationSpec;
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

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new AttributeIdTranslationSpec());
        list.add(new TestAttributeIdTranslationSpec());
        list.add(new AndFilterTranslationSpec());
        list.add(new FalseFilterTranslationSpec());
        list.add(new NotFilterTranslationSpec());
        list.add(new OrFilterTranslationSpec());
        list.add(new TrueFilterTranslationSpec());
        list.add(new AttributeFilterTranslationSpec());
        list.add(new FilterTranslationSpec());
        list.add(new LabelerTranslationSpec());
        list.add(new EqualityTranslationSpec());
        list.add(new PartitionTranslationSpec());
        list.add(new PartitionsPluginDataTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PartitionsTranslatorId.TRANSLATOR_ID)
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
