package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.LabelerInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestLabeler;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.support.Labeler;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_LabelerTranslationSpec {

    @Test
    @UnitTestConstructor(target = LabelerTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new LabelerTranslationSpec());
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

        LabelerTranslationSpec translationSpec = new LabelerTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Labeler expectedAppValue = new TestLabeler("test");

        LabelerInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        Labeler actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = LabelerTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        LabelerTranslationSpec translationSpec = new LabelerTranslationSpec();

        assertEquals(Labeler.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = LabelerTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        LabelerTranslationSpec translationSpec = new LabelerTranslationSpec();

        assertEquals(LabelerInput.class, translationSpec.getInputObjectClass());
    }
}
