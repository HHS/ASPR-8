package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import plugins.groups.GroupsPluginData;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AppTest {
    Path basePath = getCurrentDir();
    Path inputFilePath = basePath.resolve("json");
    Path outputFilePath = makeOutputDir();

    private Path getCurrentDir() {
        try {
            return Path.of(this.getClass().getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path makeOutputDir() {
        Path path = basePath.resolve("json/output");

        path.toFile().mkdirs();

        return path;
    }

    @Test
    public void testGroupsTranslator() {
        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(GroupsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), GroupsPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), GroupsPluginData.class)
                .build();

        translatorController.readInput();

        GroupsPluginData groupsPluginData = translatorController.getObject(GroupsPluginData.class);

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

    @Test
    public void testGroupPropertyReportTranslatorSpec() {
        String fileName = "propertyReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(GroupsTranslator.builder(true).build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), GroupPropertyReportPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), GroupPropertyReportPluginData.class)
                .build();

        translatorController.readInput();

        GroupPropertyReportPluginData actualPluginData = translatorController
                .getObject(GroupPropertyReportPluginData.class);

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

        GroupPropertyReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());
        assertEquals(expectedPluginData.getDefaultInclusionPolicy(), actualPluginData.getDefaultInclusionPolicy());
        assertEquals(expectedPluginData.getGroupTypeIds(), actualPluginData.getGroupTypeIds());

        for (GroupTypeId groupTypeId : actualPluginData.getGroupTypeIds()) {
            assertEquals(expectedPluginData.getIncludedProperties(groupTypeId),
                    actualPluginData.getIncludedProperties(groupTypeId));
            assertEquals(expectedPluginData.getExcludedProperties(groupTypeId),
                    actualPluginData.getExcludedProperties(groupTypeId));
        }

        translatorController.writeOutput();
    }
}
