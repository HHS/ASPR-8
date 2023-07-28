package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.reports.input.MaterialsProducerPropertyReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_MaterialsProducerPropertyReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = MaterialsProducerPropertyReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new MaterialsProducerPropertyReportPluginDataTranslationSpec());
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

        MaterialsProducerPropertyReportPluginDataTranslationSpec translationSpec = new MaterialsProducerPropertyReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();
        ReportLabel reportLabel = new SimpleReportLabel("materials producer property report report label");

        builder.setReportLabel(reportLabel);

        MaterialsProducerPropertyReportPluginData expectedAppValue = builder.build();

        MaterialsProducerPropertyReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        MaterialsProducerPropertyReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerPropertyReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        MaterialsProducerPropertyReportPluginDataTranslationSpec translationSpec = new MaterialsProducerPropertyReportPluginDataTranslationSpec();

        assertEquals(MaterialsProducerPropertyReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerPropertyReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        MaterialsProducerPropertyReportPluginDataTranslationSpec translationSpec = new MaterialsProducerPropertyReportPluginDataTranslationSpec();

        assertEquals(MaterialsProducerPropertyReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
