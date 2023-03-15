package gov.hhs.aspr.gcm.translation.plugins.groups;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.SimpleGroupTypeIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.TestGroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.TestGroupTypeIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslator;
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

public class App {
    public JsonObject deepMerge(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            JsonElement value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.add(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value.isJsonObject()) {
                    JsonObject valueJson = value.getAsJsonObject();
                    deepMerge(valueJson, target.getAsJsonObject(key));
                } else if (value.isJsonArray()) {
                    JsonArray valueArray = value.getAsJsonArray();
                    JsonArray targetArray = target.getAsJsonArray(key);
                    targetArray.addAll(valueArray);
                } else {
                    target.add(key, value);
                }
            }
        }
        return target;
    }

    private static boolean printNotSame() {
        System.out.println("Datas are not the same");
        return false;
    }

    private static void checkSame(GroupsPluginData actualPluginData) {
        boolean isSame = true;

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestGroupTypeId> expectedGroupTypeIds = EnumSet.allOf(TestGroupTypeId.class);
        Set<GroupTypeId> actualGroupTypeIds = actualPluginData.getGroupTypeIds();

        if (!actualGroupTypeIds.equals(expectedGroupTypeIds)) {
            isSame = printNotSame();
        }

        for (TestGroupPropertyId expectedPropertyId : TestGroupPropertyId.values()) {
            TestGroupTypeId expectedGroupTypeId = expectedPropertyId.getTestGroupTypeId();
            PropertyDefinition expectedPropertyDefinition = expectedPropertyId.getPropertyDefinition();

            if (!actualPluginData.getGroupPropertyIds(expectedGroupTypeId).contains(expectedPropertyId)) {
                isSame = printNotSame();
            }
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getGroupPropertyDefinition(expectedGroupTypeId, expectedPropertyId);
            if (!expectedPropertyDefinition.equals(actualPropertyDefinition)) {
                isSame = printNotSame();
            }
        }

        if (actualPluginData.getGroupIds().size() != 5) {
            isSame = printNotSame();
        }

        if (actualPluginData.getPersonCount() != 100) {
            isSame = printNotSame();
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524805676405822016L);
        for (GroupId groupId : actualPluginData.getGroupIds()) {
            TestGroupTypeId expectedGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            GroupTypeId actualGroupTypeId = actualPluginData.getGroupTypeId(groupId);
            if (expectedGroupTypeId != actualGroupTypeId) {
                isSame = printNotSame();
            }

            List<GroupPropertyValue> expectedGroupPropertyValues = new ArrayList<>();
            for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                    .getTestGroupPropertyIds(expectedGroupTypeId)) {
                GroupPropertyValue expectedValue = new GroupPropertyValue(testGroupPropertyId,
                        testGroupPropertyId.getRandomPropertyValue(randomGenerator));
                expectedGroupPropertyValues.add(expectedValue);
            }

            if (expectedGroupPropertyValues.size() != actualPluginData.getGroupPropertyValues(groupId).size()) {
                isSame = printNotSame();
            }

            for (int i = 0; i < expectedGroupPropertyValues.size(); i++) {
                GroupPropertyValue expectPropertyValue = expectedGroupPropertyValues.get(i);
                GroupPropertyValue actualPropertyValue = actualPluginData.getGroupPropertyValues(groupId).get(i);
                if (!expectPropertyValue.equals(actualPropertyValue)) {
                    isSame = printNotSame();
                }
            }
        }

        Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
        List<GroupId> groups = actualPluginData.getGroupIds();
        while (groupMemeberships.size() < 100) {
            PersonId personId = people.get(randomGenerator.nextInt(people.size()));
            GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
            groupMemeberships.add(new MultiKey(groupId, personId));
        }

        for (MultiKey multiKey : groupMemeberships) {
            GroupId expectedGroupId = multiKey.getKey(0);
            PersonId expectedPersonId = multiKey.getKey(1);

            if (!actualPluginData.getGroupsForPerson(expectedPersonId).contains(expectedGroupId)) {
                isSame = printNotSame();
            }
        }

        double numGroups = 0;
        for (PersonId person : people) {
            numGroups += actualPluginData.getGroupsForPerson(person).size();
        }

        double actualGroupsPerPerson = numGroups / 100;

        double lowerBound = 1 * 0.9;
        double upperBound = 1 * 1.1;

        if (actualGroupsPerPerson > upperBound) {
            isSame = printNotSame();
        }
        if (actualGroupsPerPerson <= lowerBound) {
            isSame = printNotSame();
        }

        if (isSame) {
            System.out.println("Datas are the same");
        }
    }

    public static void main(String[] args) {

        String inputFileName = "./groups-plugin/src/main/resources/json/input.json";
        String outputFileName = "./groups-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(GroupsTranslator.getTranslator(inputFileName, outputFileName))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslatorSpec(new TestGroupTypeIdTranslator())
                .addTranslatorSpec(new TestGroupPropertyIdTranslator())
                .addTranslatorSpec(new SimpleGroupTypeIdTranslator())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GroupsPluginData actualPluginData = (GroupsPluginData) pluginDatas.get(0);

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        checkSame(actualPluginData);

        // translatorController.writeOutput(GroupsTestPluginFactory.getStandardGroupsPluginData(5, 100, people, 
        //         524805676405822016L));

        translatorController.writeOutput();
    }
}
