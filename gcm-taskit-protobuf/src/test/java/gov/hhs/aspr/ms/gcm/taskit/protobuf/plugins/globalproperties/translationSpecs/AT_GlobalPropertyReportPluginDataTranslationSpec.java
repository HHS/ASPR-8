package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.reports.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = GlobalPropertyReportPluginDataTranslationSpec.class, args = {})
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

        GlobalPropertyReportPluginDataTranslationSpec translationSpec = new GlobalPropertyReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524805676405822016L);
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        ReportLabel reportLabel = new SimpleReportLabel("report label");

        builder.setDefaultInclusion(false).setReportLabel(reportLabel);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeGlobalProperty(testGlobalPropertyId);
            } else {
                builder.excludeGlobalProperty(testGlobalPropertyId);
            }
        }

        GlobalPropertyReportPluginData expectedAppValue = builder.build();

        GlobalPropertyReportPluginDataInput globalPropertiesPluginDataInput = translationSpec
                .convertAppObject(expectedAppValue);

        GlobalPropertyReportPluginData actualAppValue = translationSpec
                .convertInputObject(globalPropertiesPluginDataInput);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GlobalPropertyReportPluginDataTranslationSpec translationSpec = new GlobalPropertyReportPluginDataTranslationSpec();

        assertEquals(GlobalPropertyReportPluginData.class,
                translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GlobalPropertyReportPluginDataTranslationSpec translationSpec = new GlobalPropertyReportPluginDataTranslationSpec();

        assertEquals(GlobalPropertyReportPluginDataInput.class,
                translationSpec.getInputObjectClass());
    }

}
