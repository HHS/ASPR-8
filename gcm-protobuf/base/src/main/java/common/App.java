package common;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import base.TranslatorController;
import testsupport.simobjects.PropertyValueMapSimObject;
import testsupport.simobjects.TestMessageSimObject;
import testsupport.translators.Layer1Translator;
import testsupport.translators.PropertyValueMapTranslator;
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

        String inputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\base\\src\\main\\resources\\json\\testJson1.json";
        String outputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\base\\src\\main\\resources\\json\\output\\testJson1Output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(PropertiesPluginBundle.getPluginBundle(inputFileName, outputFileName,
                        PropertyValueMap.getDefaultInstance()))
                .addCustomTranslator(new TestMessageTranslator())
                .addCustomTranslator(new Layer1Translator())
                .addCustomTranslator(new PropertyValueMapTranslator())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        PropertyValueMapSimObject map = (PropertyValueMapSimObject) objects.get(0);

        if (TestMessageSimObject.class.isAssignableFrom(map.getKey().getClass())) {
            System.out.println("key is Test Message Actual");
        }

        if (String.class.isAssignableFrom(map.getValue().getClass())) {
            System.out.println("value is String");
        }

        System.out.println(map.getKey().toString());
        System.out.println(map.getValue().toString());

        translatorController.writeOutput();

    }
}
