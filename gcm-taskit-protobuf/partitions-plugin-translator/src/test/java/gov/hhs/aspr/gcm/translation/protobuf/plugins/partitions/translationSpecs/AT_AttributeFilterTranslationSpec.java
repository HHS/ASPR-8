package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.AttributeFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.support.Equality;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_AttributeFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = AttributeFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new AttributeFilterTranslationSpec());
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

        AttributeFilterTranslationSpec translationSpec = new AttributeFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        AttributeFilter expectedAppValue = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, "test");

        AttributeFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        AttributeFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = AttributeFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        AttributeFilterTranslationSpec translationSpec = new AttributeFilterTranslationSpec();

        assertEquals(AttributeFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = AttributeFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        AttributeFilterTranslationSpec translationSpec = new AttributeFilterTranslationSpec();

        assertEquals(AttributeFilterInput.class, translationSpec.getInputObjectClass());
    }
}
