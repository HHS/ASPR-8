package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.GroupsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypesForPersonFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.groups.support.GroupTypesForPersonFilter;
import plugins.partitions.support.Equality;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GroupTypesForPersonFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = GroupTypesForPersonFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GroupTypesForPersonFilterTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GroupsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(PartitionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        GroupTypesForPersonFilterTranslationSpec translationSpec = new GroupTypesForPersonFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        GroupTypesForPersonFilter expectedAppValue = new GroupTypesForPersonFilter(Equality.EQUAL, 10);

        GroupTypesForPersonFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        GroupTypesForPersonFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GroupTypesForPersonFilterTranslationSpec translationSpec = new GroupTypesForPersonFilterTranslationSpec();

        assertEquals(GroupTypesForPersonFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GroupTypesForPersonFilterTranslationSpec translationSpec = new GroupTypesForPersonFilterTranslationSpec();

        assertEquals(GroupTypesForPersonFilterInput.class, translationSpec.getInputObjectClass());
    }
}
