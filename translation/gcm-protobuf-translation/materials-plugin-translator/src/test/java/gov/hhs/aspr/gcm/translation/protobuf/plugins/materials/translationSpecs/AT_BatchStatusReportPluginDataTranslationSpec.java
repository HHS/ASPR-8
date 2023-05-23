package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchStatusReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_BatchStatusReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = BatchStatusReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new BatchStatusReportPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(MaterialsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        BatchStatusReportPluginDataTranslationSpec translationSpec = new BatchStatusReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

        ReportLabel reportLabel = new SimpleReportLabel("batch status report label");

        builder.setReportLabel(reportLabel);

        BatchStatusReportPluginData expectedAppValue = builder.build();

        BatchStatusReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        BatchStatusReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = BatchStatusReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        BatchStatusReportPluginDataTranslationSpec translationSpec = new BatchStatusReportPluginDataTranslationSpec();

        assertEquals(BatchStatusReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = BatchStatusReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        BatchStatusReportPluginDataTranslationSpec translationSpec = new BatchStatusReportPluginDataTranslationSpec();

        assertEquals(BatchStatusReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
