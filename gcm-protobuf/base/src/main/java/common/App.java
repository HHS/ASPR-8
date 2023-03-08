package common;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import base.TranslatorController;
import common.simobjects.PropertyValueMap;
import common.translators.PropertyValueMapTranslator;
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

        String inputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\base\\src\\main\\resources\\json\\testJson1.json";
        String outputFileName = "C:\\Dev\\CDC\\ASPR-8\\gcm-protobuf\\base\\src\\main\\resources\\json\\output\\testJson1Output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(PropertiesPluginBundle.getPluginBundle(inputFileName, outputFileName,
                        PropertyValueMapInput.getDefaultInstance()))
                .addCustomTranslator(new TestMessageTranslator())
                .addCustomTranslator(new Layer1Translator())
                .addCustomTranslator(new PropertyValueMapTranslator())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        PropertyValueMap map = (PropertyValueMap) objects.get(0);

        if (TestMessageSimObject.class.isAssignableFrom(map.getPropertyId().getClass())) {
            System.out.println("key is Test Message Actual");
        }

        if (String.class.isAssignableFrom(map.getPropertyValue().getClass())) {
            System.out.println("value is String");
        }

        System.out.println(map.getPropertyId().toString());
        System.out.println(map.getPropertyValue().toString());

        translatorController.writeOutput();

    }
}
