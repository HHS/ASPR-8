package plugins.stochastics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import base.MasterTranslator;
import base.TranslatorController;
import common.translators.PropertiesPluginBundle;
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

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(new StochasticsPluginBundle())
                .addBundle(new PropertiesPluginBundle())
                .build();

        translatorController.loadInput();

        MasterTranslator masterTranslator = translatorController.getMasterTranslator();

        StochasticsPluginDataInput inputData = masterTranslator.parseJson(
                "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\stochastics-plugin\\src\\main\\resources\\json\\testJson1.json",
                StochasticsPluginDataInput.newBuilder());

        StochasticsPluginData stochasticsPluginData = (StochasticsPluginData) masterTranslator
                .convertInputObject(inputData);

        masterTranslator.printJson(inputData);

        System.out.println(stochasticsPluginData.getSeed());
        System.out.println(stochasticsPluginData.getSeed() == 524805676405822016L);

        for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
            System.out.println(randomNumberGeneratorId);
        }
    }
}