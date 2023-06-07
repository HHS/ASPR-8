package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.GroupsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_GroupPropertyReportPluginDataTranslationSpec {
    
    @Test
    @UnitTestConstructor(target = GroupPropertyReportPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GroupPropertyReportPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GroupsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        GroupPropertyReportPluginDataTranslationSpec translationSpec = new GroupPropertyReportPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524805676405822016L);

        GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();

        ReportLabel reportLabel = new SimpleReportLabel("report label");

        builder.setReportLabel(reportLabel).setDefaultInclusion(false).setReportPeriod(ReportPeriod.DAILY);

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
                if (randomGenerator.nextBoolean()) {
                    builder.includeGroupProperty(testGroupTypeId, testGroupPropertyId);
                } else {
                    builder.excludeGroupProperty(testGroupTypeId, testGroupPropertyId);
                }
            }
        }

        GroupPropertyReportPluginData expectedAppValue = builder.build();

        GroupPropertyReportPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        GroupPropertyReportPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = GroupPropertyReportPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GroupPropertyReportPluginDataTranslationSpec translationSpec = new GroupPropertyReportPluginDataTranslationSpec();

        assertEquals(GroupPropertyReportPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyReportPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GroupPropertyReportPluginDataTranslationSpec translationSpec = new GroupPropertyReportPluginDataTranslationSpec();

        assertEquals(GroupPropertyReportPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
