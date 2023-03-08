package gov.hhs.aspr.gcm.gcmprotobuf.people;

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

    public static void main(String[] args) {

        String inputFileName = "./people-plugin/src/main/resources/json/testJson1.json";
        String outputFileName = "./people-plugin/src/main/resources/json/output/testJson1Output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(PeoplePluginBundle.getPluginBundle(inputFileName, outputFileName))
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();
        PeoplePluginData peoplePluginData = (PeoplePluginData) pluginDatas.get(0);

        for (PersonId personId : peoplePluginData.getPersonIds()) {
            if (personId != null)
                System.out.println(personId.getValue());
        }

        translatorController.writeOutput();
    }
}