package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.reports.input.RegionTransferReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionTransferReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_RegionTransferReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = RegionTransferReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new RegionTransferReportPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        RegionTransferReportPluginDataTranslationSpec translationSpec = new RegionTransferReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ReportLabel reportLabel = new SimpleReportLabel("region transfer report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod);

        RegionTransferReportPluginData expectedAppValue = builder.build();

        RegionTransferReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        RegionTransferReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = RegionTransferReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        RegionTransferReportPluginDataTranslationSpec translationSpec = new RegionTransferReportPluginDataTranslationSpec();

        assertEquals(RegionTransferReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = RegionTransferReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        RegionTransferReportPluginDataTranslationSpec translationSpec = new RegionTransferReportPluginDataTranslationSpec();

        assertEquals(RegionTransferReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
