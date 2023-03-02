package plugins.globalproperties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import base.MasterTranslator;
import base.TranslatorController;
import common.translators.PropertiesPluginBundle;
import plugins.globalproperties.support.GlobalPropertyId;
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

    public static void main(String[] args) {
        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(new GlobalPropertiesPluginBundle())
                .addBundle(new PropertiesPluginBundle())
                .build();

        translatorController.loadInput();

        MasterTranslator masterTranslator = translatorController.getMasterTranslator();

        GlobalPropertiesPluginDataInput inputData = masterTranslator.parseJson(
                "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\globalproperties-plugin\\src\\main\\resources\\json\\testJson1.json",
                GlobalPropertiesPluginDataInput.newBuilder());

        GlobalPropertiesPluginData pluginData = (GlobalPropertiesPluginData) masterTranslator.convertInputObject(inputData);

        masterTranslator.printJson(inputData);

        System.out.println(pluginData.getGlobalPropertyIds());
        for (GlobalPropertyId id : pluginData.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = pluginData.getGlobalPropertyDefinition(id);
            Object value = pluginData.getGlobalPropertyValue(id);
            System.out.println(propertyDefinition);
            System.out.println(value);

        }
    }
}
