package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.TestRandomGeneratorIdTranslatorSpec;
import nucleus.PluginData;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

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

    private static void checkSame(StochasticsPluginData actualPluginData) {

        boolean isSame = true;

        if(actualPluginData.getSeed() != 524805676405822016L) {
            isSame = printNotSame();
        }

        Set<TestRandomGeneratorId> expectedRandomGeneratorIds = EnumSet.allOf(TestRandomGeneratorId.class);
        // assertFalse(expectedRandomGeneratorIds.isEmpty());

        Set<RandomNumberGeneratorId> actualsGeneratorIds = actualPluginData.getRandomNumberGeneratorIds();

        if(!expectedRandomGeneratorIds.equals(actualsGeneratorIds)) {
            isSame = printNotSame();
        }

        if (isSame) {
            System.out.println("Datas are the same");
        }
    }
    public static void main(String[] args) {

        String inputFileName = "./stochastics-plugin/src/main/resources/json/input.json";
        String outputFileName = "./stochastics-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(StochasticsTranslator.getTranslator(inputFileName, outputFileName))
                .addTranslatorSpec(new TestRandomGeneratorIdTranslatorSpec())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        StochasticsPluginData stochasticsPluginData = (StochasticsPluginData) pluginDatas.get(0);

        checkSame(stochasticsPluginData);

        translatorController.writeOutput();
    }
}