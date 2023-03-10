package gov.hhs.aspr.gcm.gcmprotobuf.properties;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.gcmprotobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.simobjects.PropertyValueMap;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.testsupport.simobjects.TestMessageSimObject;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.testsupport.translators.Layer1Translator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.testsupport.translators.TestMessageTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.translators.PropertyDefinitionMapTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.translators.PropertyValueMapTranslator;
import plugins.properties.input.PropertyValueMapInput;

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

        String inputFileName = "./properties-plugin/src/main/resources/json/input.json";
        String outputFileName = "./properties-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addBundle(PropertiesPluginBundle.getPluginBundle(inputFileName, outputFileName,
                        PropertyValueMapInput.getDefaultInstance()))
                .addTranslator(new TestMessageTranslator())
                .addTranslator(new Layer1Translator())
                .addTranslator(new PropertyValueMapTranslator())
                .addTranslator(new PropertyDefinitionMapTranslator())
                .build()
                .init();

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
