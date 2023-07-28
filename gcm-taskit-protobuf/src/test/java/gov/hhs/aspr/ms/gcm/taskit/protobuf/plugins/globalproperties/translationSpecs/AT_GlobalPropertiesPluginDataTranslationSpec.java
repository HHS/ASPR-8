package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.data.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GlobalPropertiesPluginDataTranslationSpec {

        @Test
        @UnitTestConstructor(target = GlobalPropertiesPluginDataTranslationSpec.class, args = {})
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

                GlobalPropertiesPluginDataTranslationSpec translationSpec = new GlobalPropertiesPluginDataTranslationSpec();
                translationSpec.init(protobufTranslationEngine);

                GlobalPropertiesPluginData expectedAppValue = GlobalPropertiesTestPluginFactory
                                .getStandardGlobalPropertiesPluginData(8368397106493368066L);

                GlobalPropertiesPluginDataInput globalPropertiesPluginDataInput = translationSpec
                                .convertAppObject(expectedAppValue);

                GlobalPropertiesPluginData actualAppValue = translationSpec
                                .convertInputObject(globalPropertiesPluginDataInput);

                assertEquals(expectedAppValue, actualAppValue);
                assertEquals(expectedAppValue.toString(), actualAppValue.toString());
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
