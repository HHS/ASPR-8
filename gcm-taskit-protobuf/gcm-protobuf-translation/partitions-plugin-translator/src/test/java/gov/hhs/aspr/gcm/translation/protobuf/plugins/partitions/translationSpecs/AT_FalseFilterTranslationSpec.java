package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.FalseFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.support.filters.FalseFilter;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_FalseFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = FalseFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new FalseFilterTranslationSpec());
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

        FalseFilterTranslationSpec translationSpec = new FalseFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        FalseFilter expectedAppValue = new FalseFilter();

        FalseFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        FalseFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = FalseFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        FalseFilterTranslationSpec translationSpec = new FalseFilterTranslationSpec();

        assertEquals(FalseFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = FalseFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        FalseFilterTranslationSpec translationSpec = new FalseFilterTranslationSpec();

        assertEquals(FalseFilterInput.class, translationSpec.getInputObjectClass());
    }
}
