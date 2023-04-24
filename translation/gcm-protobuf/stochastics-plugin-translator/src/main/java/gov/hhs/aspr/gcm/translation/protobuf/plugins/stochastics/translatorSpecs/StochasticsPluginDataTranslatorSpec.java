package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.WellStateInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.WellState;

public class StochasticsPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<StochasticsPluginDataInput, StochasticsPluginData> {

    @Override
    protected StochasticsPluginData convertInputObject(StochasticsPluginDataInput inputObject) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = this.translatorCore.convertObject(inputObject.getWellState());

        builder.setMainRNGState(wellState);

        for (RandomNumberGeneratorMapInput randomGenIdInput : inputObject.getRandomNumberGeneratorIdsList()) {
            RandomNumberGeneratorId generatorId = this.translatorCore
                    .convertObject(randomGenIdInput.getRandomNumberGeneratorId());
            WellState generatorWellState = this.translatorCore.convertObject(randomGenIdInput.getWellState());
            builder.addRNG(generatorId, generatorWellState);
        }

        return builder.build();
    }

    @Override
    protected StochasticsPluginDataInput convertAppObject(StochasticsPluginData simObject) {
        StochasticsPluginDataInput.Builder builder = StochasticsPluginDataInput.newBuilder();

        WellStateInput wellStateInput = this.translatorCore.convertObject(simObject.getWellState());
        builder.setWellState(wellStateInput);

        for (RandomNumberGeneratorId randomNumberGeneratorId : simObject.getRandomNumberGeneratorIds()) {
            RandomNumberGeneratorIdInput randomNumberGeneratorIdInput = this.translatorCore
                    .convertObjectAsSafeClass(randomNumberGeneratorId, RandomNumberGeneratorId.class);
            WellStateInput generatorWellStateInput = this.translatorCore
                    .convertObject(simObject.getWellState(randomNumberGeneratorId));

            builder.addRandomNumberGeneratorIds(
                    RandomNumberGeneratorMapInput.newBuilder().setWellState(generatorWellStateInput)
                            .setRandomNumberGeneratorId(randomNumberGeneratorIdInput).build());
        }

        return builder.build();
    }

    @Override
    public Class<StochasticsPluginData> getAppObjectClass() {
        return StochasticsPluginData.class;
    }

    @Override
    public Class<StochasticsPluginDataInput> getInputObjectClass() {
        return StochasticsPluginDataInput.class;
    }

}
