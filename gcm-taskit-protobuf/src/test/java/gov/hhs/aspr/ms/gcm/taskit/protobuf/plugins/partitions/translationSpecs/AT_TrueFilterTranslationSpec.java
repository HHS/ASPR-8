package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.TrueFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.partitions.support.filters.TrueFilter;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TrueFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = TrueFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TrueFilterTranslationSpec());
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

        TrueFilterTranslationSpec translationSpec = new TrueFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TrueFilter expectedAppValue = new TrueFilter();

        TrueFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TrueFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TrueFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TrueFilterTranslationSpec translationSpec = new TrueFilterTranslationSpec();

        assertEquals(TrueFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TrueFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TrueFilterTranslationSpec translationSpec = new TrueFilterTranslationSpec();

        assertEquals(TrueFilterInput.class, translationSpec.getInputObjectClass());
    }
}
