package com.example;

import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import common.PropertyDefinitionInput;
import common.TestMessage;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class App {
    private JsonObject inputJson;
    private Parser jsonParser;
    private Printer jsonPrinter;

    private App(String inputFileName) {
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(this.getClass().getResourceAsStream(inputFileName)));
        this.inputJson = JsonParser.parseReader(jsonReader).getAsJsonObject();

        this.jsonParser = JsonFormat.parser().usingTypeRegistry(getTypeRegistry());
        this.jsonPrinter = JsonFormat.printer().includingDefaultValueFields().usingTypeRegistry(getTypeRegistry());
    }

    private TypeRegistry getTypeRegistry() {
        TypeRegistry.Builder builder = TypeRegistry.newBuilder();

        // primitive types
        builder.add(BoolValue.getDescriptor())
        .add(Int32Value.getDescriptor())
        .add(UInt32Value.getDescriptor())
        .add(Int64Value.getDescriptor())
        .add(UInt64Value.getDescriptor())
        .add(StringValue.getDescriptor())
        .add(BytesValue.getDescriptor())
        .add(FloatValue.getDescriptor())
        .add(DoubleValue.getDescriptor());

        return builder.build();
    }

    public GlobalPropertiesPluginDataInput parseInput() {
       
        JsonObject jsonObject = inputJson.deepCopy();

        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        

        try {
            jsonParser.merge(jsonObject.toString(), builder);
            GlobalPropertiesPluginDataInput input = builder.build();
            printInput(input);
            return input;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printInput(Message message) {
        try {
            System.out.println(this.jsonPrinter.print(message));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

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
        App app = new App("/json/testJson1.json");

        GlobalPropertiesPluginDataInput input = app.parseInput();

        GlobalPropertiesTranslator translator = GlobalPropertiesTranslator.builder().build();
        GlobalPropertiesPluginData pluginData = translator.convertInputToPluginData(input);

        app.printInput(input);

        System.out.println(pluginData.getGlobalPropertyIds());
        for(GlobalPropertyId id : pluginData.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = pluginData.getGlobalPropertyDefinition(id);
            Object value = pluginData.getGlobalPropertyValue(id);
            System.out.println(propertyDefinition);
            System.out.println(value);

        }
    }
}
