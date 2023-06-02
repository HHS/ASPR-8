package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.PartitionsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.TestFilter;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.TestLabeler;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.Partition;
import plugins.partitions.support.filters.Filter;
import util.annotations.UnitTestForCoverage;

public class IT_PartitionsTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testGroupsTranslator() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder()
                        .addTranslationSpec(new TestFilterTranslationSpec())
                        .addTranslationSpec(new TestLabelerTranslationSpec()))
                .addTranslator(PartitionsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), PartitionsPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), PartitionsPluginData.class)
                .build();

        Filter partitionFilter = new TestFilter(0);
        Labeler partitionLabeler = new TestLabeler("Test");

        Partition partition = Partition.builder()
                .setFilter(partitionFilter)
                .addLabeler(partitionLabeler)
                .build();

        PartitionsPluginData expectedPluginData = PartitionsPluginData.builder()
                .addPartition("testPartition", partition)
                .build();

        translatorController.writeOutput(expectedPluginData);

        translatorController.readInput();

        PartitionsPluginData actualPluginData = translatorController.getFirstObject(PartitionsPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }
}