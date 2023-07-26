package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.OrFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestFilter;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.support.filters.OrFilter;
import plugins.partitions.support.filters.Filter;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_OrFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = OrFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new OrFilterTranslationSpec());
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

        OrFilterTranslationSpec translationSpec = new OrFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Filter filterA = new TestFilter(0);
        Filter filterB = new TestFilter(1);

        OrFilter expectedAppValue = new OrFilter(filterA, filterB);

        OrFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        OrFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = OrFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        OrFilterTranslationSpec translationSpec = new OrFilterTranslationSpec();

        assertEquals(OrFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = OrFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        OrFilterTranslationSpec translationSpec = new OrFilterTranslationSpec();

        assertEquals(OrFilterInput.class, translationSpec.getInputObjectClass());
    }
}
