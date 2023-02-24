package com.example;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import common.CommonTranslator;
import common.ITranslator;
import common.ITranslatorBuilder;
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
        }
    }

    public static class Builder implements ITranslatorBuilder {
        private Data data;
        private CommonTranslator.Builder commonTranslatorBuilder = CommonTranslator.builder();

        private Builder(Data data) {
            this.data = data;
            addDescriptorsForTranslator(this.commonTranslatorBuilder);
        }

        public StochasticsTranslator build() {
            this.data.commonTranslator = this.commonTranslatorBuilder.build();
            return new StochasticsTranslator(this.data);
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.commonTranslatorBuilder.setIgnoringUnknownFields(ignoringUnknownFields);
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.commonTranslatorBuilder.setIncludingDefaultValueFields(includingDefaultValueFields);
            return this;
        }

        public Builder addDescriptor(Message message) {
            this.commonTranslatorBuilder.addDescriptor(message);

            return this;
        }
    }

    private static List<Message> getDescriptorsForTranslator() {
        return Arrays.asList(StochasticsPluginDataInput.getDefaultInstance());
    }

    private static void addDescriptorsForTranslator(CommonTranslator.Builder builder) {
        for (Message message : getDescriptorsForTranslator()) {
            builder.addDescriptor(message);
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
