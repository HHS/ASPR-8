package gov.hhs.aspr.gcm.translation.plugins.properties;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.properties.simobjects.PropertyValueMap;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.simobjects.TestMessageSimObject;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.translatorSpecs.Layer1TranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.translatorSpecs.TestMessageTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.PropertyDefinitionMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.PropertyValueMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyValueMapInput;

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
                .addTranslator(PropertiesTranslator.getBaseTranslatorBuilder()
                        .addInputFile(inputFileName, PropertyValueMapInput.getDefaultInstance())
                        .addOutputFile(outputFileName, PropertyValueMap.class).build())
                .addTranslatorSpec(new TestMessageTranslatorSpec())
                .addTranslatorSpec(new Layer1TranslatorSpec())
                .addTranslatorSpec(new PropertyValueMapTranslatorSpec())
                .addTranslatorSpec(new PropertyDefinitionMapTranslatorSpec())
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
