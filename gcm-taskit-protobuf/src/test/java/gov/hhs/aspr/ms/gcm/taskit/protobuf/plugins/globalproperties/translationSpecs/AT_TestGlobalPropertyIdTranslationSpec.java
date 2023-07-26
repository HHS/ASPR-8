package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.testsupport.input.TestGlobalPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestGlobalPropertyIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestGlobalPropertyIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GlobalPropertiesPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        TestGlobalPropertyIdTranslationSpec translationSpec = new TestGlobalPropertyIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestGlobalPropertyId expectedValue = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;

        TestGlobalPropertyIdInput inputValue = translationSpec.convertAppObject(expectedValue);

        TestGlobalPropertyId actualValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = TestGlobalPropertyIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestGlobalPropertyIdTranslationSpec translationSpec = new TestGlobalPropertyIdTranslationSpec();

        assertEquals(TestGlobalPropertyId.class,
                translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestGlobalPropertyIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestGlobalPropertyIdTranslationSpec translationSpec = new TestGlobalPropertyIdTranslationSpec();

        assertEquals(TestGlobalPropertyIdInput.class,
                translationSpec.getInputObjectClass());
    }

}
