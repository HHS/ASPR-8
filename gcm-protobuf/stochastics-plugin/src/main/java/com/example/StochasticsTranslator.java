package com.example;

import com.google.gson.JsonObject;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import common.CommonTranslator;
import common.ITranslator;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginDataInput;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.SimpleRandomNumberGeneratorId;

public class StochasticsTranslator implements ITranslator {

    private Data data;

    private StochasticsTranslator(Data data) {
        this.data = data;
    }

    private static class Data {
        private CommonTranslator commonTranslator;

        private Data() {
            this.commonTranslator = CommonTranslator.builder()
                    .addDescriptor(StochasticsPluginDataInput.getDefaultInstance()).build();
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public StochasticsTranslator build() {
            return new StochasticsTranslator(this.data);
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

    public <T extends Message, U extends Message.Builder> T parseJson(String inputFileName, U builder) {
        return this.data.commonTranslator.parseJson(inputFileName, builder);
    }

    public <T extends Message, U extends Message.Builder> T parseJson(JsonObject inputJson, U builder) {
        return this.data.commonTranslator.parseJson(inputJson, builder);
    }

    public CommonTranslator getCommonTranslator() {
        return this.data.commonTranslator;
    }

    public StochasticsPluginData convertInputToPluginData(StochasticsPluginDataInput input) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        builder.setSeed(input.getSeed());

        for (Any randomGenIdInput : input.getRandomNumberGeneratorIdsList()) {
            Object randomGenId = this.data.commonTranslator.getObjectFromInput(randomGenIdInput);
            RandomNumberGeneratorId generatorId = new SimpleRandomNumberGeneratorId(randomGenId);
            builder.addRandomGeneratorId(generatorId);
        }

        return builder.build();
    }

}
