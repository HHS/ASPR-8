package common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
        
        CommonTranslator commonTranslator = CommonTranslator.builder().addDescriptor(TestMessage.getDefaultInstance()).build();

        PropertyValueMap map = commonTranslator.parseJson("/json/testJson1.json", PropertyValueMap.newBuilder());


        Object key = commonTranslator.getObjectFromInput(map.getPropertyId());
        Object value = commonTranslator.getObjectFromInput(map.getPropertyValue());

        commonTranslator.printJson(map);

        if(Integer.class.isAssignableFrom(key.getClass())) {
            System.out.println("key is int");
        }

        if(String.class.isAssignableFrom(value.getClass())) {
            System.out.println("value is String");
        }

        System.out.println(key.toString());
        System.out.println(value.toString());

    }
}
