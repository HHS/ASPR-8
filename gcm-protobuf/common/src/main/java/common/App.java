package common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import testsupport.simobjects.TestMessageSimObject;
import testsupport.translators.Layer1Translator;
import testsupport.translators.TestMessageTranslator;

import com.google.protobuf.Message;

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

        Translator commonTranslator = CommonTranslator.builder().addCustomTranslator(new TestMessageTranslator())
                .addCustomTranslator(new Layer1Translator())
                .build();

        PropertyValueMap map = commonTranslator.parseJson("/json/testJson1.json", PropertyValueMap.newBuilder());

        Object key = commonTranslator.getObjectFromAny(map.getPropertyId());
        Object value = commonTranslator.getObjectFromAny(map.getPropertyValue());

        commonTranslator.printJson(map);

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
