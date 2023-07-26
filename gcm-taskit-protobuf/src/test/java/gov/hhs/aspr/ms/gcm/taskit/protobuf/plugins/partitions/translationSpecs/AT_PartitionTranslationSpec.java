package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestFilter;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestLabeler;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.Partition;
import plugins.partitions.support.filters.Filter;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PartitionTranslationSpec {

    @Test
    @UnitTestConstructor(target = PartitionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PartitionTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder()
                        .addTranslationSpec(new TestFilterTranslationSpec())
                        .addTranslationSpec(new TestLabelerTranslationSpec()))
                .addTranslator(PartitionsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PartitionTranslationSpec translationSpec = new PartitionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Filter partitionFilter = new TestFilter(0);
        Labeler partitionLabeler = new TestLabeler("Test");

        Partition expectedAppValue = Partition.builder()
                .setFilter(partitionFilter)
                .addLabeler(partitionLabeler)
                .build();

        PartitionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        Partition actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        expectedAppValue = Partition.builder()
                .addLabeler(partitionLabeler)
                .build();

        inputValue = translationSpec.convertAppObject(expectedAppValue);

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PartitionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PartitionTranslationSpec translationSpec = new PartitionTranslationSpec();

        assertEquals(Partition.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PartitionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PartitionTranslationSpec translationSpec = new PartitionTranslationSpec();

        assertEquals(PartitionInput.class, translationSpec.getInputObjectClass());
    }
}
