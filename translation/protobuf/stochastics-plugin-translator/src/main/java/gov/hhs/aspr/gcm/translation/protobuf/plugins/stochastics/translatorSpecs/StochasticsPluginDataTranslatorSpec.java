package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.WellStateInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.WellState;

public class StochasticsPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<StochasticsPluginDataInput, StochasticsPluginData> {

    @Override
    protected StochasticsPluginData convertInputObject(StochasticsPluginDataInput inputObject) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = this.translator.convertInputObject(inputObject.getWellState());

        builder.setMainRNGState(wellState);

        for (RandomNumberGeneratorMapInput randomGenIdInput : inputObject.getRandomNumberGeneratorIdsList()) {
            RandomNumberGeneratorId generatorId = this.translator
                    .convertInputObject(randomGenIdInput.getRandomNumberGeneratorId(), RandomNumberGeneratorId.class);
            WellState generatorWellState = this.translator.convertInputObject(randomGenIdInput.getWellState());
            builder.addRNG(generatorId, generatorWellState);
        }

        return builder.build();
    }

    @Override
    protected StochasticsPluginDataInput convertAppObject(StochasticsPluginData simObject) {
        StochasticsPluginDataInput.Builder builder = StochasticsPluginDataInput.newBuilder();

        WellStateInput wellStateInput = this.translator.convertSimObject(simObject.getWellState());
        builder.setWellState(wellStateInput);

        for (RandomNumberGeneratorId randomNumberGeneratorId : simObject.getRandomNumberGeneratorIds()) {
            RandomNumberGeneratorIdInput randomNumberGeneratorIdInput = this.translator
                    .convertSimObject(randomNumberGeneratorId, RandomNumberGeneratorId.class);
            WellStateInput generatorWellStateInput = this.translator
                    .convertSimObject(simObject.getWellState(randomNumberGeneratorId));

            builder.addRandomNumberGeneratorIds(
                    RandomNumberGeneratorMapInput.newBuilder().setWellState(generatorWellStateInput)
                            .setRandomNumberGeneratorId(randomNumberGeneratorIdInput).build());
        }

        return builder.build();
    }

    @Override
    public StochasticsPluginDataInput getDefaultInstanceForInputObject() {
        return StochasticsPluginDataInput.getDefaultInstance();
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
