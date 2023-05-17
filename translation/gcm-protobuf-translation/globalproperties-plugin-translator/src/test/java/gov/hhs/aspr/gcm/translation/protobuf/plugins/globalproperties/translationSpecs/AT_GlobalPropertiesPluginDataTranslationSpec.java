package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GlobalPropertiesPluginDataTranslationSpec {

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        GlobalPropertiesPluginDataTranslationSpec translationSpec = new GlobalPropertiesPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        GlobalPropertiesPluginData expectedValue = GlobalPropertiesTestPluginFactory
                .getStandardGlobalPropertiesPluginData();

        GlobalPropertiesPluginData.Builder expectedValueBuilder = (GlobalPropertiesPluginData.Builder) expectedValue
                .getCloneBuilder();

        expectedValueBuilder
                .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 1500, 0)
                .defineGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE,
                        PropertyDefinition.builder()//
                                .setType(Double.class)//
                                .setPropertyValueMutability(true)//
                                .build(),
                        0)
                .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE, 500.0, 0)
                .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, false, 0);
                    

        expectedValue = expectedValueBuilder.build();

        GlobalPropertiesPluginDataInput globalPropertiesPluginDataInput = translationSpec
                .convertAppObject(expectedValue);

        GlobalPropertiesPluginData actualValue = translationSpec
                .convertInputObject(globalPropertiesPluginDataInput);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GlobalPropertiesPluginDataTranslationSpec translationSpec = new GlobalPropertiesPluginDataTranslationSpec();

        assertEquals(GlobalPropertiesPluginData.class,
                translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GlobalPropertiesPluginDataTranslationSpec translationSpec = new GlobalPropertiesPluginDataTranslationSpec();

        assertEquals(GlobalPropertiesPluginDataInput.class,
                translationSpec.getInputObjectClass());
    }

}
