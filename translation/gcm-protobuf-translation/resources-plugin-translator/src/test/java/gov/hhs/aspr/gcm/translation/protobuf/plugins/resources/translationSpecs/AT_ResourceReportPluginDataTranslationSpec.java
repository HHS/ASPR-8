package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceReportPluginDataInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_ResourceReportPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = ResourceReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ResourceReportPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        ResourceReportPluginDataTranslationSpec translationSpec = new ResourceReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 524805676405822016L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ReportLabel reportLabel = new SimpleReportLabel("resource report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod).setDefaultInclusion(false);

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        for (ResourceId resourceId : expectedResourceIds) {
            if (randomGenerator.nextBoolean()) {
                builder.includeResource(resourceId);
            } else {
                builder.excludeResource(resourceId);
            }
        }

        ResourceReportPluginData expectedAppValue = builder.build();

        ResourceReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ResourceReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ResourceReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ResourceReportPluginDataTranslationSpec translationSpec = new ResourceReportPluginDataTranslationSpec();

        assertEquals(ResourceReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ResourceReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ResourceReportPluginDataTranslationSpec translationSpec = new ResourceReportPluginDataTranslationSpec();

        assertEquals(ResourceReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
