package gov.hhs.aspr.gcm.translation.plugins.resources;

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
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsPluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.TestRegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.TestResourceIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.TestResourcePropertyIdTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
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

    private static void checkSame(ResourcesPluginData actualPluginData) {
        boolean isSame = true;

        long seed = 524805676405822016L;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);

        Set<ResourceId> actualResourceIds = actualPluginData.getResourceIds();

        if (!expectedResourceIds.equals(actualResourceIds)) {
            printNotSame();
            return;
        }

        for (TestResourceId resourceId : TestResourceId.values()) {
            TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
            TimeTrackingPolicy actualPolicy = actualPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
            if (!expectedPolicy.equals(actualPolicy)) {
                printNotSame();
                return;
            }
        }


        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
            PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
            Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

            if (!actualPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId)) {
                printNotSame();
                return;
            }

            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
            if (!expectedPropertyDefinition.equals(actualPropertyDefinition)) {
                printNotSame();
                return;
            }

            Object actualPropertyValue = actualPluginData.getResourcePropertyValue(expectedResourceId,
                    resourcePropertyId);
            if (!expectedPropertyValue.equals(actualPropertyValue)) {
                printNotSame();
                return;
            }
        }

        randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        if (actualPluginData.getPersonCount() != people.size()) {
            printNotSame();
            return;
        }

        for (int i = 0; i < actualPluginData.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            List<ResourceInitialization> personResourceLevels = actualPluginData.getPersonResourceLevels(personId);

            if (personResourceLevels.isEmpty()) {
                printNotSame();
                return;
            }

            Set<ResourceId> personResourceLevelsResourceIds = new LinkedHashSet<>();
            for (ResourceInitialization resourceInitialization : personResourceLevels) {
                personResourceLevelsResourceIds.add(resourceInitialization.getResourceId());
                if (Math.abs(randomGenerator.nextLong()) != resourceInitialization.getAmount()) {
                    printNotSame();
                    return;
                }
            }
            if (!expectedResourceIds.equals(personResourceLevelsResourceIds)) {
                printNotSame();
                return;
            }
        }

        Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);

        if (!expectedRegionIds.equals(actualPluginData.getRegionIds())) {
            printNotSame();
            return;
        }
        for (RegionId regionId : actualPluginData.getRegionIds()) {

            List<ResourceInitialization> regionResourceLevels = actualPluginData.getRegionResourceLevels(regionId);
            Set<ResourceId> regionResourceLevelsResourceIds = new LinkedHashSet<>();
            for (ResourceInitialization resourceInitialization : regionResourceLevels) {
                regionResourceLevelsResourceIds.add(resourceInitialization.getResourceId());
                if (Math.abs(randomGenerator.nextLong()) != resourceInitialization.getAmount()) {
                    printNotSame();
                    return;
                }
            }
            if (!expectedResourceIds.equals(regionResourceLevelsResourceIds)) {
                printNotSame();
                return;
            }
        }

        if (isSame) {
            System.out.println("Datas are the same");
        }
    }

    public static void main(String[] args) {

        String inputFileName = "./resources-plugin/src/main/resources/json/input.json";
        String outputFileName = "./resources-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(ResourcesPluginBundle.getPluginBundle(inputFileName, outputFileName))
                .addBundle(PropertiesPluginBundle.getPluginBundle())
                .addBundle(PeoplePluginBundle.getPluginBundle())
                .addBundle(RegionsPluginBundle.getPluginBundle())
                .addTranslator(new TestResourceIdTranslator())
                .addTranslator(new TestResourcePropertyIdTranslator())
                .addTranslator(new TestRegionIdTranslator())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        ResourcesPluginData actualPluginData = (ResourcesPluginData) pluginDatas.get(0);

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        checkSame(actualPluginData);

        translatorController.writeOutput();
    }

}
