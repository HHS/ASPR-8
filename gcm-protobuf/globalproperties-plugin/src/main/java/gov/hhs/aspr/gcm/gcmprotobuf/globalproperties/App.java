package gov.hhs.aspr.gcm.gcmprotobuf.globalproperties;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.gcmprotobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.gcmprotobuf.globalproperties.translators.TestGlobalPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundle;
import nucleus.PluginData;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

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

    private static void checkSame(GlobalPropertiesPluginData actualPluginData) {
        boolean isSame = true;
        Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);

        Set<GlobalPropertyId> actualGlobalPropertyIds = actualPluginData.getGlobalPropertyIds();
        if (!expectedPropertyIds.equals(actualGlobalPropertyIds)) {
            isSame = printNotSame();
        }

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getGlobalPropertyDefinition(testGlobalPropertyId);

            if (!expectedPropertyDefinition.equals(actualPropertyDefinition)) {
                isSame = printNotSame();
            }
        }

        if (isSame) {
            System.out.println("PluginDatas are the same");
        }
    }

    public static void main(String[] args) {

        String inputFileName = "./globalproperties-plugin/src/main/resources/json/testJson2.json";
        String outputFileName = "./globalproperties-plugin/src/main/resources/json/output/testJson2Output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(GlobalPropertiesPluginBundle.getPluginBundle(inputFileName, outputFileName))
                .addBundle(PropertiesPluginBundle.getPluginBundle())
                .addTranslator(new TestGlobalPropertyIdTranslator())
                .build()
                .init();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GlobalPropertiesPluginData actualPluginData = (GlobalPropertiesPluginData) pluginDatas.get(0);

        checkSame(actualPluginData);

        translatorController.writeOutput();
    }
}