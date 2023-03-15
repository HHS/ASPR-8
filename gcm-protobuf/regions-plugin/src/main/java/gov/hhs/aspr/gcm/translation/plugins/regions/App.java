package gov.hhs.aspr.gcm.translation.plugins.regions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.TestRegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.TestRegionPropertyIdTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

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

    private static void checkSame(RegionsPluginData actualPluginData) {
        boolean isSame = true;

        long seed = 524805676405822016L;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);

        Set<RegionId> actualRegionIds = actualPluginData.getRegionIds();
        if (!expectedRegionIds.equals(actualRegionIds)) {
            isSame = printNotSame();
        }

        Set<TestRegionPropertyId> expectedRegionPropertyIds = EnumSet.allOf(TestRegionPropertyId.class);

        Set<RegionPropertyId> actualRegionPropertyIds = actualPluginData.getRegionPropertyIds();
        if (!expectedRegionPropertyIds.equals(actualRegionPropertyIds)) {
            isSame = printNotSame();
        }
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (TestRegionPropertyId expectedRegionPropertyId : TestRegionPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = expectedRegionPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getRegionPropertyDefinition(expectedRegionPropertyId);
            if (!expectedPropertyDefinition.equals(actualPropertyDefinition)) {
                isSame = printNotSame();
            }

            if (expectedPropertyDefinition.getDefaultValue().isEmpty()) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    Object expectedPropertyValue = expectedRegionPropertyId.getRandomPropertyValue(randomGenerator);
                    actualPluginData.getRegionPropertyValues(regionId);
                    if (!actualPluginData.getRegionPropertyValues(regionId).containsKey(expectedRegionPropertyId)) {
                        isSame = printNotSame();
                    }
                    if (!expectedPropertyValue.equals(
                            actualPluginData.getRegionPropertyValues(regionId).get(expectedRegionPropertyId))) {
                        isSame = printNotSame();
                    }
                }
            }

        }
        if (!TimeTrackingPolicy.TRACK_TIME.equals(actualPluginData.getPersonRegionArrivalTrackingPolicy())) {
            isSame = printNotSame();
        }

        if (100 != actualPluginData.getPersonCount()) {
            isSame = printNotSame();
        }

        TestRegionId regionId = TestRegionId.REGION_1;
        for (PersonId personId : people) {
            if (actualPluginData.getPersonRegion(personId).isEmpty()) {
                isSame = printNotSame();
            }
            if (!regionId.equals(actualPluginData.getPersonRegion(personId).get())) {
                isSame = printNotSame();
            }
            regionId = regionId.next();
        }

        if (isSame) {
            System.out.println("Datas are the same");
        }
    }

    public static void main(String[] args) {

        String inputFileName = "./regions-plugin/src/main/resources/json/input.json";
        String outputFileName = "./regions-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(RegionsPluginBundle.getPluginBundle(inputFileName, outputFileName))
                .addBundle(PropertiesPluginBundle.getPluginBundle())
                .addBundle(PeoplePluginBundle.getPluginBundle())
                .addTranslator(new TestRegionIdTranslator())
                .addTranslator(new TestRegionPropertyIdTranslator())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        RegionsPluginData actualPluginData = (RegionsPluginData) pluginDatas.get(0);

        // List<PersonId> people = new ArrayList<>();

        // for (int i = 0; i < 100; i++) {
        // people.add(new PersonId(i));
        // }

        checkSame(actualPluginData);

        // translatorController.writeOutput(GroupsTestPluginFactory.getStandardGroupsPluginData(5,
        // 100, people,
        // 524805676405822016L));

        translatorController.writeOutput();
    }

}
