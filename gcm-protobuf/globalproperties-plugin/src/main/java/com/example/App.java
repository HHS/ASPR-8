package com.example;

import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class App {
    private JsonObject inputJson;

    private App(String inputFileName) {
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(this.getClass().getResourceAsStream(inputFileName)));
        this.inputJson = JsonParser.parseReader(jsonReader).getAsJsonObject();
    }

    private JsonObject getInput() {
        return this.inputJson;
    }

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
        App app = new App("/json/testJson1.json");

        JsonObject input = app.getInput();

        GlobalPropertiesTranslator translator = GlobalPropertiesTranslator.builder().build();

        GlobalPropertiesPluginDataInput inputData = translator.parseJson(input, GlobalPropertiesPluginDataInput.newBuilder());
        GlobalPropertiesPluginData pluginData = translator.convertInputToPluginData(inputData);

                
        translator.printJson(inputData);

        System.out.println(pluginData.getGlobalPropertyIds());
        for (GlobalPropertyId id : pluginData.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = pluginData.getGlobalPropertyDefinition(id);
            Object value = pluginData.getGlobalPropertyValue(id);
            System.out.println(propertyDefinition);
            System.out.println(value);

        }
    }
}
