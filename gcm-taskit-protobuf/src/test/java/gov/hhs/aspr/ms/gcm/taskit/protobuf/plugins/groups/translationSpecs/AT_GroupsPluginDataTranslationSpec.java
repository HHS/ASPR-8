package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.GroupsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.data.input.GroupsPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.groups.datamanagers.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GroupsPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = GroupsPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GroupsPluginDataTranslationSpec());
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

        GroupsPluginDataTranslationSpec translationSpec = new GroupsPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        int expectedGroupsPerPerson = 5;
        int expectedPeoplePerGroup = 100;

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        GroupsPluginData expectedAppValue = GroupsTestPluginFactory.getStandardGroupsPluginData(expectedGroupsPerPerson,
                expectedPeoplePerGroup, people, seed);

        GroupsPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        GroupsPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());

        expectedGroupsPerPerson = 0;
        expectedPeoplePerGroup = 0;
        GroupsPluginData.Builder builder = (GroupsPluginData.Builder) GroupsTestPluginFactory
                .getStandardGroupsPluginData(expectedGroupsPerPerson,
                        expectedPeoplePerGroup, people, seed)
                .getCloneBuilder();
        builder.addGroup(new GroupId(100), TestGroupTypeId.GROUP_TYPE_1)
                .associatePersonToGroup(new GroupId(100), new PersonId(110))
                .setNextGroupIdValue(101);

        expectedAppValue = builder.build();
        inputValue = translationSpec.convertAppObject(expectedAppValue);

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GroupsPluginDataTranslationSpec translationSpec = new GroupsPluginDataTranslationSpec();

        assertEquals(GroupsPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GroupsPluginDataTranslationSpec translationSpec = new GroupsPluginDataTranslationSpec();

        assertEquals(GroupsPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
