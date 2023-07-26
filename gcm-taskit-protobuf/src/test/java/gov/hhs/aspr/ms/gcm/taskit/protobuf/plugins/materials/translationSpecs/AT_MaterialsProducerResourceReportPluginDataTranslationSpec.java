package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.reports.input.MaterialsProducerResourceReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_MaterialsProducerResourceReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = MaterialsProducerResourceReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new MaterialsProducerResourceReportPluginDataTranslationSpec());
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

        MaterialsProducerResourceReportPluginDataTranslationSpec translationSpec = new MaterialsProducerResourceReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

        ReportLabel reportLabel = new SimpleReportLabel("materials producer resource report label");

        builder.setReportLabel(reportLabel);

        MaterialsProducerResourceReportPluginData expectedAppValue = builder.build();

        MaterialsProducerResourceReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        MaterialsProducerResourceReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerResourceReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        MaterialsProducerResourceReportPluginDataTranslationSpec translationSpec = new MaterialsProducerResourceReportPluginDataTranslationSpec();

        assertEquals(MaterialsProducerResourceReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerResourceReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        MaterialsProducerResourceReportPluginDataTranslationSpec translationSpec = new MaterialsProducerResourceReportPluginDataTranslationSpec();

        assertEquals(MaterialsProducerResourceReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
