package common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import base.MasterTranslator;
import base.TranslatorController;
import common.translators.PropertiesPluginBundle;
import testsupport.simobjects.TestMessageSimObject;
import testsupport.translators.Layer1Translator;
import testsupport.translators.TestMessageTranslator;

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
                .addBundle(new PropertiesPluginBundle())
                .addCustomTranslator(new TestMessageTranslator())
                .addCustomTranslator(new Layer1Translator())
                .build();

        translatorController.loadInput();

        MasterTranslator masterTranslator = translatorController.getMasterTranslator();

        PropertyValueMap map = masterTranslator.parseJson(
                "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\base\\src\\main\\resources\\json\\testJson1.json",
                PropertyValueMap.newBuilder());

        Object key = masterTranslator.getObjectFromAny(map.getPropertyId());
        Object value = masterTranslator.getObjectFromAny(map.getPropertyValue());

        masterTranslator.printJson(map);

        if (TestMessageSimObject.class.isAssignableFrom(key.getClass())) {
            System.out.println("key is Test Message Actual");
        }

        if (String.class.isAssignableFrom(value.getClass())) {
            System.out.println("value is String");
        }

        System.out.println(key.toString());
        System.out.println(value.toString());

    }
}
