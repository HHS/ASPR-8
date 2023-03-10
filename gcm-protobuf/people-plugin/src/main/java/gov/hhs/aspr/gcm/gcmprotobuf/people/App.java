package gov.hhs.aspr.gcm.gcmprotobuf.people;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.gcmprotobuf.core.TranslatorController;
import nucleus.PluginData;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;

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
        System.out.println("PluginDatas not same");
        return false;
    }

    private static void checkSame(PeoplePluginData actualPluginData) {
        boolean isSame = true;

        if (actualPluginData.getPersonIds().size() > 100) {
            isSame = printNotSame();
        }

        List<PersonId> expectedPersonIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0)
                expectedPersonIds.add(new PersonId(i));
            else
                expectedPersonIds.add(null);
        }

        for (int i = 0; i < actualPluginData.getPersonIds().size(); i++) {
            PersonId actualPersonId = actualPluginData.getPersonIds().get(i);
            PersonId expectedPersonid = expectedPersonIds.get(i);

            if (expectedPersonid == null) {
                if (actualPersonId != null)
                    isSame = printNotSame();
            } else if (!expectedPersonid.equals(actualPersonId)) {
                isSame = printNotSame();
            }
        }

        if (isSame) {
            System.out.println("PluginDatas are the same");
        }
    }

    public static void main(String[] args) {

        String inputFileName = "./people-plugin/src/main/resources/json/input.json";
        String outputFileName = "./people-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(PeoplePluginBundle.getPluginBundle(inputFileName, outputFileName))
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();
        PeoplePluginData peoplePluginData = (PeoplePluginData) pluginDatas.get(0);

        checkSame(peoplePluginData);

        translatorController.writeOutput();
    }
}