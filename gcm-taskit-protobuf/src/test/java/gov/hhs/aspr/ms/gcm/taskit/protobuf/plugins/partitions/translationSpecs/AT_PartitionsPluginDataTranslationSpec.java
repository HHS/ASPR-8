package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.data.input.PartitionsPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs.TestLabelerTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.datamanagers.PartitionsPluginData;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PartitionsPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = PartitionsPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PartitionsPluginDataTranslationSpec());
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

        PartitionsPluginDataTranslationSpec translationSpec = new PartitionsPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PartitionsPluginData expectedAppValue = PartitionsPluginData.builder()
                .setRunContinuitySupport(true)
                .build();

        PartitionsPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PartitionsPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());

        expectedAppValue = PartitionsPluginData.builder()
                .setRunContinuitySupport(false)
                .build();

        inputValue = translationSpec.convertAppObject(expectedAppValue);
        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = PartitionsPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PartitionsPluginDataTranslationSpec translationSpec = new PartitionsPluginDataTranslationSpec();

        assertEquals(PartitionsPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PartitionsPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PartitionsPluginDataTranslationSpec translationSpec = new PartitionsPluginDataTranslationSpec();

        assertEquals(PartitionsPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
