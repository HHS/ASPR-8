package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.AndFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.EqualityTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FalseFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.FilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.LabelerTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.NotFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.OrFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.PartitionsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs.TrueFilterTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_PartitionsTranslator {

    @Test
    @UnitTestMethod(target = PartitionsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {

        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(PartitionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new AndFilterTranslationSpec())
                            .addTranslationSpec(new FalseFilterTranslationSpec())
                            .addTranslationSpec(new NotFilterTranslationSpec())
                            .addTranslationSpec(new OrFilterTranslationSpec())
                            .addTranslationSpec(new TrueFilterTranslationSpec())
                            .addTranslationSpec(new FilterTranslationSpec())
                            .addTranslationSpec(new LabelerTranslationSpec())
                            .addTranslationSpec(new EqualityTranslationSpec())
                            .addTranslationSpec(new PartitionTranslationSpec())
                            .addTranslationSpec(new PartitionsPluginDataTranslationSpec());
                }).build();

        assertEquals(expectedTranslator, PartitionsTranslator.getTranslator());
    }
}
