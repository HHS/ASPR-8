package com.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import common.Translator;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginDataInput;
import plugins.stochastics.support.RandomNumberGeneratorId;

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

        Translator stochasticsTranslator = StochasticsTranslator.builder().build();

        StochasticsPluginDataInput stochasticsPluginDataInput = stochasticsTranslator.parseJson("/json/testJson1.json",
                StochasticsPluginDataInput.newBuilder());

        StochasticsPluginData stochasticsPluginData = (StochasticsPluginData) stochasticsTranslator
                .convertInputObject(stochasticsPluginDataInput);

        stochasticsTranslator.printJson(stochasticsPluginDataInput);

        System.out.println(stochasticsPluginData.getSeed());
        System.out.println(stochasticsPluginData.getSeed() == 524805676405822016L);

        for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
            System.out.println(randomNumberGeneratorId);
        }
    }
}