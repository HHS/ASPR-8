package com.example;

import com.google.gson.JsonObject;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import common.CommonTranslator;
import common.ITranslator;
import common.PropertyDefinitionMap;
import common.PropertyValueMap;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesTranslator implements ITranslator {

    private Data data;

    private GlobalPropertiesTranslator(Data data) {
        this.data = data;
    }

    private static class Data {
        private CommonTranslator commonTranslator;

        private Data() {
            this.commonTranslator = CommonTranslator.builder()
                    .addDescriptor(GlobalPropertiesPluginDataInput.getDefaultInstance()).build();
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public GlobalPropertiesTranslator build() {
            return new GlobalPropertiesTranslator(this.data);
        }

        public Builder setCommonTranslator(CommonTranslator commonTranslator) {
            this.data.commonTranslator = commonTranslator;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    public Parser getJsonParser() {
        return this.data.commonTranslator.getJsonParser();
    }

    public Printer getJsonPrinter() {
        return this.data.commonTranslator.getJsonPrinter();
    }

    public void printJson(Message message) {
        this.data.commonTranslator.printJson(message);
    }

    public <T extends Message, U extends Message.Builder> T parseJson(JsonObject inputJson, U builder) {
        return this.data.commonTranslator.parseJson(inputJson, builder);
    }

    public CommonTranslator getCommonTranslator() {
        return this.data.commonTranslator;
    }

    public GlobalPropertiesPluginData convertInputToPluginData(GlobalPropertiesPluginDataInput input) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMap propertyDefinitionMap : input.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(
                    this.data.commonTranslator.getObjectFromInput(propertyDefinitionMap.getPropertyId()));
            PropertyDefinition propertyDefinition = this.data.commonTranslator
                    .convertInputToPropertyDefinition(propertyDefinitionMap.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition);
        }

        for (PropertyValueMap propertyValueMap : input.getGlobalPropertyValuesList()) {
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(
                    this.data.commonTranslator.getObjectFromInput(propertyValueMap.getPropertyId()));
            Object value = this.data.commonTranslator.getObjectFromInput(propertyValueMap.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value);
        }

        return builder.build();
    }

}
