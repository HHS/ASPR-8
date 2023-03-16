package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
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

    private static void printNotSame() {
        System.out.println("PluginDatas not same");
    }

    private static void checkSame(PersonPropertiesPluginData actualPluginData) {

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);

        Set<PersonPropertyId> actualPersonPropertyIds = actualPluginData.getPersonPropertyIds();
        if (!expectedPersonPropertyIds.equals(actualPersonPropertyIds)) {
            printNotSame();
            return;
        }

        for (TestPersonPropertyId expecetedPropertyId : expectedPersonPropertyIds) {
            PropertyDefinition expectedPropertyDefinition = expecetedPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getPersonPropertyDefinition(expecetedPropertyId);
            if (!expectedPropertyDefinition.equals(actualPropertyDefinition)) {
                printNotSame();
                return;
            }
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4684903523797799712L);
        for (PersonId personId : people) {
            List<Pair<TestPersonPropertyId, Object>> expectedValues = new ArrayList<>();
            for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
                if (propertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
                    Object expectedPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
                    expectedValues.add(new Pair<>(propertyId, expectedPropertyValue));
                }
            }
            List<PersonPropertyInitialization> propInitList = actualPluginData
                    .getPropertyValues(personId.getValue());

            if (expectedValues.size() != propInitList.size()) {
                printNotSame();
                return;
            }
            for (int i = 0; i < propInitList.size(); i++) {
                TestPersonPropertyId expectedPersonPropertyId = expectedValues.get(i).getFirst();
                Object expectedValue = expectedValues.get(i).getSecond();

                PersonPropertyId actualPropertyId = propInitList.get(i).getPersonPropertyId();
                Object actualValue = propInitList.get(i).getValue();

                if (!expectedPersonPropertyId.equals(actualPropertyId)) {
                    printNotSame();
                    return;
                }
                if (!expectedValue.equals(actualValue)) {
                    printNotSame();
                    return;
                }

            }

        }

        System.out.println("PluginDatas are the same");
    }

    public static void main(String[] args) {

        String inputFileName = "./personproperties-plugin/src/main/resources/json/input.json";
        String outputFileName = "./personproperties-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(PersonPropertiesTranslator.getTranslatorRW(inputFileName, outputFileName))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslatorSpec(new TestPersonPropertyIdTranslatorSpec())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        PersonPropertiesPluginData actualPluginData = (PersonPropertiesPluginData) pluginDatas.get(0);

        checkSame(actualPluginData);

        translatorController.writeOutput();
    }
}
