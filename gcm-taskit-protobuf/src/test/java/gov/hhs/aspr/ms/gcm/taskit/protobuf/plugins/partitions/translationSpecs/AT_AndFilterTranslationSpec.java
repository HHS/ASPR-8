package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.AndFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestFilter;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.AndFilter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_AndFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = AndFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new AndFilterTranslationSpec());
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

        AndFilterTranslationSpec translationSpec = new AndFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Filter filterA = new TestFilter(0);
        Filter filterB = new TestFilter(1);

        AndFilter expectedAppValue = new AndFilter(filterA, filterB);

        AndFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        AndFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = AndFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        AndFilterTranslationSpec translationSpec = new AndFilterTranslationSpec();

        assertEquals(AndFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = AndFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        AndFilterTranslationSpec translationSpec = new AndFilterTranslationSpec();

        assertEquals(AndFilterInput.class, translationSpec.getInputObjectClass());
    }
}
