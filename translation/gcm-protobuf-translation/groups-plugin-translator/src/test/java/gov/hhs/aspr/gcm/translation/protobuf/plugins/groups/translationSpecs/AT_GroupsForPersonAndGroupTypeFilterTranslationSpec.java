package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.GroupsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupsForPersonAndGroupTypeFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.groups.support.GroupsForPersonAndGroupTypeFilter;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Equality;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GroupsForPersonAndGroupTypeFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = GroupsForPersonAndGroupTypeFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GroupsForPersonAndGroupTypeFilterTranslationSpec());
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
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        GroupsForPersonAndGroupTypeFilterTranslationSpec translationSpec = new GroupsForPersonAndGroupTypeFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        GroupsForPersonAndGroupTypeFilter expectedAppValue = new GroupsForPersonAndGroupTypeFilter(
                TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 10);

        GroupsForPersonAndGroupTypeFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        GroupsForPersonAndGroupTypeFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GroupsForPersonAndGroupTypeFilterTranslationSpec translationSpec = new GroupsForPersonAndGroupTypeFilterTranslationSpec();

        assertEquals(GroupsForPersonAndGroupTypeFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GroupsForPersonAndGroupTypeFilterTranslationSpec translationSpec = new GroupsForPersonAndGroupTypeFilterTranslationSpec();

        assertEquals(GroupsForPersonAndGroupTypeFilterInput.class, translationSpec.getInputObjectClass());
    }
}
