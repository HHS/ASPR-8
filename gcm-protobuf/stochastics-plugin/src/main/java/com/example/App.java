package com.example;

import java.io.InputStreamReader;

import com.example.tutorial.protos.Layer1;
import com.example.tutorial.protos.Layer2;
import com.example.tutorial.protos.Layer3;
import com.example.tutorial.protos.Layer4;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

public class App {

    private JsonObject testObj1;
    private JsonObject testObj2;
    private JsonObject testObj3;
    private JsonObject testObj4;

    private App() {
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(this.getClass().getResourceAsStream("/json/testJson1.json")));
        this.testObj1 = JsonParser.parseReader(jsonReader).getAsJsonObject();

        jsonReader = new JsonReader(new InputStreamReader(this.getClass().getResourceAsStream("/json/testJson2.json")));
        this.testObj2 = JsonParser.parseReader(jsonReader).getAsJsonObject();

        jsonReader = new JsonReader(new InputStreamReader(this.getClass().getResourceAsStream("/json/testJson3.json")));
        this.testObj3 = JsonParser.parseReader(jsonReader).getAsJsonObject();

        jsonReader = new JsonReader(new InputStreamReader(this.getClass().getResourceAsStream("/json/testJson4.json")));
        this.testObj4 = JsonParser.parseReader(jsonReader).getAsJsonObject();
    }

    private void testSettingUnknownValue() {
        Layer1.Builder l1UnknownBuilder = Layer1.newBuilder();
        JsonObject testUnknownObjectl1 = testObj4.get("l1_Unknown").getAsJsonObject();

        Layer2.Builder l2UnknownBuilder = Layer2.newBuilder();
        JsonObject testUnknownObjectl2 = testObj4.get("l2_Unknown").getAsJsonObject();

        Layer3.Builder l3UnknownBuilder = Layer3.newBuilder();
        JsonObject testUnknownObjectl3 = testObj3;

        try {
            JsonFormat.parser().merge(testUnknownObjectl1.toString(), l1UnknownBuilder);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            JsonFormat.parser().merge(testUnknownObjectl2.toString(), l2UnknownBuilder);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            JsonFormat.parser().merge(testUnknownObjectl3.toString(), l3UnknownBuilder);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void testSettingUnknownValueWithIgnore() throws InvalidProtocolBufferException {
        Layer1.Builder l1UnknownBuilder = Layer1.newBuilder();
        JsonObject testUnknownObjectl1 = testObj4.get("l1_Unknown").getAsJsonObject();

        Layer2.Builder l2UnknownBuilder = Layer2.newBuilder();
        JsonObject testUnknownObjectl2 = testObj4.get("l2_Unknown").getAsJsonObject();

        Layer3.Builder l3UnknownBuilder = Layer3.newBuilder();
        JsonObject testUnknownObjectl3 = testObj3;

        JsonFormat.parser().ignoringUnknownFields().merge(testUnknownObjectl1.toString(), l1UnknownBuilder);
        JsonFormat.parser().ignoringUnknownFields().merge(testUnknownObjectl2.toString(), l2UnknownBuilder);
        JsonFormat.parser().ignoringUnknownFields().merge(testUnknownObjectl3.toString(), l3UnknownBuilder);

        Layer1 layer1 = l1UnknownBuilder.build();
        System.out.println(layer1);
        Layer2 layer2 = l2UnknownBuilder.build();
        System.out.println(layer2);
        Layer3 layer3 = l3UnknownBuilder.build();
        System.out.println(layer3);
    }

    private void test1LayerMerge() throws InvalidProtocolBufferException {
        Layer1.Builder l1Builder = Layer1.newBuilder();

        JsonObject l1_1Obj = testObj4.get("l1_1").getAsJsonObject();
        JsonObject l1_2Obj = testObj4.get("l1_2").getAsJsonObject();

        JsonObject finalObject = new JsonObject();
        deepMerge(l1_1Obj, finalObject);
        deepMerge(l1_2Obj, finalObject);
        JsonFormat.parser().ignoringUnknownFields().merge(finalObject.toString(), l1Builder);

        Layer1 layer1 = l1Builder.build();
        System.out.println(layer1);
    }

    private void test2LayerMerge() throws InvalidProtocolBufferException {
        Layer2.Builder l2Builder = Layer2.newBuilder();

        JsonObject l2_1Obj = testObj4.get("l2_1").getAsJsonObject();
        JsonObject l2_2Obj = testObj4.get("l2_2").getAsJsonObject();

        JsonObject finalObject = new JsonObject();
        deepMerge(l2_1Obj, finalObject);
        deepMerge(l2_2Obj, finalObject);
        JsonFormat.parser().merge(finalObject.toString(), l2Builder);

        Layer2 layer2 = l2Builder.build();
        System.out.println(layer2);
    }

    private void test3LayerMerge() throws InvalidProtocolBufferException {
        Layer4.Builder l3Builder = Layer4.newBuilder();

        JsonObject l4_1Obj = testObj1;
        JsonObject l3_2Obj = testObj2;

        JsonObject finalObject = new JsonObject();
        deepMerge(l4_1Obj, finalObject);
        deepMerge(l3_2Obj, finalObject.getAsJsonObject("geoSpatials"));
        JsonFormat.parser().merge(finalObject.toString(), l3Builder);

        Layer4 layer4 = l3Builder.build();
        System.out.println(layer4);
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

    // public <T extends Message> GlobalPropertiesPluginData getGlobalPropertiesPluginDataFromInput(List<Class<T>> messageTypes) {

    // }
    // private input thats defined in a proto file thats been read in via input data
    // public GlobalPropertiesPluginData getGlobalPropertiesPluginDataFromInput() {
    //     // IF AND ONLY IF ALL PROPERTY VALUES ARE PRIMITIVE TYPES
    //     PropertyValue value;

    //     // The Contract says: if it is a primitive type, we will return the Sim Plugin Data
    //     // If it is "any" type, we will throw an error because we do not know what type of data it is supposed to be
    //     // call the method that has the message type as a paramater

    //     if(value.getValueCase().compareTo(ValueCase.ANYVALUE)) {
            
    //     }
    // }








    public static void main(String[] args) {
        App myapp = new App();

        try {
            myapp.test1LayerMerge();
            // myapp.test2LayerMerge();
            // myapp.test3LayerMerge();

            // should throw
            // myapp.testSettingUnknownValue();

            // should not throw
            // myapp.testSettingUnknownValueWithIgnore();
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}