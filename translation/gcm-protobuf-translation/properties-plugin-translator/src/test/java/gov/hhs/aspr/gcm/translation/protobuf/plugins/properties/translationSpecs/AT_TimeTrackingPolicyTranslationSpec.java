package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.TimeTrackingPolicyInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TimeTrackingPolicyTranslationSpec {
    
    @Test
    @UnitTestConstructor(target = TimeTrackingPolicyTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TimeTrackingPolicyTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PropertiesTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        TimeTrackingPolicyTranslationSpec translationSpec = new TimeTrackingPolicyTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TimeTrackingPolicy expectedAppValue = TimeTrackingPolicy.TRACK_TIME;

        TimeTrackingPolicyInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TimeTrackingPolicy actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TimeTrackingPolicyTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TimeTrackingPolicyTranslationSpec translationSpec = new TimeTrackingPolicyTranslationSpec();

        assertEquals(TimeTrackingPolicy.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TimeTrackingPolicyTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TimeTrackingPolicyTranslationSpec translationSpec = new TimeTrackingPolicyTranslationSpec();

        assertEquals(TimeTrackingPolicyInput.class, translationSpec.getInputObjectClass());
    }
}
