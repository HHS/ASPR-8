package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.SimpleGroupTypeIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.TestGroupPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.TestGroupTypeIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import nucleus.PluginData;
import plugins.groups.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AppTest {

    @Test
    public void testGroupsPluginTranslator() {

        String inputFileName = "./groups-plugin/src/main/resources/json/input.json";
        String outputFileName = "./groups-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(GroupsTranslator.getTranslatorRW(inputFileName, outputFileName))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslatorSpec(new TestGroupTypeIdTranslatorSpec())
                .addTranslatorSpec(new TestGroupPropertyIdTranslatorSpec())
                .addTranslatorSpec(new SimpleGroupTypeIdTranslatorSpec())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GroupsPluginData groupsPluginData = (GroupsPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        int groupCount = 5;
        int membershipCount = 100;
        int expectedGroupsPerPerson = 1;

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        Set<TestGroupTypeId> expectedGroupTypeIds = EnumSet.allOf(TestGroupTypeId.class);
        assertFalse(expectedGroupTypeIds.isEmpty());

        Set<GroupTypeId> actualGroupTypeIds = groupsPluginData.getGroupTypeIds();
        assertEquals(expectedGroupTypeIds, actualGroupTypeIds);

        Set<TestGroupPropertyId> expectedGroupPropertyIds = EnumSet.allOf(TestGroupPropertyId.class);
        assertFalse(expectedGroupPropertyIds.isEmpty());

        for (TestGroupPropertyId expectedPropertyId : TestGroupPropertyId.values()) {
            TestGroupTypeId expectedGroupTypeId = expectedPropertyId.getTestGroupTypeId();
            PropertyDefinition expectedPropertyDefinition = expectedPropertyId.getPropertyDefinition();

            assertTrue(groupsPluginData.getGroupPropertyIds(expectedGroupTypeId).contains(expectedPropertyId));
            PropertyDefinition actualPropertyDefinition = groupsPluginData
                    .getGroupPropertyDefinition(expectedGroupTypeId, expectedPropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
        }

        assertEquals(groupCount, groupsPluginData.getGroupIds().size());
        assertEquals(initialPopulation, groupsPluginData.getPersonCount());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (GroupId groupId : groupsPluginData.getGroupIds()) {
            GroupTypeId actualGroupTypeId = groupsPluginData.getGroupTypeId(groupId);
            TestGroupTypeId expectedGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            assertEquals(expectedGroupTypeId, actualGroupTypeId);

            List<GroupPropertyValue> expectedGroupPropertyValues = new ArrayList<>();
            for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                    .getTestGroupPropertyIds(expectedGroupTypeId)) {
                GroupPropertyValue expectedValue = new GroupPropertyValue(testGroupPropertyId,
                        testGroupPropertyId.getRandomPropertyValue(randomGenerator));
                expectedGroupPropertyValues.add(expectedValue);
            }

            assertEquals(expectedGroupPropertyValues.size(), groupsPluginData.getGroupPropertyValues(groupId).size());
            for (int i = 0; i < expectedGroupPropertyValues.size(); i++) {
                assertEquals(expectedGroupPropertyValues.get(i),
                        groupsPluginData.getGroupPropertyValues(groupId).get(i));
            }
            expectedGroupTypeId = expectedGroupTypeId.next();
        }

        Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
        List<GroupId> groups = groupsPluginData.getGroupIds();
        while (groupMemeberships.size() < membershipCount) {
            PersonId personId = people.get(randomGenerator.nextInt(people.size()));
            GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
            groupMemeberships.add(new MultiKey(groupId, personId));
        }

        for (MultiKey multiKey : groupMemeberships) {
            GroupId expectedGroupId = multiKey.getKey(0);
            PersonId expectedPersonId = multiKey.getKey(1);

            assertTrue(groupsPluginData.getGroupsForPerson(expectedPersonId).contains(expectedGroupId));
        }

        double numGroups = 0;
        for (PersonId person : people) {
            numGroups += groupsPluginData.getGroupsForPerson(person).size();
        }

        double actualGroupsPerPerson = numGroups / initialPopulation;

        double lowerBound = expectedGroupsPerPerson * 0.9;
        double upperBound = expectedGroupsPerPerson * 1.1;

        assertTrue(actualGroupsPerPerson <= upperBound);
        assertTrue(actualGroupsPerPerson > lowerBound);

        translatorController.writeOutput();
    }
}
